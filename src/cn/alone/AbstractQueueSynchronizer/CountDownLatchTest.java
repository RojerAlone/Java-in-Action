package cn.alone.AbstractQueueSynchronizer;

import java.util.concurrent.CountDownLatch;

/**
 * Created by RojerAlone on 2017-08-23.
 */
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
                    threadLatch.countDown();
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
