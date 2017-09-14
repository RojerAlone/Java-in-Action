package cn.alone.DesignPattern.ProxyPattern;

/**
 * Created by RojerAlone on 2017-09-14.
 * 12306 客户端
 */
public class App12306 implements TicketCenter {
    @Override
    public void login(String username, String pwd) {
        System.out.println(username + " 登录成功");
    }

    @Override
    public void buyTicket(int id) {
        System.out.println("购买 " + id + " 车次成功");
    }

    @Override
    public void returnTicket(int id) {
        System.out.println("退票 " + id + " 车次成功");
    }
}
