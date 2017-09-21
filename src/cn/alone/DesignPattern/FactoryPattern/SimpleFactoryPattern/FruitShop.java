package cn.alone.DesignPattern.FactoryPattern.SimpleFactoryPattern;

/**
 * Created by RojerAlone on 2017-09-18.
 * 水果商店，用来生产水果
 */
public class FruitShop {

    public static Fruit getFruit(String name) {
        if (name.equalsIgnoreCase("banana")) {
            return new Banana();
        } else if (name.equalsIgnoreCase("apple")) {
            return new Apple();
        } else if (name.equalsIgnoreCase("orange")) {
            return new Orange();
        } else {
            return null;
        }
    }

}
