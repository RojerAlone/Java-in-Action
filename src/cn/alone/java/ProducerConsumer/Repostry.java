package cn.alone.java.ProducerConsumer;

/**
 * Created by RojerAlone on 2017/7/4.
 * 仓库，生产者和消费者都要从仓库中存取数据
 */
public class Repostry {

    /**
     * 仓库上限
     */
    private final int TOTAL = 10;

    private int things;

    public Repostry() {
        this.things = 0;
    }

    public void product() {
        things++;
    }

    public void consume() {
        things--;
    }

    public boolean isFull() {
        return things == TOTAL;
    }

    public boolean isEmpty() {
        return things == 0;
    }

}
