package cn.alone.DesignPattern.StrategyPattern;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by RojerAlone on 2018-03-01
 */
class TreeMapNextStrategy implements NextStrategy {

    private Map<Character, String> next = new TreeMap<>();

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
