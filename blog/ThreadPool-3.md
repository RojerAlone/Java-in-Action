# ThreadPool 之 `Callable`、`Future` 和 `FutureTask`
## `Callable`、`Future` 和 `FutureTask`
　　一般情况下，线程有两种创建方式，一种是继承 `Thread` 类，一种是实现 `Runnable` 接口。在线程运行执行的都是 `run` 方法，这是一个 `void` 方法，没有返回值。如果要获取线程执行的结果，就必须传入一个变量，在执行完之前将结果写入这个变量中。

　　从 JDK1.5 开始，Java 提供了 `Callable` 和 `Future` 接口，通过它们可以返回执行结果。
### `Callable`
　　`Callable` 是一个接口，其中只有一个 `call` 方法：
``` Java
public interface Callable<V> {
    V call() throws Exception;
}
```
　　可以看到这是一个泛型接口，返回一个结果值。
### `Future`
　　`Future` 是一个接口，代码如下：
``` Java
public interface Future<V> {

    /**
     * mayInterruptIfRunning 表示如果任务正在执行是否允许中断任务
     */
    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isDone();

    /**
     * 阻塞地获取执行结果，如果还没有执行完毕会一直等待直到执行完毕
     */
    V get() throws InterruptedException, ExecutionException;

    /**
     * 如果在执行时间内还没有执行完，返回 null
     */
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
```
　　因为 `Future` 是一个接口，`FutureTask` 是它的实现类。
### `FutureTask`
``` Java
public class FutureTask<V> implements RunnableFuture<V> {
    public FutureTask(Runnable runnable, V result) {
        // 调用 Executors 的方法将 runnable 封装为 callable
        this.callable = Executors.callable(runnable, result); 
        this.state = NEW;       // ensure visibility of callable
    }
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;       // ensure visibility of callable
    }
    // 省略其他代码
}
```
　　`FutureTask` 实现了 `RunnableFuture<V>` 接口，查看`RunnableFuture<V>`：
``` Java
public interface RunnableFuture<V> extends Runnable, Future<V> {
    void run();
}
```
　　`RunnableFuture<V>` 接口又继承了 `Runnable` 接口和 `Future` 接口，所以 `FutureTask` 既能被当做线程执行又能当做 `Future` 获取执行结果。
### Demo
``` Java
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
```
运行结果：
``` Console
callable task is running......
正在执行任务......
callable task is running......
callable task is running......
callable task is running......
callable task 运行结果为：4950
future task is running......
正在执行任务......
future task is running......
future task is running......
future task 运行结果为：4950
```
