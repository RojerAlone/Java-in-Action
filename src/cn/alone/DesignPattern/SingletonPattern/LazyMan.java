package cn.alone.DesignPattern.SingletonPattern;

/**
 * Created by RojerAlone on 2017-09-05.
 * 懒汉式单例模式，非线程安全
 */
public class LazyMan {

    private static LazyMan instance;

    private LazyMan() { }

    public static LazyMan getInstance() {
        if (instance == null) {
            instance = new LazyMan();
        }
        return instance;
    }

}
