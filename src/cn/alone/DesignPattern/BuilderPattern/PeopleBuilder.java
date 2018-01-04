package cn.alone.DesignPattern.BuilderPattern;

/**
 * Created by RojerAlone on 2018-01-03
 */
public class PeopleBuilder {

    private int height;

    private int weight;

    PeopleBuilder() {}

    public static PeopleBuilder newBuilder() {
        return new PeopleBuilder();
    }

    public People build() {
        return new People(this);
    }

    public int getHeight() {
        return height;
    }

    public PeopleBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public int getWeight() {
        return weight;
    }

    public PeopleBuilder setWeight(int weight) {
        this.weight = weight;
        return this;
    }
}
