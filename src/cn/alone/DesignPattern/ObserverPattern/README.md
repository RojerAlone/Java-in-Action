# 观察者模式
观察者模式是**行为型**设计模式，对象间存在一对多关系时，当一个对象发生了改变（被观察者），其他对应的多个对象（观察者）会观察到这个对象的改变（其实是被观察者通知）。

## [Demo](https://github.com/RojerAlone/Java-in-Action/tree/master/src/cn/alone/DesignPattern/ObserverPattern)
有这样一个任务中心，里边有很多的工人，每当有一个任务到来，就被工人观察到，然后工人执行任务。