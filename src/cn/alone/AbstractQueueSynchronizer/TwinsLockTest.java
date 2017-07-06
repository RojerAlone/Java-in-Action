package cn.alone.AbstractQueueSynchronizer;

import java.util.concurrent.locks.Lock;

/**
 * Created by RojerAlone on 2017/7/6.
 * ！！！未按照预期运行
 */
public class TwinsLockTest {

    public static void main(String[] args) throws InterruptedException {
        Lock lock = new TwinsLock();

        class Worker extends Thread {
            public void run() {
                while (true) {
                    lock.lock();
                    try {
                        Thread.sleep(1000);
                        System.out.println(Thread.currentThread().getName());
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
            }
        }

        for (int i = 0 ; i < 10; i++) {
            Worker worker = new Worker();
            worker.setDaemon(true);
            worker.start();
        }
        for (int i = 0 ; i < 10; i++) {
            Thread.sleep(1000);
            System.out.println();
        }
    }

}
