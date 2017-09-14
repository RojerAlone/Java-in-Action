package cn.alone.DesignPattern.ProxyPattern.StaticProxy;

import cn.alone.DesignPattern.ProxyPattern.App12306;
import cn.alone.DesignPattern.ProxyPattern.TicketCenter;

/**
 * Created by RojerAlone on 2017-09-14.
 * 飞猪，买票的代理者
 */
class FlyPig implements TicketCenter {

    private App12306 app12306 = new App12306();

    @Override
    public void login(String username, String pwd) {
        System.out.println("通过飞猪登录 12306");
        app12306.login(username, pwd);
    }

    @Override
    public void buyTicket(int id) {
        System.out.println("飞猪帮您购买车次 " + id);
        app12306.buyTicket(id);
    }

    @Override
    public void returnTicket(int id) {
        System.out.println("飞猪帮您退票车次 " + id);
        app12306.returnTicket(id);
    }
}
