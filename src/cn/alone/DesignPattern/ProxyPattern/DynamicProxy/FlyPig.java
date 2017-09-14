package cn.alone.DesignPattern.ProxyPattern.DynamicProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by RojerAlone on 2017-09-14.
 * 飞猪，动态代理类
 */
class FlyPig implements InvocationHandler {

    /**
     * 被代理类
     */
    private Object real;

    public FlyPig(Object proxy) {
        this.real = proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("飞猪代理 " + method.getName());
        Object obj = method.invoke(real, args);
        return obj;
    }
}
