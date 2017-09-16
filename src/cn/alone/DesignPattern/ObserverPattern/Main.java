package cn.alone.DesignPattern.ObserverPattern;

/**
 * Created by RojerAlone on 2017-09-16.
 */
public class Main {

    public static void main(String[] args) {
        TaskCenter taskCenter = new TaskCenter();
        taskCenter.addWorker(new SmallWorker());
        taskCenter.addWorker(new BigWorker());

        taskCenter.addTask("任务一");
        taskCenter.addTask("任务二");
        taskCenter.addTask("任务三");
    }

}
