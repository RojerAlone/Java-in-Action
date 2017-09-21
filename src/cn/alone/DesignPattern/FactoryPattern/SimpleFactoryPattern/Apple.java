package cn.alone.DesignPattern.FactoryPattern.SimpleFactoryPattern;

/**
 * Created by RojerAlone on 2017-09-18.
 * 苹果类
 */
public class Apple implements Fruit {
    @Override
    public void eat() {
        System.out.println("apple is delicious");
    }
}
