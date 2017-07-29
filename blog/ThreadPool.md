# ThreadPool
[注]本文中的源码基于 JDK1.8，源码中的注释为 JDK 中注释的翻译加上个人的理解。如有错误欢迎指正。
## 引言
　　因为进程的切换相当耗费资源，加上 CPU 的发展，操作系统中引入了线程的概念。相比于进程的上下文切换，线程的切换更轻量级，但是不代表没有开销，而且大部分多线程的生命周期都比较短，会发生频繁的线程创建、销毁动作，这也是相当消耗资源的，因此引入了线程池。
　　
　　合理利用线程池能够带来三个好处。第一：降低资源消耗。通过重复利用已创建的线程降低线程创建和销毁造成的消耗。第二：提高响应速度。当任务到达时，任务可以不需要的等到线程创建就能立即执行。第三：提高线程的可管理性。线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一的分配，调优和监控。
## 线程池的结构
　　`ThreadPool` 的实现结构如下图所示：
![ThreadPool 结构](http://i.imgur.com/OTJeDr2.png)
## `Executor` 接口
　　`ThreadPoolExecutor` 是最终的线程池实现类，顶层接口是 `Executor`，查看 `Executor` 源码，这个接口中只有一个方法：
``` Java
public interface ExecutorSource {

        /**
         * 在未来某个时间执行参数中的命令，这个命令可能在一个新的线程、线程池中的线程或者一个调用线程(?)中被执行
         */
        void execute(Runnable command);
}
```
## `ExecutorService` 接口
　　`ExecutorService` 接口继承了 `Executor` 接口，添加了一些对线程池的管理：
``` Java
public interface ExecutorService extends Executor {

    /**
     * 有序地执行完之前提交的任务，但是不会接受新的任务。如果线程池已经被关闭，调用此方法没有额外的影响。
     */
    void shutdown();
    
    /**
     * 尝试停止所有正在运行的任务，停止等待中的线程，返回正在等待执行的任务列表
     */
    List<Runnable> shutdownNow();
    
    /**
     * 获取线程池是否已经被关闭
     */
    boolean isShutdown();

    /**
     * 如果所有的任务都已经被关闭了，返回 true，除非先调用 shutdown 或者 shutdownNow ，否则永远不会返回 true
     */
    boolean isTerminated();
    
    /**
     * 阻塞直到关闭请求后所有任务被完成，或者时间超时，或者线程被中断，不管哪一种情况先发生，根据先发生的情况返回值
     * 也就是说，获取线程池是否关闭，指定了一个时间，在这个时间之前被关闭的话，返回 true，如果超时还没有关闭，返回 false
     */
    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 提交一个任务，会返回一个 Future ， Future 可以返回任务的结果，当任务被成功完成之后可以通过 get 方法获取结果
     */
    <T> Future<T> submit(Callable<T> task);

    /**
     * 提交一个实现了 Runnable 接口的任务以及返回的结果（？？？不懂），返回 Future
     */
    <T> Future<T> submit(Runnable task, T result);

    /**
     * 提交一个实现了 Runnable 接口的任务，返回 Future
     */
    Future<?> submit(Runnable task);

    /**
     * 执行给定的任务，当所有任务完成后返回带有任务状态和结果的 Future 列表。
     * Future.isDone对于返回的列表的每个元素都是正确的。 要注意的是，完成的任务可能会正常终止或抛出异常。
     * 如果在执行任务过程中修改了任务的集合，则这个方法的结果是未定义的。
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException;

    /**
     * 和 invokeAll(Collection<? extends Callable<T>> tasks) 类似
     * 只不过如果在截止时间之前没完成的任务都会被取消，不再执行
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                  long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 执行给定的任务，直到一个任务完成或者抛出异常，其他未执行的任务将不再执行，返回被执行完的任务的结果
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException;

    /**
     * 和 invokeAny(Collection<? extends Callable<T>> tasks) 类似，加上了超时机制
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
```
## `AbstractExecutorService` 抽象类
　　`AbstractExecutorService` 抽象类实现了 `ExecutorService` 接口，对一些方法实现了默认实现。
``` Java
public abstract class AbstractExecutorService implements ExecutorService {

    /**
     * 根据传进来的 Runnable 和 value 构造一个 RunnableFuture
     * RunnableFuture 是继承了 Runnable 和 Future 接口的接口
     */
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new FutureTask<T>(runnable, value);
    }

    /**
     * 根据 Runnable 构建 RunnableFuture
     */
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTask<T>(callable);
    }

    /**
     * 提交任务，从这里可以看到，内部还是调用了 execute 方法
     */
    public Future<?> submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<Void> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }

    /**
     * 同 submit，只不过构建的是一个带有 result 的 FutureTask
     */
    public <T> Future<T> submit(Runnable task, T result) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task, result);
        execute(ftask);
        return ftask;
    }

    /**
     * 同上
     */
    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task);
        execute(ftask);
        return ftask;
    }

    /**
     * invokeAny 内部都调用这个方法
     * 三个参数，第一个表示要提交的任务，第二个表示是否是有时间限制的，第三个表示时间
     */
    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks,
                              boolean timed, long nanos)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks == null)
            throw new NullPointerException();
        int ntasks = tasks.size();
        if (ntasks == 0)
            throw new IllegalArgumentException();
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(ntasks);
        
        // 将自己本身作为参数传入 ExecutorCompletionService 的构造函数
        // 在这个类内部只用了 AbstractExecutorService 的 submit 方法
        ExecutorCompletionService<T> ecs = new ExecutorCompletionService<T>(this);

        try {
            // 记录异常，如果最终没有获得任何结果，就抛出这个异常
            ExecutionException ee = null;
            // 记录终止操作的时间
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Iterator<? extends Callable<T>> it = tasks.iterator();

            // 提交一个任务，其他的等下看情况再提交
            futures.add(ecs.submit(it.next()));
            --ntasks;
            int active = 1;

            for (;;) {
                Future<T> f = ecs.poll(); // 从队列中出队一个任务的结果
                if (f == null) { // 如果没有结果，那么就继续从提交的任务中选取下一个执行
                    if (ntasks > 0) {
                        --ntasks;
                        futures.add(ecs.submit(it.next()));
                        ++active;
                    }
                    else if (active == 0)
                        break;
                    else if (timed) {
                        f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
                        if (f == null)
                            throw new TimeoutException();
                        nanos = deadline - System.nanoTime();
                    }
                    else
                        f = ecs.take();
                }
                if (f != null) { // 如果已经有任务执行完毕，那么就返回结果
                    --active;
                    try {
                        return f.get();
                    } catch (ExecutionException eex) {
                        ee = eex;
                    } catch (RuntimeException rex) {
                        ee = new ExecutionException(rex);
                    }
                }
            }
            // 执行到这里还没有返回，那么就抛出异常
            if (ee == null)
                ee = new ExecutionException();
            throw ee;

        } finally { // 最后将没有执行的任务取消
            for (int i = 0, size = futures.size(); i < size; i++)
                futures.get(i).cancel(true);
        }
    }

    // 其他的 invokeAny 都是基于 doInvokeAny 的，就不贴源码了
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        // code......
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        // code......
    }

    /**
     * 执行所有任务，直到所有任务完成或者出现异常才返回
     */
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException {
        if (tasks == null)
            throw new NullPointerException();
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false;
        try {
            // 执行所有的任务
            for (Callable<T> t : tasks) {
                RunnableFuture<T> f = newTaskFor(t);
                futures.add(f);
                execute(f);
            }
            for (int i = 0, size = futures.size(); i < size; i++) {
                Future<T> f = futures.get(i);
                if (!f.isDone()) { // 如果任务还没有完成，就等待任务完成
                    try {
                        f.get();
                    } catch (CancellationException ignore) {
                    } catch (ExecutionException ignore) {
                    }
                }
            }
            done = true;
            return futures;
        } finally { // 如果发生了异常，取消没有执行的任务
            if (!done)
                for (int i = 0, size = futures.size(); i < size; i++)
                    futures.get(i).cancel(true);
        }
    }

    /**
     * 和一般的 invokeAll 类似，但是加上了判断是否在指定时间内执行完毕
     * 只要到指定时间，不管任务执行完没有，都直接返回结果
     */
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                         long timeout, TimeUnit unit)
        throws InterruptedException {
        if (tasks == null)
            throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false;
        try {
            for (Callable<T> t : tasks)
                futures.add(newTaskFor(t));

            final long deadline = System.nanoTime() + nanos;
            final int size = futures.size();

            // Interleave time checks and calls to execute in case
            // executor doesn't have any/much parallelism.
            for (int i = 0; i < size; i++) {
                execute((Runnable)futures.get(i));
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L)
                    return futures;
            }

            for (int i = 0; i < size; i++) {
                Future<T> f = futures.get(i);
                if (!f.isDone()) {
                    if (nanos <= 0L)
                        return futures;
                    try {
                        f.get(nanos, TimeUnit.NANOSECONDS);
                    } catch (CancellationException ignore) {
                    } catch (ExecutionException ignore) {
                    } catch (TimeoutException toe) {
                        return futures;
                    }
                    nanos = deadline - System.nanoTime();
                }
            }
            done = true;
            return futures;
        } finally { // 如果最后有任务没有执行完，取消没执行的任务
            if (!done)
                for (int i = 0, size = futures.size(); i < size; i++)
                    futures.get(i).cancel(true);
        }
    }

}
```
## `ThreadPoolExecutor` 线程池
　　`ThreadPoolExecutor` 继承了 `AbstractExecutorService`，实现了核心方法 `execute` 以及一些获取线程池信息的方法。

　　`ThreadPoolExecutor` 有一些重要的参数：
``` Java
// ctl存储了线程状态以及当前线程池的线程数量
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
private static final int COUNT_BITS = Integer.SIZE - 3;
private static final int CAPACITY   = (1 << COUNT_BITS) - 1;// 最多可容纳 2^29 个线程
// 运行时状态存储在高字节位
private static final int RUNNING    = -1 << COUNT_BITS;
private static final int SHUTDOWN   =  0 << COUNT_BITS;
private static final int STOP       =  1 << COUNT_BITS;
private static final int TIDYING    =  2 << COUNT_BITS;
private static final int TERMINATED =  3 << COUNT_BITS;

private final ReentrantLock mainLock = new ReentrantLock(); // 对线程池进行操作的时候的锁
private final HashSet<Worker> workers = new HashSet<Worker>(); // 存放工作线程
private final Condition termination = mainLock.newCondition(); // 支持等待终止的等待条件
private int largestPoolSize; // 记录线程池曾经出现过的最大大小，只记录，和容量没有关系
private long completedTaskCount; // 已经完成的任务数
private volatile boolean allowCoreThreadTimeOut; // 是否允许核心池设置存活时间

// 下面的参数是线程池的核心参数
private final BlockingQueue<Runnable> workQueue; // 存放等待被执行的任务的队列
private volatile ThreadFactory threadFactory; // 用来创建线程的线程工厂
private volatile RejectedExecutionHandler handler; // 任务拒绝策略
private volatile long keepAliveTime; // 线程存活时间
private volatile int corePoolSize; // 核心池大小
private volatile int maximumPoolSize; // 线程池的最大线程数
```
　　上面有几个参数是线程池的核心参数，在构造函数中不一定需要传入所有的值，但是 `ThreadPoolExecutor` 的构造函数最终都调用了下面这个构造函数：
``` Java
public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException(); // 先进行参数检验
        if (workQueue == null || threadFactory == null || handler == null) // 判断空指针
            throw new NullPointerException();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }
```
　　下面来逐一解释一下各个参数的含义。
### `int corePoolSize`
　　核心池大小，一个线程池是同时在执行很多任务的，核心池就是正在执行的任务池。
### `int maximumPoolSize`
　　线程池的最大线程数，当核心池满了以后，新添加的任务就会放到等待队列中，如果等待队列满了，线程池就想快点执行任务，腾出位置给新加进来的线程，如果当前工作线程数小于 `maximumPoolSize`，那么就创建新的线程来执行刚加进来的任务，可以认为是线程池负荷过重，创建新的线程来减轻压力。
### `long keepAliveTime`
　　线程存活时间，如果一个线程处在空闲状态的时间超过了这个值，就会因为超时而退出。
### `BlockingQueue<Runnable> workQueue`
　　`workQueue` 是一个阻塞队列，用来存放等待被执行的任务的队列。如果核心池满了，就把等待执行的线程放到这里。
### `ThreadFactory threadFactory`
　　用来创建线程的线程工厂。
### `RejectedExecutionHandler handler`
　　任务拒绝策略。`ThreadPoolExecutor` 中有四个实现了 `RejectedExecutionHandler` 接口的内部类，分别是：

- ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。 
- ThreadPoolExecutor.DiscardPolicy：内部什么也没有做，也就是丢弃任务，不抛出异常。 
- ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后提交当前插入的任务。
- ThreadPoolExecutor.CallerRunsPolicy：调用策略的调用者直接在内部执行了任务的 `run` 方法。
### `execute` 方法
　　线程池的核心方法是 `execute` 方法，来看这个方法做了什么：
``` Java
public void execute(Runnable command) {
    if (command == null) // 先判断输入参数的合法性
        throw new NullPointerException();
    /*
     * Proceed in 3 steps:
     *
     * 1. If fewer than corePoolSize threads are running, try to
     * start a new thread with the given command as its first
     * task.  The call to addWorker atomically checks runState and
     * workerCount, and so prevents false alarms that would add
     * threads when it shouldn't, by returning false.
     *
     * 2. If a task can be successfully queued, then we still need
     * to double-check whether we should have added a thread
     * (because existing ones died since last checking) or that
     * the pool shut down since entry into this method. So we
     * recheck state and if necessary roll back the enqueuing if
     * stopped, or start a new thread if there are none.
     *
     * 3. If we cannot queue task, then we try to add a new
     * thread.  If it fails, we know we are shut down or saturated
     * and so reject the task.
     *
     * 上面是 JDK 自带的注释，来翻译一下：
     * 1. 如果核心池没有满，尝试执行当前任务， addWorker 原子性地检查线程池状态和线程数量，
     * 通过返回 false 防止不应该加入线程却加入了线程这样的错误警告
     * 2. 如果一个线程能够成功插入队列，我们还应该二次检查我们是否已经加了一个线程
     * （因为可能有线程在上次检查过后死掉了）或者进到这个方法以后线程池关闭了。
     * 因此我们再次检查线程池状态，如果线程池已经关闭了我们有必要回滚进队操作，
     * 或者如果没有，就启动新线程
     * 3. 如果我们不能把线程插入队列，那么我们尝试添加一个新线程，
     * 如果失败了，那就是线程池饱和了或者关闭了，按照之前的拒绝策略拒绝任务。
     */
    int c = ctl.get(); // 获取当前线程池状态信息
    // 如果当前核心池没有满，就执行当前任务
    if (workerCountOf(c) < corePoolSize) { 
        if (addWorker(command, true)) // 如果执行成功直接返回
            return;
        c = ctl.get(); // 没执行成功，再次获取线程池状态
    }
    // 如果线程池正在运行并且成功加入到等待队列中
    if (isRunning(c) && workQueue.offer(command)) { 
        int recheck = ctl.get(); // 再次检查线程池状态，因为线程池在上次检查之后可能关闭了
        // 如果线程池已经关闭并且成功从等待队列中移除刚插入的任务，拒绝任务
        if (! isRunning(recheck) && remove(command)) 
            reject(command);
        else if (workerCountOf(recheck) == 0)
            addWorker(null, false);
    }
    // 如果添加到等待队列失败，拒绝任务
    else if (!addWorker(command, false))
        reject(command);
}
```
　　
