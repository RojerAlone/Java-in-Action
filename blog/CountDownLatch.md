# CountDownLatch

`CountDownLatch` 直译过来为“倒数阀门”，顾名思义，它是起到一个阀门的作用，实际上是用来控制线程的执行过程。

来看一下这个类提供了哪些方法：
![CountDownLatch-Outline](http://i.imgur.com/eZZ5prk.png)

从图中可以看出，`CountDownLatch` 的构造方法是传入一个整数，然后提供了 `await()`、`countDown()`、`getCount()` 等方法，还有一个内部类 `Sync`，这个内部类继承了 `AbstractQueuedSynchronizer`。

整个 `CountDownLatch` 的核心是 `Sync` 类，先来看这个类。
## Sync
``` Java
private static final class Sync extends AbstractQueuedSynchronizer { // 继承了 AQS 类
    private static final long serialVersionUID = 4982264981922014374L;

    Sync(int count) { // 构造函数，将 AQS 中的 state 设置为 count 的值
        setState(count);
    }

    int getCount() { // 获取 state 的值
        return getState();
    }
    /**
     * 重写 AQS 中的 tryAcquireShared
     * 尝试获取共享锁，如果当前 state 等于 0 返回 1 表示获取成功，否则返回 -1 表示失败
     */
    protected int tryAcquireShared(int acquires) {
        return (getState() == 0) ? 1 : -1;
    }
    /**
     * 重写 AQS 中的 tryReleaseShared
     */
    protected boolean tryReleaseShared(int releases) {
        // Decrement count; signal when transition to zero
        for (;;) { // 死循环，保证 cas 更改成功
            int c = getState();
            if (c == 0) // 如果当前 state 为 0，释放失败
                return false;
            int nextc = c-1; // state - 1
            if (compareAndSetState(c, nextc)) // 更改最新的 state 值
                return nextc == 0; // 如果 state - 1 后为 0 返回 true，否则返回 false
        }
    }
}
```
`Sync` 类重写了 AQS 中的 `tryAcquireShared` 和 `tryReleaseShared` 方法，`tryAcquireShared` 判断当前的 state 是否为 0，`tryReleaseShared` 中对 state 进行减一操作，如果减过之后 state 为 0 则返回 true。

`CountDownLatch` 中的方法都是调用了 `Sync` 中的方法，接下来看一下 `CountDownLatch` 的实现。
## CountDownLatch(int)
``` Java
// 构造方法，初始化 sync
public CountDownLatch(int count) {
    // 如果 count 小于 0 就抛出非法参数异常
    if (count < 0) throw new IllegalArgumentException("count < 0");
    this.sync = new Sync(count);
}
```
## `await`
`await` 有两种，一种是没有时间限制的，一种是有时间限制的。
``` Java
/**
 * 调用 AQS 的 acquireSharedInterruptibly
 * 内部还是调用了 tryAcquireShared 方法，也就是判断当前 state 是否为 0
 * 如果不为 0 那么当前线程加入等待队列，被阻塞
 */
public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(1); 
}
/**
 * 调用 AQS 的 tryAcquireSharedNanos
 * 在指定时间内没有获取锁，返回 false
 */
public boolean await(long timeout, TimeUnit unit)
    throws InterruptedException {
    return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
}
```
## `countDown`
``` Java
/**
 * 调用 AQS 的 releaseShared
 * releaseShared 内部调用了 Sync 中重写的 tryReleaseShared 方法，将 state - 1
 * 如果 state - 1 后为 0 ，返回 true，那么就会唤醒等待的线程
 */
public void countDown() {
    sync.releaseShared(1);
}
```
## 总结
根据对 `AbstractQueuedSynchronizer` 的了解，加上看完上面的源码，我们已经清楚地了解了 `CountDownLatch` 的作用以及原理：

- `CountDownLatch` 中的内部类 `Sync` 继承了 AQS，并重写了 `tryAcquireShared` 和 `tryReleaseShared` 方法
- `CountDownLatch` 实现了对 AQS 共享锁相关方法的封装，相当于换了个直观的名字
- 执行 `await` 方法判断 state，相当于计数器，是否为 0，如果不为 0，当前线程执行到这里以后阻塞，直到 state 为 0
- 执行 `countDown` 方法对 state 进行减一操作，如果减一后为 0 ，那么唤醒等待的线程

简单归纳一下就是，`CountDownLatch` 是阀门，初始化时设置的 count 表示容量，线程执行 `await` 方法等待阀门内没有东西（state == 0），如果阀门内有东西（state > 0），那么执行 `await` 的方法被阻塞，直到阀门内没有东西 （state == 0）才被唤醒。每当一个线程执行一次 `countDown` 方法，阀门内的东西就减少一个 （state = state - 1），如果减少一个以后阀门内没有东西了，就唤醒等待阀门为空的线程。

更生动一点来说，打个比方，有一个公共澡堂，澡堂内有一定数量的淋浴头（count），这些淋浴头都已经有人在使用了，每当有人想要洗澡的时候，这些人都要等待所有的洗澡的人都已经洗完了（state == 0），因此他们要在外面等待（`await`），每当有一个人洗完了（`countDown`），都会看一下是不是所有人都洗完了，如果所有人都洗完了，那么给外面等待的人说，你们可以进来了，这时候等待的人才能进来（也就是线程被唤醒，继续执行）。
## 应用 Demo
``` Java
import java.util.concurrent.CountDownLatch;

public class CountDownLatchTest {

    private static final int numOfThreads = 10; // 线程数

    private static final int sleepTime = 3000; // 睡眠时间

    public static void main(String[] args) {
        CountDownLatch startLatch = new CountDownLatch(1); // 只有执行 start.countDown() 之后线程才开始执行
        CountDownLatch threadLatch = new CountDownLatch(numOfThreads);

        for (int i = 0; i < numOfThreads; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await(); // 等待 startLatch.countDown 才开始执行
                        Thread.sleep(sleepTime);
                        System.out.println(Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    threadLatch.countDown(); // 完成的线程数 -1
                }
            }).start();
        }

        startLatch.countDown(); // 所有线程开始执行
        long start = System.currentTimeMillis();
        try {
            threadLatch.await(); // 等待所有线程执行完毕
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(numOfThreads + " 个线程执行完花费时间为 : " + (System.currentTimeMillis() - start) + " ms");
    }

}
```
执行结果为：
```
Thread-8
Thread-9
Thread-5
Thread-3
Thread-1
Thread-0
Thread-2
Thread-4
Thread-7
Thread-6
10 个线程执行完花费时间为 : 3003 ms
```