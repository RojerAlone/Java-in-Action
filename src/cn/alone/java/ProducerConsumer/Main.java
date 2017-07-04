package cn.alone.java.ProducerConsumer;

/**
 * Created by RojerAlone on 2017/7/4.
 */
public class Main {

    public static void main(String[] args) {
        Repostry repostry = new Repostry();
        Thread producer = new Thread(new Producer(repostry), "producer");
        Thread consumer_1 = new Thread(new Consumer(repostry), "consumer-1");
        Thread consumer_2 = new Thread(new Consumer(repostry), "consumer-2");
        Thread consumer_3 = new Thread(new Consumer(repostry), "consumer-3");

        producer.start();
        consumer_1.start();
        consumer_2.start();
        consumer_3.start();
    }

}
