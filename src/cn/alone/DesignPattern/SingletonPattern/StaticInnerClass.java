package cn.alone.DesignPattern.SingletonPattern;

/**
 * Created by RojerAlone on 2017-09-05.
 * 静态内部类单例模式
 */
public class StaticInnerClass {

    /**
     * 当内部类不被使用时不会加载，使用时才加载，同样的，JVM 的类加载机制保证了线程安全
     */
    private static class SingletonHolder {
        private static final StaticInnerClass INSTANCE = new StaticInnerClass();
    }

    private StaticInnerClass (){ }

    public static final StaticInnerClass getInstance() {
        return SingletonHolder.INSTANCE;
    }

}
