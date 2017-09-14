# 代理模式
代理模式是**结构型**模式，用一个类代表另一个类的功能，代理类和被代理类要继承共同的父类或者实现相同的接口。
## 举个栗子
代理模式一般有两种，动态代理和静态代理。
### 静态代理
静态代理是我们在编码的时候就编写好代理类或者由相关的工具生成代理类，静态代理的代理类是会生成 `.class` 文件再由虚拟机加载。

现在有很多抢票软件，其中有一个是阿里的飞猪，在飞猪中登录 12306 账号就可以让飞猪帮我们抢票，这就是典型的代理模式，我们把钱给了飞猪，它再去买票，其实票还是从 12306 上买的，只不过飞猪成了抢票的代理者。

如果代理类代理的方法很多的话，就要对每一个方法进行代理，编写较为复杂；如果代理的接口增加了一个方法，那么不仅所有被代理类需要实现这个方法，代理类也要实现这个方法，复用性降低了。

[飞猪抢票 Demo](https://github.com/RojerAlone/Java-in-Action/blob/master/src/cn/alone/DesignPattern/ProxyPattern/StaticProxy/Main.java)
### 动态代理
动态代理又分为两种，Java 自带的动态代理和 `cglib` 动态代理。

- Java 动态代理是在程序运行期间 JVM 根据反射等机制动态地生产代理类，所以不存在 `.class` 文件，缺点是被代理类必须要实现接口。
- `cglib` 代理是通过 `cglib` 运行期读取被代理类的 `.class` 文件然后修改其字节码（`cglib` 采用了非常底层的字节码技术 `asm`，`fastjson` 也使用了这个）动态生成子类，被代理类不需要实现接口。

Spring AOP 中同时使用了 Java 动态代理和 `cglib` 动态代理。

[Java 的动态代理](https://github.com/RojerAlone/Java-in-Action/tree/master/src/cn/alone/DesignPattern/ProxyPattern/DynamicProxy)

1. 通过实现 `InvocationHandler` 接口创建自己的调用处理器；
2. 通过为 `Proxy` 类指定 `ClassLoader` 对象和一组 `interface` 类创建动态代理类；
3. 通过反射机制获取动态代理类的构造函数，其唯一参数类型是调用处理器接口类型
3. 通过构造函数创建动态代理类实例，构造时调用处理器对象作为参数被传入。