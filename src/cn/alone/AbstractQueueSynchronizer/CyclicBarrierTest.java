package cn.alone.AbstractQueueSynchronizer;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by RojerAlone on 2017-08-23.
 */
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
