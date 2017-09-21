package cn.alone.DesignPattern.FactoryPattern.SimpleFactoryPattern;

/**
 * Created by RojerAlone on 2017-09-18.
 * 香蕉类
 */
public class Banana implements Fruit {
    @Override
    public void eat() {
        System.out.println("banana is delicious");
    }
}
