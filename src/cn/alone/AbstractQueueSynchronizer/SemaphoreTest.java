package cn.alone.AbstractQueueSynchronizer;

import java.util.concurrent.Semaphore;

/**
 * Created by RojerAlone on 2017-08-28.
 */
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
