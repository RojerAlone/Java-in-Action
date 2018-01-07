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
@Override
public V get(K key) throws ExecutionException {
    return localCache.getOrLoad(key);
}
```