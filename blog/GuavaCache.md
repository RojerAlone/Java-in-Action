# Guava Cache 源码解析
## Builder
Guava Cache 的构建是用 Builder Pattern 模式构建的，构建方式如下：
``` Java
Cache<String, String> cache = CacheBuilder.newBuilder().build();
```
这是最简单的构建方法，不包含缓存更新等设置。

`build` 方法有两个重载方法，`<K1 extends K, V1 extends V> Cache<K1, V1> build()` 和 `<K1 extends K, V1 extends V> LoadingCache<K1, V1> build(CacheLoader<? super K1, V1> loader)`，一个返回 `Cache`，一个返回 `LoadingCache`，`LoadingCache` 又继承了 `Cache`，因此也可以赋值给 `Cache` 变量。
``` Java
public <K1 extends K, V1 extends V> LoadingCache<K1, V1> build(
    CacheLoader<? super K1, V1> loader) {
    checkWeightWithWeigher();
    return new LocalCache.LocalLoadingCache<K1, V1>(this, loader);
}

public <K1 extends K, V1 extends V> Cache<K1, V1> build() {
    checkWeightWithWeigher();
    checkNonLoadingCache();
    return new LocalCache.LocalManualCache<K1, V1>(this);
}
```
## Cache 和 LoadingCache
通过源码来看一下 `Cache` 和 `LoadingCache` 有什么不同。
``` Java
public interface Cache<K, V> {

  V getIfPresent(Object key);

  V get(K key, Callable<? extends V> loader) throws ExecutionException;

  ImmutableMap<K, V> getAllPresent(Iterable<?> keys);

  void put(K key, V value);

  void putAll(Map<? extends K, ? extends V> m);

  void invalidate(Object key);

  void invalidateAll(Iterable<?> keys);

  void invalidateAll();

  long size();

  CacheStats stats();

  ConcurrentMap<K, V> asMap();

  void cleanUp();
  
}

public interface LoadingCache<K, V> extends Cache<K, V>, Function<K, V> {

  V get(K key) throws ExecutionException;

  V getUnchecked(K key);

  ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException;

  @Deprecated
  @Override
  V apply(K key);

  void refresh(K key);

  @Override
  ConcurrentMap<K, V> asMap();
}
```
`Cache` 是缓存，而 `LoadingCache` 是加载的缓存，最大的区别在于 `Cache` 是一个单纯的缓存，开发者可以添加或者删除数据，而 `LoadingCache` 是一个相对来说“智能”一点的缓存，因为已经指定加载缓存的方法了，所以没有提供 put、delete 这样的操作，只有 get 和 refresh 等。

`LoadingCache` 接口中的方法只有 6 个，还有 1 个是已经被废弃的，其中最核心的方法就是 `V get(K key)` 方法，这个方法用来获取 key 对应的值，如果该值没有或者缓存已经失效的话，就会调用 build 时传入的 `CacheLoader` 去加载该值并且存入缓存中，这个方法支持并发操作。而 `Cache` 中的 `get` 方法需要多传入一个 `Callable` 变量，如果没有获取到值的话就会通过传入的 `Callable` 去加载值。

