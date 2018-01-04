package cn.alone.DesignPattern.BuilderPattern;

/**
 * Created by RojerAlone on 2018-01-03
 */
public class Main {

    public static void main(String[] args) {
        People people = PeopleBuilder.newBuilder().setHeight(180).setWeight(140).build();
        System.out.println(people);
    }

}
