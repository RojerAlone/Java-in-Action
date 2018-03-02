package cn.alone.DesignPattern.StrategyPattern;

import java.util.Collections;
import java.util.Iterator;

/**
 * Created by RojerAlone on 2018-03-01
 */
class SingleNextStrategy implements NextStrategy {

    private char character;
    private String value;

    @Override
    public String getNext(char c) {
        return c == character ? value : null;
    }

    @Override
    public void setNext(char c, String value) {
        this.character = c;
        this.value = value;
    }

    @Override
    public Iterator<Character> iterator() {
        if (value == null) {
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Character next() {
                    return null;
                }
            };
        } else {
            return Collections.singleton(character).iterator();
        }
    }
}