下面来看一下 `LoadingCache` 最核心的 `get` 方法（`LoadingCache` 接口的实现类是 `LocalCache` 中的 `LocalLoadingCache`）。
## LoadingCache.get(K key)
``` Java
static class LocalLoadingCache<K, V> extends LocalManualCache<K, V> 
    implements LoadingCache<K, V> {
    LocalLoadingCache(
        CacheBuilder<? super K, ? super V> builder, CacheLoader<? super K, V> loader) {
        super(new LocalCache<K, V>(builder, checkNotNull(loader)));
    }

    @Override
    public V get(K key) throws ExecutionException {
        /*
         * 这里的 localCache 来自于父类 LocalManualCache，
         * 而父类的 localCache 由 LocalLoadingCache 构造方法传入，传入的是新创建的 LocalCache
         */
        return localCache.getOrLoad(key);
    }
}
```
来看一下 `LocalCache` 的构造方法中做了什么：
``` Java
LocalCache(
      CacheBuilder<? super K, ? super V> builder, @Nullable CacheLoader<? super K, V> loader) {
    // 允许的并发数，默认为 4
    concurrencyLevel = Math.min(builder.getConcurrencyLevel(), MAX_SEGMENTS);
    // key 和 value 的引用强弱，enum 类型，分别为 强引用、软引用、弱引用，默认为 强引用
    keyStrength = builder.getKeyStrength();
    valueStrength = builder.getValueStrength();
    // key 和 value 的比较策略，在 Strength 的 3 种引用中有默认实现
    keyEquivalence = builder.getKeyEquivalence();
    valueEquivalence = builder.getValueEquivalence();
    // 从 builder 中获取 build 之前设置的参数
    maxWeight = builder.getMaximumWeight(); // 最大权重
    weigher = builder.getWeigher(); // 权重计算器
    expireAfterAccessNanos = builder.getExpireAfterAccessNanos(); // 最后一次访问多久之后缓存失效
    expireAfterWriteNanos = builder.getExpireAfterWriteNanos(); // 缓存写入多久之后缓存失效
    refreshNanos = builder.getRefreshNanos(); // 缓存更新时间

    removalListener = builder.getRemovalListener(); // 软引用、弱引用被回收时候的监听器
    removalNotificationQueue =
        (removalListener == NullListener.INSTANCE)
            ? LocalCache.<RemovalNotification<K, V>>discardingQueue()
            : new ConcurrentLinkedQueue<RemovalNotification<K, V>>();

    ticker = builder.getTicker(recordsTime());
    entryFactory = EntryFactory.getFactory(keyStrength, usesAccessEntries(), usesWriteEntries());
    globalStatsCounter = builder.getStatsCounterSupplier().get();
    defaultLoader = loader;

    int initialCapacity = Math.min(builder.getInitialCapacity(), MAXIMUM_CAPACITY);
    if (evictsBySize() && !customWeigher()) {
      initialCapacity = Math.min(initialCapacity, (int) maxWeight);
    }

    // Find the lowest power-of-two segmentCount that exceeds concurrencyLevel, unless
    // maximumSize/Weight is specified in which case ensure that each segment gets at least 10
    // entries. The special casing for size-based eviction is only necessary because that eviction
    // happens per segment instead of globally, so too many segments compared to the maximum size
    // will result in random eviction behavior.
    int segmentShift = 0;
    int segmentCount = 1;
    while (segmentCount < concurrencyLevel && (!evictsBySize() || segmentCount * 20 <= maxWeight)) {
      ++segmentShift;
      segmentCount <<= 1;
    }
    this.segmentShift = 32 - segmentShift;
    segmentMask = segmentCount - 1;

    this.segments = newSegmentArray(segmentCount);

    int segmentCapacity = initialCapacity / segmentCount;
    if (segmentCapacity * segmentCount < initialCapacity) {
      ++segmentCapacity;
    }

    int segmentSize = 1;
    while (segmentSize < segmentCapacity) {
      segmentSize <<= 1;
    }

    if (evictsBySize()) {
      // Ensure sum of segment max weights = overall max weights
      long maxSegmentWeight = maxWeight / segmentCount + 1;
      long remainder = maxWeight % segmentCount;
      for (int i = 0; i < this.segments.length; ++i) {
        if (i == remainder) {
          maxSegmentWeight--;
        }
        this.segments[i] =
            createSegment(segmentSize, maxSegmentWeight, builder.getStatsCounterSupplier().get());
      }
    } else {
      for (int i = 0; i < this.segments.length; ++i) {
        this.segments[i] =
            createSegment(segmentSize, UNSET_INT, builder.getStatsCounterSupplier().get());
      }
    }
}
```
继续看 `getOrLoad` 方法：
``` Java
V getOrLoad(K key) throws ExecutionException {
    // defaultLoader 是 build(CacheLoader loader) 传入的 loader
    return get(key, defaultLoader);
}

V get(K key, CacheLoader<? super K, V> loader) throws ExecutionException {
    int hash = hash(checkNotNull(key)); // 获取 hash 值
    return segmentFor(hash).get(key, hash, loader); // 根据 hash 值找到对应的桶，并调用 get 方法
}
```

`get` 方法根据哈希值找到对应的桶，这里 `LoadingCache` 使用的是一个自定义的 `Segment` 数组，找到 `key` 对应的 `Segment` 对象后，调用 `Segment` 对象的 `get` 方法。
### Segment

`Segment` 中有 58 个方法，14 个成员变量，过于庞大，先来看一下各个成员变量。

``` Java
    /**
     * 当前 Segment 所属的 LocalCache 对象
     */
    @Weak 
    final LocalCache<K, V> map;

    /**
     * 仍然有效的元素个数
     */
    volatile int count;

    /**
     * 仍然有效元素的权重之和
     */
    @GuardedBy("this")
    long totalWeight;

    /**
     * 记录更改哈希表大小的次数，如果一个线程操作过程中计数改变，需要重试
     */
    int modCount;

    /**
     * 哈希表扩容时候的上限，和 HashMap 一样，通常是总容量的 0.75
     */
    int threshold;

    /**
     * 每个段的表
     */
    volatile AtomicReferenceArray<ReferenceEntry<K, V>> table;

    /**
     * 当前分段的最大权重，如果没有最大权重，就是 UNSET_INT 
     */
    final long maxSegmentWeight;

    /**
     * 存放已经过期被清理的 key 和需要被清除的 key
     */
    final ReferenceQueue<K> keyReferenceQueue;

    /**
     * 存放已经过期被清理的 value 和需要被清除的 value
     */
    final ReferenceQueue<V> valueReferenceQueue;

    /**
     * TODO
     *
     * The recency queue is used to record which entries were accessed for updating the access
     * list's ordering. It is drained as a batch operation when either the DRAIN_THRESHOLD is
     * crossed or a write occurs on the segment.
     */
    final Queue<ReferenceEntry<K, V>> recencyQueue;

    /**
     * 自从上次被写入以后，读取的次数，用来清理过期队列
     */
    final AtomicInteger readCount = new AtomicInteger();

    /**
     * 当前 map 中的队列，一个链表，按照写入的顺序排序，新写入的元素放在尾部
     */
    @GuardedBy("this")
    final Queue<ReferenceEntry<K, V>> writeQueue;

    /**
     * 当前 map 中的队列，按照访问时间排序
     */
    @GuardedBy("this")
    final Queue<ReferenceEntry<K, V>> accessQueue;

    /** 缓存统计数据 */
    final StatsCounter statsCounter;
```