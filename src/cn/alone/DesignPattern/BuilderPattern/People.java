package cn.alone.DesignPattern.BuilderPattern;

/**
 * Created by RojerAlone on 2018-01-03
 */
class People {

    private int height;

    private int weight;

    People(PeopleBuilder builder) {
        this.height = builder.getHeight();
        this.weight = builder.getWeight();
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "People{" +
                "height=" + height +
                ", weight=" + weight +
                '}';
    }
}
