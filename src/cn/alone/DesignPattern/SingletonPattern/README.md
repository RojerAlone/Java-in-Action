# 单例模式
单例模式是设计模式中最简单的模式之一，是 **创建型** 模式单例模式确保某个类只有一个对象被创建，在内部进行创建，调用时只需要获取即可。
## 不同的实现
单例模式有多重实现方式，如下所示：

| 名字 | 是否延迟初始化 | 是否线程安全 |
|:---: | :---: | :---: |
| [懒汉式（非线程安全）](https://github.com/RojerAlone/Java-in-Action/blob/master/src/cn/alone/DesignPattern/SingletonPattern/LazyMan.java) | 是 | 否 |
| 懒汉式（线程安全） | 是 | 是 |
| 饿汉式 | 否 | 是 |
| 双重校验锁（double-checked locking） | 是 | 是 |
| 静态内部类 | 是 | 是 |
| 枚举 | 否 | 是 |

[注] 懒汉式，当需要时再创建对象；饿汉式，类创建时就创建生成的对象。