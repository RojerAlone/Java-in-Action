package cn.alone.DesignPattern.StrategyPattern;

/**
 * Created by RojerAlone on 2018-03-01
 */
public class DynamicNext {

    private static final int SINGLE_NEXT_BOUND = 2;
    private static final int TREEMAP_NEXT_BOUND = 8;

    private NextStrategy next;
    /**
     * 记录当前结点的子结点的个数，用于扩展子结点
     */
    private int size;

    public String getNext(char c) {
        if (next == null) {
            return null;
        }
        return next.getNext(c);
    }

    void setNext(char c, String value) {
        if (next != null && next.getNext(c) != null) {
            next.setNext(c, value);
            return;
        }
        size += 1;
        NextStrategy newStrategy = null;
        switch (size) {
            case SINGLE_NEXT_BOUND - 1:
                next = new SingleNextStrategy();
                break;
            case SINGLE_NEXT_BOUND:
                newStrategy = new TreeMapNextStrategy();
                break;
            case TREEMAP_NEXT_BOUND:
                newStrategy = new HashMapNextStrategy();
                break;
            default:
                break;
        }
        if (newStrategy != null) {
            for (char ch : next) {
                newStrategy.setNext(ch, next.getNext(ch));
            }
            this.next = newStrategy;
        }
        next.setNext(c, value);
    }

}
