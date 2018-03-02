package cn.alone.DesignPattern.StrategyPattern;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by RojerAlone on 2018-03-01
 */
class HashMapNextStrategy implements NextStrategy {

    private Map<Character, String> next = new HashMap<>();

    @Override
    public String getNext(char c) {
        return next.get(c);
    }

    @Override
    public void setNext(char c, String value) {
        next.put(c, value);
    }

    @Override
    public Iterator<Character> iterator() {
        return next.keySet().iterator();
    }
}
