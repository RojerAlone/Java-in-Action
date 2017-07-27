package cn.alone.ThreadPool;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by RojerAlone on 2017-07-27.
 */
public class ThreadPoolDemo {

    static class DemoThread implements Runnable {

        private String name;

        public DemoThread(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            System.out.println("正在执行任务 " + name);
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务 " + name + " 执行完毕");
        }
    }

    public static void main(String[] args) {
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            threadPool.execute(new DemoThread("thread - " + i));
            System.out.println("线程池中线程数目：" + threadPool.getTaskCount() + ", 等待执行任务数：" +
                    threadPool.getQueue().size() + "，线程池中已完成的线程数为：" + threadPool.getCompletedTaskCount());
        }
        threadPool.shutdown();
    }
}
