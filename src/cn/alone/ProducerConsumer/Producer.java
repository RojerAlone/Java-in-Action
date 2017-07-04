package cn.alone.ProducerConsumer;

/**
 * Created by RojerAlone on 2017/7/4.
 * 生产者，获取锁后查看仓库，如果仓库满了就释放锁等待消费者消费，否则生产商品，然后通知在仓库等待的其他线程
 */
public class Producer implements Runnable {

    private Repostry repostry;

    public Producer(Repostry repostry) {
        this.repostry = repostry;
    }

    @Override
    public void run() {
            while (true) {
        synchronized (repostry) {
                // 如果仓库满了就释放锁
                if (repostry.isFull()) {
                    System.out.println(Thread.currentThread().getName() + "\trepostry is full, waiting......");
//                    try {
//                        repostry.notifyAll();
//                        repostry.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                } else {
                    System.out.println(Thread.currentThread().getName() + "\tproducts......");
                    repostry.product();
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
