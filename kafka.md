kafka基本介绍

1.版本号

```
Scala 2.12  - kafka_2.12-3.3.1.tgz (asc, sha512)
Scala 2.13  - kafka_2.13-3.3.1.tgz (asc, sha512)
```

## 1.kafka启动

```
kafka-topics.bat --create --topic quickstart-events --bootstrap-server localhost:9092
```



## 2.kafka常用命令

2.1创建topic

```
bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9092


kafka-topics.bat --create --topic quickstart-events --bootstrap-server localhost:9092
```

2.2看topic的详情

```
bin/kafka-topics.sh --describe --topic quickstart-events --bootstrap-server localhost:9092
```

2.3topic发送信息

```
kafka-console-producer.sh --topic quickstart-events --bootstrap-server localhost:9092
```



## 3.配置文件的讲解

官方的介绍

```
https://kafka.apache.org/documentation/#configuration
```



## 4.kafka的api分类

![](\image\1-kafka的api分类.png)



## 5.常见报错

5.1  delivery.timeout.ms should be equal to or larger than linger.ms + request.timeout.ms

解决方案

```
设置 delivery.timeout.ms 大于  request.timeout.ms
```

5.2 队列有时候能消费到数据，有时候不能[kafka的topic中有大量的数据]

```
kafka有过期时间，一旦超过这个过期时间，consumer是不能消费到这些数据的，即使这些数据还没有被删除[过期不意味着立马删除]
```

5.3 手动提交处理异常不当，造成的重复消费，从而造成逻辑错误

```
根据业务需要，把错误信息记录起来，把错误进行try catch，保证consumer能够正常提价offset
```

5.4 三种传递语义可能引发的问题

```
https://blog.csdn.net/wangxueying5172/article/details/122506420?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-0-122506420-blog-119881907.pc_relevant_recovery_v2&spm=1001.2101.3001.4242.1&utm_relevant_index=3
```



## 6.题外闲谈

6.1版本号

```
Scala 2.12  - kafka_2.12-3.3.1.tgz (asc, sha512)
Scala 2.13  - kafka_2.13-3.3.1.tgz (asc, sha512)
```

6.2 kafka部署再windows和linux上有区别吗？

```
1. Kafka 客户端底层使用了 Java 的 selector，selector 在 Linux 上的实现机制是 epoll，而在 Windows 平台上的实现机制是 select。因此在这一点上将 Kafka 部署在 Linux 上是有优势的，因为能够获得更高效的 I/O 性能。
通常情况下认为epoll比select更加的高级。
2.零拷贝（Zero Copy）技术，就是当数据在磁盘和网络进行传输时避免昂贵的内核态数据拷贝从而实现快速的数据传输。
3.windows版本的kafka的bug修复慢，windows上练习下kafka即可。
```

## 7.kafka和其他消息系统的区别

```
Kafka works well as a replacement for a more traditional message broker. Message brokers are used for a variety of reasons (to decouple processing from data producers, to buffer unprocessed messages, etc). In comparison to most messaging systems Kafka has better throughput, built-in partitioning, replication, and fault-tolerance which makes it a good solution for large scale message processing applications.

In our experience messaging uses are often comparatively low-throughput, but may require low end-to-end latency and often depend on the strong durability guarantees Kafka provides.

In this domain Kafka is comparable to traditional messaging systems such as ActiveMQ or RabbitMQ.
```

kafka和rocketmq的区别

```
https://rocketmq.apache.org/docs/
```

1. 全局的消息一致性
2. 事务比较完善

疑问点

consumer提交的几种方式

- at-least-once
- at most once
- exactly once