package cn.alone.DesignPattern.ObserverPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RojerAlone on 2017-09-16.
 * 任务中心，当有新任务添加时，就通知观察者
 */
public class TaskCenter {

    /**
     * 所有的观察者
     */
    private List<Worker> workers = new ArrayList<>();

    public void addTask(String task) {
        notifyWorkers(task);
    }

    /**
     * 增加观察者
     * @param worker
     */
    public void addWorker(Worker worker) {
        workers.add(worker);
    }

    /**
     * 通知观察者
     * @param task
     */
    private void notifyWorkers(String task) {
        for (Worker worker : workers) {
            worker.work(task);
        }
    }

}
