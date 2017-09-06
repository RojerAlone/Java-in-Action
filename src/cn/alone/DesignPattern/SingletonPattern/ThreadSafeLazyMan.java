package cn.alone.DesignPattern.SingletonPattern;

/**
 * Created by RojerAlone on 2017-09-05.
 * 线程安全的懒汉式单例模式
 */
public class ThreadSafeLazyMan {

    private static ThreadSafeLazyMan instance;

    private ThreadSafeLazyMan (){ }

    /**
     * 加锁虽然保证了线程安全，但是影响了效率
     */
    public static synchronized ThreadSafeLazyMan getInstance() {
        if (instance == null) {
            instance = new ThreadSafeLazyMan();
        }
        return instance;
    }

}
