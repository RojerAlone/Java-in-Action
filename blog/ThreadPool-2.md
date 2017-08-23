# ThreadPool 之 线程池实现类 `ThreadPoolExecutor`
## `ThreadPoolExecutor` 线程池
　　`ThreadPoolExecutor` 继承了 `AbstractExecutorService`，实现了核心方法 `execute` 以及一些获取线程池信息的方法。
### `ThreadPoolExecutor` 参数
　　`ThreadPoolExecutor` 有一些重要的参数：
``` Java
// ctl存储了线程状态以及当前线程池的线程数量
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
private static final int COUNT_BITS = Integer.SIZE - 3;
private static final int CAPACITY   = (1 << COUNT_BITS) - 1;// 最多可容纳 2^29 - 1 个线程
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
#### `int corePoolSize`
　　核心池大小，一个线程池是同时在执行很多任务的，核心池就是正在执行的任务池。
#### `int maximumPoolSize`
　　线程池的最大线程数，当核心池满了以后，新添加的任务就会放到等待队列中，如果等待队列满了，线程池就想快点执行任务，腾出位置给新加进来的线程，如果当前工作线程数小于 `maximumPoolSize`，那么就创建新的线程来执行刚加进来的任务，可以认为是线程池负荷过重，创建新的线程来减轻压力。
#### `long keepAliveTime`
　　线程存活时间，如果一个线程处在空闲状态的时间超过了这个值，就会因为超时而退出。
#### `BlockingQueue<Runnable> workQueue`
　　`workQueue` 是一个阻塞队列，用来存放等待被执行的任务的队列。如果核心池满了，就把等待执行的线程放到这里。
#### `ThreadFactory threadFactory`
　　用来创建线程的线程工厂。
#### `RejectedExecutionHandler handler`
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
　　源码中出现了多次 `addWorker` 操作，继续查看 `addWorker` 的源码。
### `addWorker`
``` Java
private boolean addWorker(Runnable firstTask, boolean core) {
    retry:
    for (;;) {
        int c = ctl.get(); // 获取线程池执行状态
        int rs = runStateOf(c);
        // Check if queue empty only if necessary.
        // 如果线程池不是正常运行状态，如果出现以下3种情况之一的，就返回 false ：
        // 1. 线程池不是关闭状态
        // 2. 线程池关闭了，但是传入的任务非空
        // 3. 线程池关闭了，传入的线程非空但是没有任务正在执行
        if (rs >= SHUTDOWN &&
            ! (rs == SHUTDOWN &&
               firstTask == null &&
               ! workQueue.isEmpty()))
            return false;
        // 如果线程池一切正常，那么执行以下逻辑
        for (;;) {
            int wc = workerCountOf(c); // 获取当前线程池中线程数量
            // 如果线程池已满，返回 false
            if (wc >= CAPACITY ||
                wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
            // 如果线程池没满，线程安全地增加线程数量，增加成功就退出循环
            if (compareAndIncrementWorkerCount(c))
                break retry;
            // 添加失败的话就再获取状态，如果线程池状态和之前获取的状态不一致，继续循环
            c = ctl.get();  // Re-read ctl
            if (runStateOf(c) != rs)
                continue retry;
            // else CAS failed due to workerCount change; retry inner loop
        }
    }
    // 添加任务成功之后才会执行到这里
    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    try {
        w = new Worker(firstTask);
        final Thread t = w.thread;
        if (t != null) {
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                // Recheck while holding lock.
                // Back out on ThreadFactory failure or if
                // shut down before lock acquired.
                // 当拿到锁以后再次检查状态
                // 如果 ThreadFactory 失败或者获取锁过程中线程池关闭，就退出
                int rs = runStateOf(ctl.get());
                // 如果线程池状态正常或者线程池关闭了同时任务为空
                if (rs < SHUTDOWN ||
                    (rs == SHUTDOWN && firstTask == null)) {
                    // 如果线程已经启动，就抛出异常
                    if (t.isAlive()) // precheck that t is startable
                        throw new IllegalThreadStateException();
                    workers.add(w); // 将 worker 加入到工作线程队列中
                    int s = workers.size();
                    if (s > largestPoolSize) // 记录线程池达到过的最大容量
                        largestPoolSize = s;
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            if (workerAdded) { // 如果任务添加成功，就开始执行任务
                t.start(); // 启动线程，也就是 worker，worker 会不断从等待队列中获取任务并执行
                workerStarted = true;
            }
        }
    } finally {
        if (! workerStarted)
            addWorkerFailed(w);
    }
    return workerStarted;
}
```
　　在 `addWorker` 方法中将任务封装成了一个 `Worker` 类，执行任务的时候执行的线程是从 `Worker` 类中获取的线程，`Worker` 是线程池的一个内部类，查看它的源码。
### `Worker` 类
``` Java
private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable
    {
        /**
         * This class will never be serialized, but we provide a
         * serialVersionUID to suppress a javac warning.
         * 为了抑制 javac 警告添加了序列化ID
         */
        private static final long serialVersionUID = 6138294804551838833L;

        /** Thread this worker is running in.  Null if factory fails. */
        // worker 运行的线程，如果 ThreadFactory 生成失败的话这个值为 null
        final Thread thread;
        /** Initial task to run.  Possibly null. */
        // 运行的初始任务，可能为 null
        Runnable firstTask;
        /** Per-thread task counter */
        // 记录总共执行过的任务
        volatile long completedTasks;

        /**
         * 用传进来的参数作为第一个任务，用 ThreadFactory 创建线程
         */
        Worker(Runnable firstTask) {
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }

        /**
         * 将运行任务交给其他的方法执行
         * Worker 作为一个实现了 Runnable 接口的类，要实现 run 方法，
         * 线程启动的时候调用的是 start 方法，start 方法内部调用 run 方法，
         * 所以实际运行时候执行的是这个 run 方法
         */
        public void run() {
            runWorker(this);
        }

        /**
         * Worker 继承了 AbstractQueuedSynchronizer，下面就是需要重写的一些必要的方法
         */

        // Lock methods
        //
        // The value 0 represents the unlocked state.
        // The value 1 represents the locked state.
        // 是否是独占锁
        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }
```
　　从 `Worker` 中可以看出，它继承了 `AbstractQueuedSynchronizer`，方便加锁解锁，并且实现了 `Runnable` 接口，本身作为一个线程运行。

　　这里就是线程池为什么任务执行之后线程没有销毁，提交到线程池的线程不是调用了线程的 `start` 方法，而是被 `Worker` 中的 `run` 方法调用，`run` 方法内部等下再看。`Worker` 中的属性 `thread` 是将 `Worker` 本身封装成为了一个 `Thread`，然后启动线程，虽然执行的是 `run` 方法而不是我们所熟知的 `start` 方法启动线程，但是任务的 `run` 方法被 `Worker` 的 `run` 方法调用，`Worker` 的 `run` 方法又是被 `start` 方法所启动，因此实现了线程的交互运行。

　　接下来看一下 `runWorker` 方法，这个方法是线程池如何不销毁线程而不断执行任务的。
### `runWorker`
　　`runWorker` 实际上是线程池 `ThreadPoolExecutor` 中的方法而不是 `Worker` 中的：
``` Java
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask; // 获取 Worker 中当前要执行的任务
    w.firstTask = null; // 已经拿到了任务，将 Worker 中的任务置为 null
    w.unlock(); // allow interrupts
    boolean completedAbruptly = true;
    try {
        // 如果任务不为空或者任务为空但是从队列中获取到了任务，就执行任务
        while (task != null || (task = getTask()) != null) { 
            w.lock();
            // If pool is stopping, ensure thread is interrupted;
            // if not, ensure thread is not interrupted.  This
            // requires a recheck in second case to deal with
            // shutdownNow race while clearing interrupt
            // 如果线程池停止了，确保线程被中断了，如果线程池正在运行，确保线程没有被中断
            if ((runStateAtLeast(ctl.get(), STOP) ||
                 (Thread.interrupted() &&
                  runStateAtLeast(ctl.get(), STOP))) &&
                !wt.isInterrupted())
                wt.interrupt();
            try {
                beforeExecute(wt, task); // 开始执行任务之前应该做的，本身什么都不做，可以子类重写
                Throwable thrown = null;
                try {
                    task.run(); // 执行任务的 run 方法，这里才真正执行了任务
                } catch (RuntimeException x) {
                    thrown = x; throw x;
                } catch (Error x) {
                    thrown = x; throw x;
                } catch (Throwable x) {
                    thrown = x; throw new Error(x);
                } finally {
                    afterExecute(task, thrown); // 同 beforeExecute
                }
            } finally { // 将要执行的任务置为空，已完成任务 +1，释放锁
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
        completedAbruptly = false; // 标记是否是正常完成，如果出现异常是不会执行这一步的，直接执行 finally
    } finally {
        // 结束线程，在之前提到的核心池满了等待队列也满了会创建临时线程执行任务，执行完销毁
        // 或者线程池停止了也要结束工作线程
        processWorkerExit(w, completedAbruptly);
    }
}
```
　　可以看出 `runWorker` 方法中真正执行了任务，然后不停从等待队列中获取新的任务继续执行。

　　下面看一下是怎么从等待队列中获取任务的。
### `getTask`
``` Java
private Runnable getTask() {
    boolean timedOut = false; // Did the last poll() time out?

    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // Check if queue empty only if necessary.
        // 线程池已经不在运行而且线程池被停止或者等待队列为空，将工作线程数减 1
        // 因为 getTask 是被一个工作线程调用的，如果返回 null，调用 getTask 方法的 Worker 就结束运行
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;
        }
        // 如果线程池一切正常，继续下面的逻辑
        int wc = workerCountOf(c);

        // Are workers subject to culling?
        // 如果创建线程池时候设置了超时或者当前启用了超出核心池的线程“加班”执行任务，timed 为 true
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
        // 如果当前请求任务的是超出核心池大小的线程或者已经超时，同时工作线程数大于 1 或者等待队列为空
        if ((wc > maximumPoolSize || (timed && timedOut))
            && (wc > 1 || workQueue.isEmpty())) {
            if (compareAndDecrementWorkerCount(c)) //  尝试将工作线程数减 1，如果成功返回 null，否则继续执行
                return null;
            continue;
        }

        try {
            // 如果超时的话从等待队列中获取任务，如果一段时间内没有任务就返回null
            // 否则阻塞地从等待队列中获取任务，一直到有任务返回才继续执行下一步，也就是一定会返回一个任务
            Runnable r = timed ?
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                workQueue.take();
            if (r != null)
                return r;
            timedOut = true;
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```
### 关闭线程池
　　`ExecutorService` 提供了两种关闭线程池的方式，`shutdown()` 和 `shutdownNow()`。
#### `shutdown()`
``` Java
public void shutdown() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock(); // 加锁
    try {
        checkShutdownAccess(); // 检查调用者是否有权关闭线程
        advanceRunState(SHUTDOWN); // 修改线程池的运行状态
        interruptIdleWorkers(); // 中断所有的空闲 worker
        onShutdown(); // ScheduledThreadPoolExecutor 的钩子，ThreadPoolExecutor 内部不做任何处理
    } finally {
        mainLock.unlock();
    }
    tryTerminate(); // 将线程池状态改为 TERMINATED
}
```
#### `shutdownNow()`
``` Java
public List<Runnable> shutdownNow() {
    List<Runnable> tasks;
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        advanceRunState(STOP); // 修改线程池的运行状态
        interruptWorkers(); // 中断所有的 worker，不管有没有在工作
        tasks = drainQueue(); // 移出所有的等待任务，最后返回
    } finally {
        mainLock.unlock();
    }
    tryTerminate();
    return tasks;
}
```
　　可以看到，`shutdown()` 只是将空闲的 worker 关闭，然后修改线程池状态为关闭。如果 worker 正在执行任务，当前任务执行完之后发现线程池已经关闭，就会结束。

　　`shutdownNow()` 是强制中止所有的正在执行的任务，然后返回待执行的任务。
### 线程池原理小结
　　看了 `execute` 、`addWorker`、`Worker` 类、`runWorker` 的源码，可以清楚地了解线程池的原理：

　　每当有任务被提交（`execute` 方法），如果核心池没有满，就创建一个 `Worker` （也就是 `addWorker` 方法），创建后 `worker` 开始工作（在 `addWorker` 方法中调用启动 `Worker` 中的 `thread`，也就是执行了 `runWorker` 方法），`runWorker` 方法会不停地从等待队列 `workQueue` 中获取任务并执行（之前说过，执行任务的时候执行的是任务的 `run` 方法，但是 `worker` 是一个线程，所以相当于 `Thread` 的 `start` 方法间接调用了任务的`run` 方法）。

　　如果核心池满了，并且等待队列也满了，而且核心池大小小于线程池最大大小，就会进行挽救措施：创建新的 `Worker` 来执行新提交的任务，这个新的 `Worker` 执行完任务以后就会销毁。

　　如果核心池满了，等待队列也满了，核心池大小也等于最大线程池大小，那就只能拒绝任务了，根据构造函数中传入的拒绝策略拒绝任务。