### JMS（Java Message Service）

------

[TOC]

##### 00：JMS

- Java 消息服务应用程序接口，是一个用于**在应用程序之间进行异步消息通信的 Java API**。

- Java 消息服务是一个与具体平台无关的API，绝大多数MOM提供商都对JMS提供支持。

- Java 提供了接口，我们使用已经开源实现好的MQ；

- ActiveMQ 是 开源的 JMS 实现；

##### 01：为什么要学JMS

- 在JAVA中，如果两个应用程序之间对各自都不了解，甚至这两个程序可能部署在不同的地方，那么它们之间如何发送消息呢？
  - 一个应用程序A部署在印度，另一个应用程序部署在美国，然后**每当A触发某件事后，B想从A获取一些更新信息**。当然，也有可能不止一个B对A的更新信息感兴趣，可能会有N个类似B的应用程序想从A中获取更新的信息。在这种情况下，JAVA提供了最佳的解决方案 JMS；
  - 特性：发送消息的时候，**消费者不需要在线**。服务器发送了消息，然后就不管了；等到客户端上线的时候，能保证接收到服务器发送的消息；

##### 02：JMS 有什么天生的优势

1. **异步**：天生就是异步的，另一端获取消息的时候，不需要主动发送请求，消息会自动发送给可用的客户端；
2. **可靠**：JMS保证消息只会递送一次，但是糟糕的环境下会出现重复；

##### 03：两种通信模式

1. Point-to-Point Messaging Domain **（点对点）**
2. Publish/Subscribe Messaging Domain **（发布/订阅模式）**

###### 点对点通信模型（Point-to-Point Messaging Domain)

- 在点对点通信模式中，应用程序由**消息队列，生产者，消费者**组成。每个消息都被发送到一个特定的队列，接收者从队列中获取消息。队列保留着消息，直到他们被消费或超时，消息的接收者**可以动态地加入或离开系统**，而不需要发送者事先知道具体的消费者。

- 每条消息只能有一个消费者，消费者主动去**pull**；

- 当生产者发送消息的时候，无论消费者服务在不在线，最终消费者都能获取到消息；

- 当消费者收到消息的时候消费完之后，会发送确认收到通知（ack）给JMS；

  <img src="https://github.com/likang315/Middleware/blob/master/04：MQ/photos/point-to-point.png?raw=true" style="zoom:67%;" />

###### 发布/订阅通信模型：（Publish/Subscribe Messaging Domain）

- 一条消息可以有多个订阅者，订阅某个主题，被动的接受或者主动拉取；
- 发布者和订阅者有时间依赖性，只有当客户端创建订阅后才能接受消息，且订阅者需一直保持活动状态以接收消息;
- <img src="https://github.com/likang315/Middleware/blob/master/04：MQ/photos/publish:subscribe-Message-domain.png?raw=true" alt="publish:subscribe-Message-domain" style="zoom:67%;" />

##### 04：JMS 消费方式

1. **同步**：消费者通过调用 **receive（）**方法来接收消息。在 receive（）方法中，线程会**阻塞**直到消息到达或者到指定时间后消息仍未到达，退出。
2. **异步**：消费者需注册一个消息监听器，只要消息到达，JMS服务提供者会通过调用监听器的 onMessage() 递送消息。

