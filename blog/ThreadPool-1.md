# ThreadPool 之线程池概览
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
　　由于贴的代码太多，如果所有内容都写到一篇文章里太过冗长，因此分开写了，下篇将研究线程池的最终实现类 `ThreadPoolExecutor`。