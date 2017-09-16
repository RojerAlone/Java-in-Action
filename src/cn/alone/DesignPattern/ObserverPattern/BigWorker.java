package cn.alone.DesignPattern.ObserverPattern;

/**
 * Created by RojerAlone on 2017-09-16.
 */
public class BigWorker implements Worker {
    @Override
    public void work(String task) {
        System.out.println("大工收到任务并完成任务: " + task);
    }
}
