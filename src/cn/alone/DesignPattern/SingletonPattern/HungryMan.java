package cn.alone.DesignPattern.SingletonPattern;

/**
 * Created by RojerAlone on 2017-09-05.
 * 饿汉式单例模式
 */
public class HungryMan {

    /**
     * 类加载时初始化（JVM 的类加载机制确保了线程安全地创建对象，所以是线程安全的），浪费内存
     */
    private static HungryMan instance = new HungryMan();

    private HungryMan (){ }

    public static HungryMan getInstance() {
        return instance;
    }

}
