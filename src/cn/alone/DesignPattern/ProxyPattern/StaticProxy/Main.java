package cn.alone.DesignPattern.ProxyPattern.StaticProxy;

import cn.alone.DesignPattern.ProxyPattern.TicketCenter;

/**
 * Created by RojerAlone on 2017-09-14.
 */
public class Main {

    public static void main(String[] args) {
        TicketCenter ticketCenter = new FlyPig();
        ticketCenter.login("user", "123456");
        ticketCenter.buyTicket(1);
        ticketCenter.returnTicket(1);
    }

}
