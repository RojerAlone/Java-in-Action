package cn.alone.ProducerConsumer;

/**
 * Created by RojerAlone on 2017/7/4.
 * 消费者，获取锁后查看仓库，如果仓库为空就释放锁等待商品生产，否则消费商品，然后通知在仓库等待的其他线程
 */
public class Consumer implements Runnable {

    private Repostry repostry;

    public Consumer(Repostry repostry) {
        this.repostry = repostry;
    }

    @Override
    public void run() {
            while (true) {
        synchronized (repostry) {
                // 如果仓库为空就等待生产者生产商品
                if (repostry.isEmpty()) {
                    System.out.println(Thread.currentThread().getName() + "\trepostry is empty, waiting......");
//                    try {
//                        repostry.notifyAll();
//                        repostry.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                } else {
                    System.out.println(Thread.currentThread().getName() + "\tconsume......");
                    repostry.consume();
                    try {
                        Thread.sleep(1000);
//                        repostry.notifyAll();
//                        repostry.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
