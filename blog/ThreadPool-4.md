# ThreadPool 之 线程池工具类 `Executors`
## 用 `Executors` 创建线程池
　　JDK 中提供了一个 `Executors` 类，在这个类中构造好了几个通用的线程池，并且 JDK 文档中也强烈推荐（urged to use）使用 `Executors` 来构建线程池。
### `Executors`
``` Java
/**
 * 创建容量为 nThreads 的线程池，最大工作线程也为 nThreads，也就是不允许有挽救策略
 * 等待队列为 LinkedBlockingQueue，也就是等待队列容量为 Integer.MAX_VALUE
 */
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}
/**
 * 创建单例线程池，只有一个线程执行工作，等待队列容量为 Integer.MAX_VALUE
 */
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}
/**
 * 可缓冲的线程池，核心池被设置为 0，池最大容量为 Integer.MAX_VALUE，
 * 也就是如果有新任务但是没有空闲的 Worker，就会创建新线程执行任务
 * 空闲 60 秒的工作线程将被销毁
 * SynchronousQueue是一个没有容量的阻塞队列。每个插入操作必须等待另一个线程的对应移除操作
 * 因此实现了如果有空闲线程就继续工作，没有空闲线程就创建线程执行任务
 */
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```
## 如何配置线程池数量？
> 　　要想合理地配置线程池，就必须首先分析任务特性，可以从以下几个角度来分析。
>
> - 任务的性质：CPU 密集型任务、IO 密集型任务和混合型任务。
> - 任务的优先级：高、中和低。
> - 任务的执行时间：长、中和短。
> - 任务的依赖性：是否依赖其他系统资源，如数据库连接。
>
> 性质不同的任务可以用不同规模的线程池分开处理。CPU 密集型任务应配置尽可能小的线程，如配置 Ncpu+1 个线程的线程池。由于 IO 密集型任务线程并不是一直在执行任务，则应配置尽可能多的线程，如 2*Ncpu 。混合型的任务，如果可以拆分，将其拆分成一个 CPU 密集型任务和一个 IO 密集型任务，只要这两个任务执行的时间相差不是太大，那么分解后执行的吞吐量将高于串行执行的吞吐量。如果这两个任务执行时间相差太大，则没必要进行分解。可以通过 `Runtime.getRuntime().availableProcessors()` 方法获得当前设备的CPU个数。
> <div align="right">——《Java 并发编程的艺术》 </div> 

　　
