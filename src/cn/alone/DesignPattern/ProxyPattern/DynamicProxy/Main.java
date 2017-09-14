package cn.alone.DesignPattern.ProxyPattern.DynamicProxy;

import cn.alone.DesignPattern.ProxyPattern.App12306;
import cn.alone.DesignPattern.ProxyPattern.TicketCenter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created by RojerAlone on 2017-09-14.
 */
public class Main {

    public static void main(String[] args) {
        App12306 app12306 = new App12306();
        InvocationHandler handler = new FlyPig(app12306);
        TicketCenter ticketCenter = (TicketCenter) Proxy.newProxyInstance(handler.getClass().getClassLoader(),
                new Class[]{TicketCenter.class}, handler);
        ticketCenter.login("user", "123456");
        ticketCenter.buyTicket(1);
        ticketCenter.returnTicket(1);
    }

}
