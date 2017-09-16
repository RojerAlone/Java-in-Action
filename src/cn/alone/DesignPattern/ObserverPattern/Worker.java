package cn.alone.DesignPattern.ObserverPattern;

/**
 * Created by RojerAlone on 2017-09-16.
 * 工作者，也就是观察者，当观察到有任务时，就执行新任务
 */
public interface Worker {

    void work(String task);

}
