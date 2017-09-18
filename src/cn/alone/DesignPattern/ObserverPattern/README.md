# 观察者模式
观察者模式是**行为型**设计模式，对象间存在一对多关系时，当一个对象发生了改变（被观察者），其他对应的多个对象（观察者）会观察到这个对象的改变（其实是被观察者通知）。

## [Demo](https://github.com/RojerAlone/Java-in-Action/blob/master/src/cn/alone/DesignPattern/ObserverPattern/Main.java)
有这样一个任务中心，里边有很多的工人，每当有一个任务到来，就被工人观察到，然后工人执行任务。
## JDK 对观察者模式的支持
JDK 提供了观察者模式相关的接口和类，分别是 `java.util.Observer` 接口和 `java.util.Observable`类，`java.util.Observer` 接口中有一个 `void update(Observable o, Object arg);`方法，发生改变时供被观察者调用。`java.util.Observable`类中提供了很多方法，比如 `addObserver`、`deleteObserver` 和 `notifyOvservers` 等方法。

和自己写的方法大同小异，不再赘述。