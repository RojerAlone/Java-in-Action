package cn.alone.ThreadPool;

import java.util.concurrent.*;

/**
 * Created by RojerAlone on 2017-08-03.
 */
public class FutureTaskDemo {

    static class CallableTask implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            int res = 0;
            System.out.println("正在执行任务......");
            Thread.sleep(1000 * 3);
            for (int i = 0; i < 100; i++) {
                res += i;
            }
            return res;
        }
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CallableTask task = new CallableTask();
        try {
            Future future = executorService.submit(task);
            while (!future.isDone()) {
                System.out.println("callable task is running......");
                Thread.sleep(1000);
            }
            System.out.println("callable task 运行结果为：" + future.get());
            FutureTask futureTask = new FutureTask(task);
            new Thread(futureTask).start();
            while (!futureTask.isDone()) {
                System.out.println("future task is running......");
                Thread.sleep(1000);
            }
            System.out.println("future task 运行结果为：" + futureTask.get());
            executorService.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}
