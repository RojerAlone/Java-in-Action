package cn.alone.DesignPattern.StrategyPattern;

/**
 * Created by RojerAlone on 2018-03-01
 * 子结点接口
 */
interface NextStrategy extends Iterable<Character> {

    String getNext(char c);

    void setNext(char c, String value);

}
