package cn.alone.DesignPattern.SingletonPattern;

/**
 * Created by RojerAlone on 2017-09-05.
 * 双重检查锁
 */
public class DoubleCheckLocking {

    private volatile static DoubleCheckLocking singleton;

    private DoubleCheckLocking (){}

    public static DoubleCheckLocking getSingleton() {
        if (singleton == null) {
            synchronized (DoubleCheckLocking.class) {
                if (singleton == null) { // 在此之前，可能已经有线程初始化过对象了，所以要再次检查是否为 null
                    singleton = new DoubleCheckLocking();
                }
            }
        }
        return singleton;
    }

}
