package cn.alone.DesignPattern.FactoryPattern.SimpleFactoryPattern;

/**
 * Created by RojerAlone on 2017-09-18.
 */
public class Main {

    public static void main(String[] args) {
        FruitShop.getFruit("apple").eat();
        FruitShop.getFruit("banana").eat();
        FruitShop.getFruit("orange").eat();
    }

}
