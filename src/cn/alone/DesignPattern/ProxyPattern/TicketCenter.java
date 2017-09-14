package cn.alone.DesignPattern.ProxyPattern;

/**
 * Created by RojerAlone on 2017-09-14.
 * 售票中心的接口
 */
public interface TicketCenter {

    /**
     * 登录
     */
    void login(String username, String pwd);

    /**
     * 买票
     */
    void buyTicket(int id);

    /**
     * 退票
     */
    void returnTicket(int id);
}
