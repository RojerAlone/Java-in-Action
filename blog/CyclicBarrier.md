# CyclicBarrier
字面意思“循环屏障”，翻译为“可重用的屏障”更贴切一点。这个工具类的作用是起到一个栅栏的作用，当指定数量的线程都执行到某一个步骤之后，再一起往下执行，相当于一个阀门，容量满了再开阀门。

看一下这个类的结构：

![CyclicBarrier-Outline](http://i.imgur.com/rvCrXyH.png)

这个类没有继承 `AbstractQueuedSynchronizer`，内部类也没有继承，而是使用了 `ReentrantLock lock` 这样一个变量来控制线程同步，内部类 `Generation` 只有一个参数 `broken`，默认值为 `false`，用来控制屏障的循环使用。
``` Java
private static class Generation {
    boolean broken = false;
}
```
## 参数
下面来看 `CyclicBarrier` 的相关参数：
``` Java
// 控制屏障的锁
private final ReentrantLock lock = new ReentrantLock();
// 控制“突破”屏障的条件，突破屏障后执行 trip.signAll()
private final Condition trip = lock.newCondition();
// 拦截线程的数量，也就是屏障被“突破”的上限
private final int parties;
// “突破”屏障以后要执行的内容
private final Runnable barrierCommand;
// 当前是否在使用屏障
private Generation generation = new Generation();
// 当前还需要多少个线程才能“突破”屏障
private int count;
```
## 构造方法
``` Java
public CyclicBarrier(int parties) {
    this(parties, null); // 调用了另一个构造方法
}

// 对没有初始化的参数赋值
public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException(); // 参数检验
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
}
```
构造方法就是对没有进行初始化的参数进行了赋值。
## await
核心方法为 `await` 方法，来看代码：
``` Java
public int await() throws InterruptedException, BrokenBarrierException {
    try {
        return dowait(false, 0L); // 不限时退出
    } catch (TimeoutException toe) {
        throw new Error(toe); // cannot happen
    }
}
    
public int await(long timeout, TimeUnit unit)
        throws InterruptedException,
               BrokenBarrierException,
               TimeoutException {
    return dowait(true, unit.toNanos(timeout)); // 有限时
}
```
## dowait
`await` 方法中调用了 `dowait` 方法，这个才是真正的控制屏障的核心方法：
``` Java
private int dowait(boolean timed, long nanos)
    throws InterruptedException, BrokenBarrierException,
           TimeoutException {
    final ReentrantLock lock = this.lock;
    lock.lock(); // 先加锁
    try {
        final Generation g = generation; // 获取当前的分代信息

        // 当屏障已经被“突破”了，抛出异常
        // 也就是一个线程在屏障已经“被突破”之后执行 await 方法会抛出异常
        if (g.broken) 
            throw new BrokenBarrierException();
        // 当线程被中断，破坏屏障，放行所有线程，并且抛出中断异常
        if (Thread.interrupted()) {
            breakBarrier();
            throw new InterruptedException();
        }

        int index = --count; // 有一个线程执行 await，count - 1
        // 所有等待的线程都已经执行到了这里，执行传进来的 runnable 任务
        if (index == 0) {  // tripped 
            boolean ranAction = false;
            try {
                final Runnable command = barrierCommand;
                if (command != null)
                    command.run();
                ranAction = true;
                // 下一代，在这里实现了重用，唤醒当前等待在屏障处的线程，
                // 将 generation 改为一个新的 Generation，
                // count 重新设置为 parties
                nextGeneration();
                return 0; // 返回 0 表示不需要线程来一起“突破”屏障
            } finally {
                // 如果传进来的 runnable 任务执行出错的话，破坏屏障
                if (!ranAction)
                    breakBarrier();
            }
        }

        // 循环直到“突破”屏障、抛出异常、中断或者时间超时
        for (;;) {
            try {
                if (!timed) // 如果不设置超时，等待 trip 执行 signAll
                    trip.await();
                else if (nanos > 0L)
                    nanos = trip.awaitNanos(nanos);
            } catch (InterruptedException ie) {
                if (g == generation && ! g.broken) {
                    breakBarrier();
                    throw ie;
                } else {
                    Thread.currentThread().interrupt();
                }
            }
            // 当前线程被唤醒之后，如果 generation 的 broken 为 true，抛出异常
            if (g.broken)
                throw new BrokenBarrierException();
            // 如果 generation 已经是新的了，返回还需要多少个线程才能“突破”屏障
            if (g != generation)
                return index;
            // 如果已经超时，破坏屏障并且唤醒所有线程，抛出异常
            if (timed && nanos <= 0L) {
                breakBarrier();
                throw new TimeoutException();
            }
        }
    } finally { // 最后释放锁
        lock.unlock();
    }
}
```
其他的一些方法是获取信息或者辅助性的，这里就不贴出来了。
## 总结
`CyclicBarrier` 的作用是阻塞指定数量的线程，直到指定数量的线程执行了 `await` 方法后才唤醒所有线程，继续往下执行，并且可以重用。
## 应用
``` Java
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierTest {

    private static final int numOfThreads = 5; // 线程数

    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(numOfThreads);
        for (int i = 0; i < numOfThreads; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " ---> 已经到了");
                    try {
                        System.out.println(Thread.currentThread().getName() + " ---> 等待所有线程都到");
                        cyclicBarrier.await(); // 等待所有的线程都执行完这一步
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + " ---> 突破屏障!");
                }
            }).start();
        }
    }

}
```
输出结果为：
```
Thread-0 ---> 已经到了
Thread-3 ---> 已经到了
Thread-4 ---> 已经到了
Thread-4 ---> 等待所有线程都到
Thread-2 ---> 已经到了
Thread-3 ---> 等待所有线程都到
Thread-1 ---> 已经到了
Thread-0 ---> 等待所有线程都到
Thread-1 ---> 等待所有线程都到
Thread-2 ---> 等待所有线程都到
Thread-2 ---> 突破屏障!
Thread-3 ---> 突破屏障!
Thread-4 ---> 突破屏障!
Thread-1 ---> 突破屏障!
Thread-0 ---> 突破屏障!
```
## 和 `CountDownLatch` 对比
- `CountDownLatch` 使用了内部类 `Sync` 进行控制线程，`CyclicBarrier` 使用了 `ReentrantLock` 和 `Condition` 来控制
- `CyclicBarrier` 可以重用，并且只需要调用 `await`，`CountDownLatch` 需要一个或多个线程执行 `await`，被等待的线程执行 `countDown`
- `CyclicBarrier` 是控制一组线程，而 `CountDownLatch` 是一组线程等待另一组线程


