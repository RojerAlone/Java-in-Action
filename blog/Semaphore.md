# Semaphore
`Semaphore` 是 Java 并发包中提供的一个工具类，翻译过来为“信号量”，作用是控制并发线程的数量。
## 类的结构
先来看一下 `Semaphore` 的结构：
![Semaphore-Outline](http://i.imgur.com/vfIjIXN.png)

`Semaphore` 中有三个个内部类：

- 类 `Sync` 继承了 `AbstractQueuedSynchronizer`，重写了 `tryReleaseShared` 方法，还有一些在 `Semaphore` 中用到的辅助方法，都是对线程进行控制的
- 类 `NonfairSync` 继承了 `Sync`，通过重写 `tryAcquireShared` 方法实现了非公平的线程竞争机制，这个方法内部是调用了 `Sync` 的 `nonfairTryAcquireShared` 方法
- 类 `FairSync` 继承了 `Sync`，也是通过重写 `tryAcquireShared` 方法实现了公平的线程竞争机制

由此可以看出，`Semaphore` 实际上是通过 AQS 的共享锁来控制线程的。
## 构造方法
`Semaphore` 的构造函数对变量 sync 进行了初始化，默认是非公平竞争的，也可以通过指定参数设置为公平竞争，其他所有的方法内部都是调用了 sync 变量的方法。
``` Java
public Semaphore(int permits) {
    sync = new NonfairSync(permits);
}

public Semaphore(int permits, boolean fair) {
    sync = fair ? new FairSync(permits) : new NonfairSync(permits);
}
```
## acquire
`acquire` 用来获取信号量，`Semaphore` 提供了重载方法，也可以获取多个信号量。
``` Java
public void acquire() throws InterruptedException { // 获取 1 个信号量
    sync.acquireSharedInterruptibly(1);
}
    
public void acquire(int permits) throws InterruptedException {
    if (permits < 0) throw new IllegalArgumentException(); // 参数判断
    sync.acquireSharedInterruptibly(permits); // 获取指定个数的信号量
}
```
`acquire` 方法内部调用了 `sync` 的 `acquireSharedInterruptibly` 方法，这里并没有对这个方法进行重写，所以调用的还是 `AbstractQueuedSynchronizer` 中的方法，这里就不贴代码了，但是 `acquireSharedInterruptibly` 方法内部又调用了 `tryAcquireShared` 方法，由于 `Semaphore` 类提供了公平和非公平两种竞争机制，所以 `tryAcquireShared` 也有两种不同的实现。

来看一下两种获取锁的方法。
### `tryAcquireShared`
先来看一下非公平的 `tryAcquireShared`，这个方法是内部类 `NonfairSync` 中的：
``` Java
protected int tryAcquireShared(int acquires) {
    return nonfairTryAcquireShared(acquires);
}
```
继续看 `nonfairTryAcquireShared`，这个方法是 `Sync` 提供的：
``` Java
final int nonfairTryAcquireShared(int acquires) {
    for (;;) { // 循环直到获取成功
        int available = getState(); // 获取当前 state，在这里就是信号量
        int remaining = available - acquires; // 减去获取的信号量后剩余的信号量
        // 如果信号量小于 0（获取失败） 或者更新信号量成功，返回剩余信号量
        if (remaining < 0 ||
            compareAndSetState(available, remaining))
            return remaining;
    }
}
```
再看公平的 `tryAcquireShared`，这个方法是内部类 `FairSync` 中的：
``` Java
protected int tryAcquireShared(int acquires) {
    for (;;) {
        // 先判断在当前线程之前是否有线程正在 acquire，如果有返回 -1 表示获取失败
        if (hasQueuedPredecessors())
            return -1;
        int available = getState();
        int remaining = available - acquires;
        if (remaining < 0 ||
            compareAndSetState(available, remaining))
            return remaining;
    }
}
```
相对于非公平的 `nonfairTryAcquireShared`，公平的 `tryAcquireShared` 先判断在当前线程之前是否有线程正在 acquire，如果有就直接返回 -1 表示 tryAcquire 失败了，这就是公平的体现。

`acquire` 还提供了忽略中断的 `acquireUninterruptibly`，这里就不展开来说了。
## release
`release` 方法用来释放信号量，同样的，`Semaphore` 提供了重载方法，可以释放多个信号量。
``` Java
public void release() {
    sync.releaseShared(1);
}

public void release(int permits) {
    if (permits < 0) throw new IllegalArgumentException();
    sync.releaseShared(permits);
}
```
和 `acquire` 一样，`release` 方法调用了 `AbstractQueuedSynchronizer` 中的 `releaseShared` 方法，`releaseShared` 方法内部又调用了 `tryReleaseShared` 方法，这个方法由子类 `Sync` 重写：
``` Java
protected final boolean tryReleaseShared(int releases) {
    for (;;) {
        int current = getState();
        int next = current + releases;
        if (next < current) // overflow 溢出
            throw new Error("Maximum permit count exceeded");
        if (compareAndSetState(current, next))
            return true;
    }
}
```
`tryReleaseShared` 的逻辑比较简单，将信号量归还，CAS 更新 state 即可。

`Semaphore` 还提供了 `tryAcquire` 方法以及一些辅助方法，这里不再赘述。
## 总结
`Semaphore` 提供了线程的控制方案，对线程的竞争提供了公平和非公平的方式。
## 应用
``` Java
import java.util.concurrent.Semaphore;

public class SemaphoreTest {

    private static final int numOfThreads = 5; // 线程数

    private static final int sleepTime = 3000; // 睡眠时间

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(numOfThreads);
        System.out.println("停车场一共有 " + numOfThreads + " 个停车位");
        for (int i = 0; i < 2 * numOfThreads; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        semaphore.acquire();
                        System.out.println(Thread.currentThread().getName() + " 停车");
                        Thread.sleep(sleepTime);
                        System.out.println(Thread.currentThread().getName() + " 开走了");
                        semaphore.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

}
```
输出结果为：
```
停车场一共有 5 个停车位
Thread-2 停车
Thread-0 停车
Thread-1 停车
Thread-4 停车
Thread-3 停车
Thread-0 开走了
Thread-2 开走了
Thread-8 停车
Thread-7 停车
Thread-1 开走了
Thread-4 开走了
Thread-3 开走了
Thread-9 停车
Thread-6 停车
Thread-5 停车
Thread-7 开走了
Thread-8 开走了
Thread-9 开走了
Thread-6 开走了
Thread-5 开走了
```
## CountDownLatch、CyclicBarrier 和 Semaphore
- `CountDownLatch` 由一类线程控制另一类线程，`CyclicBarrier` 是一类线程都执行到了 `await` 方法后再继续执行，`Semaphore` 则是控制同时执行的线程的数量
- `CountDownLatch` 主要通过 `await` 方法和 `countDown` 方法控制，`CyclicBarrier` 只通过 `await` 方法，`Semaphore` 通过 `acquire` 方法和 `release` 方法