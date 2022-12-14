## 1.kafka是什么?

kafka是一个**高吞吐量、低延迟**的消息队列系统，源码由**java（客户端）和scala（服务端）**语言编写。

## 2.kafka有哪些优势

1、吞吐量相当高；得益于[zero-copy](https://zh.m.wikipedia.org/zh-hans/%E9%9B%B6%E5%A4%8D%E5%88%B6)和顺序刷盘。

>The key fact about disk performance is that the throughput of hard drives has been diverging from the latency of a disk seek for the last decade. As a result the performance of linear writes on a [JBOD](http://en.wikipedia.org/wiki/Non-RAID_drive_architectures) configuration with six 7200rpm SATA RAID-5 array is about **600MB/sec** but the performance of random **writes is only about 100k/sec**—a difference of over 6000X.

2、多分区的机制可以让kafka拥有灵活的拓展性，并且能提高kafka的吞吐量。
3、有完整的生态，特别是大数据领域（能够保存大量的数据比如PB级别的数据）。

4、活跃的社区环境，当我们遇到问题的时候可以求助于社区，如果是bug的话社区也会进行快速修复。

## 3.kafka基本概念

### 3.1 主题（topic）、生产者（producer）和分区（partition）

![](./image/2-kafka生产者.png)

### 3.2 消费者与消费组

![](./image/5-消费者和消费组的关系.png)

消费组有如下特点

- 一个消费组内可以有1个或者多个消费者。
- 多个消费组之间的消费互不影响。

### 3.3 offset(偏移量)

![](./image/6-offset.png)

offset有如下特点

- 每个消费组中的每个分区维护一套offset，分区与分区的offset互相隔离。
- 每条消息对应一个offset，并且是唯一的。

### 3.4 消费组详解

> kafka消费组就是一个组内可以有一个或者多个消费者。而消费组与消费组之间的消费是没有任何关系，两个不同的消费组之间消费互不影响。

假设现在某个主题有4个分区，那么随着消费组内的消费者的变化，消费者订阅分区的策略如下。

#### 3.4.1 消费组只有一个消费者

![](./image/consumer-group/1-一个消费者.jpg)

#### 3.4.2 消费组内有2个消费者

![](./image/consumer-group/2-多个消费者.jpg)

#### 3.4.3 消费者和分区数一样多

![](./image/consumer-group/3-消费者数量和分区数一样.jpg)

#### 3.4.4 组内消费者数量大于分区数量

![](./image/consumer-group/4-消费者数大于分区数.jpg)



#### 3.4.5 消费组与消费组之间的关系

![](./image/consumer-group/5-消费者和分组直接的关系.jpg)

## 4.kafka安装与依赖组件

[Kafka官方安装文档](https://kafka.apache.org/quickstart)

> kafka的运行依赖于zookeeper，zookeeper的目的主要是用来管理kafka的一些`元数据`信息；比如：topic主题和分区的元数据、Broker 数据、ACL（Access Control List 访问控制列表） 信息等等。
>
> kafka安装包版本号解释
>
> > 安装包`kafka_2.12-3.3.1.tgz`，其中2.12代表的是本安装包是采用2.12版本的scala进行编译的，3.3.1才是kafka的真正版本号。

小提示

> 虽然现在kafka还是依赖于zookeeper，但是从kafka从0.8.2.x开始就在酝酿逐渐减少kafka对zookeeper的依赖，因为kafka天然会有大量的读写操作，而zookeeper又天然的不适用于这种高频操作。
>
> 比如原来以前的kafka中维护offset都是交给zookeeper来完成的，现在的kafka版本都是有broker来统一维护offset。

## 5.常用配置解释

[服务器配置文件](./file/server.properties)

kafka的broker的参数多达200多个，broker常用的配置文件如下

> log.dirs. 制定日志的位置，可以指定多个，通常设置多个路径挂载在不同的硬盘，这样可以提高读写性能，也能提高容错性。
>
> listeners=PLAINTEXT://192.168.0.213:9092  内网访问kafka
>
> advertised.listeners=PLAINTEXT://101.89.163.1:9092 外网访问kafka
>
> auto.create.topics.enable 是否允许自动创建topic
>
> unclean.leader.election.enable  fasle：只允许从isr中选取leader副本；true：可以选择非isr中的副本作为leader副本。

官方的介绍

```
https://kafka.apache.org/documentation/#configuration
```

## 6.kafka的api分类

![](.\image\1-kafka的api分类.png)

### 6.1生产者

```java
public void send() {
        Properties props = new Properties();
        // kafka集群地址，多个地址使用","分割
        props.put("bootstrap.servers", "localhost:9092");
        // 同步的策略  0[异步发送], 1[同步leader],  all[副本和leader都给同步到][-1]
        // https://betterprogramming.pub/kafka-acks-explained-c0515b3b707e
        props.put("acks", "all");
        // total time between sending a record and receiving acknowledgement from the broker.
        props.put("delivery.timeout.ms", 60000);
  			// 请求超时时间，用来控制重试;必须设置 delivery.timeout.ms > request.timeout.ms
        props.put("request.timeout.ms", 30000);
        // The producer maintains buffers of unsent records for each partition.
        props.put("batch.size", 16384);
        // 每毫秒发送一次和batch.size 配合使用
        props.put("linger.ms", 1);
        // 缓存的总内存，当产生的消息比传输的快的时候，这个内存会被快速消耗
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 5000; i++){
            // producer 的send方法本身就是一个异步的方法，所以想要知道每一条数据是否发送成功，
            // 一定要写回调方法来确认是否发送成功
            Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>("foo-1", Integer.toString(i), "messge"), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    if(e != null) {
                        // 如果这个发送异常，数据可以数据库，方便下次再发送
                        e.printStackTrace();
                    } else {
                        System.out.println("The offset of the record we just sent is: " + metadata.offset());
                    }
                }
            });
        }


        producer.close();
    }
```

### 6.2消费者

#### 自动提交

```java
public void consumerAutoCommit() {
        Properties props = new Properties();
        // 设置消费的位置 earliest[有提交offset，从提交位置，没有从头开始],latest[有提交的offset，从offset消费，等待新消息]
        props.put("auto.offset.reset","earliest");
        props.put("bootstrap.servers", "localhost:9092");
        // 消费组
        props.put("group.id", "test");
        // 开启自动提交
        props.put("enable.auto.commit", "true");
        // 每隔多少ms来提交一次offset
        props.put("auto.commit.interval.ms", "1000");
        // 每次拉取的最大条数 默认为500
        props.put("max.poll.records",500);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        // 订阅的topic[也可以订阅多个topic] 但是通常订阅一个topic
        consumer.subscribe(Arrays.asList("foo-1"));
        while (true) {
            // 在100ms内pull数据，如果没有拉取到返回空集合
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records){
                try {
                  // 模拟逻辑消费
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        }
    }
```

> 自动提交的优点：不用进行手动的提交offset，方便进行管理。
>
> 缺点：
>
> 1. 可能会重复消费消息。比如有每5秒中自动提交一次offset，并且在这期间拉取到100条消息准备消费，时间经过了4.5秒，并且这时候消费了88条数据，然后consumer挂掉了，那么下次启动的时候，前88条数据又需要被重复消费一遍了。
> 2. 可能会"丢失数据"，比如拉取到100条数据，当消费到66条数据的时候，提交时间已到，就把这100条数据的offset给提交了，但是业务处理到88条的时候异常了，那么89条-100条的数据发生了“丢失”。
>
> 所以我们一般对于比较敏感的数据不建议使用自动提交，比如财务数据等等。
>
> 自动提交造成的数据重复消费和数据丢失文章
>
> > https://cloud.tencent.com/developer/article/1786809

#### 手动提交

```java
public void consumerManualCommit() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
  			// 改成手动提交
        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("foo", "bar"));
        final int minBatchSize = 200;
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                // 比如这里有大量的业务逻辑
                buffer.add(record);
            }
          // 一般认为kafka是一个无限消费的队列，所以没有对records%minBatchSize有余数的数据继续处理
            if (buffer.size() >= minBatchSize) {
              // 如果这个提交offset失败了，可以把这200条数据给留存下来，方便分析
                consumer.commitSync();
                buffer.clear();
            }
        }
    }
```

> commitSync手动提交优点：可以人为的控制offset的提交，并且当消费失败的时候能够知道是哪些数据消费失败了，从而实现精确控制。
>
> commitSync手动提交缺点：
>
> 1. 当提交的时候，consumer阻塞的，从而影响consumer的消费效率。
> 2. 由于offset完全交给开发者来操作，这就需要开发者遵守开发的规范。比如由于先提交了offset再消费数据，从而造成这一批数据过程中发生处理逻辑发生异常，这就会造成发生异常之后的数据没有被消费掉。而又由于offset被提交而这个消费组再也消费不到了。
>

#### 关于线程安全问题

> kafka的生产者是线程安全的，但是消费者却是线程不安全的，不建议多线程来进行来对同时对一个consumer来进行消费。
>
> 一般开发中多个消费者的时候建议一个线程对应一个consumer，这样编写代码逻辑简单，并且就算出错了也容易排查。
>
> > 当然特殊情况下，比如一些对消费效率要求高的系统，可以考虑使用多个线程来对consumer来进行消费，从而提高消费的效率。

一般采取的消费策略为：

> 宁可重复消费也不要让消息遗失而消费不到。因为我们可以在消费端做“数据重复”的处理，比如把消费过的数据的key放入到redis中。

## 7.副本机制

### 7.1 kafka副本的特点

一般分布式系统的副本都会有以下的特点

> 1. 提供数据冗余；比如hadoop，elasticsearch。
> 2. 提供高伸缩性；支持横行拓展，能够通过加机器的方式来提高读操作的性能。比如elasticsearch.
> 3. 允许将数据放入与用户地理位置相近的地方，从而降低系统延时。比如sprak计算拿数据的时候，会从距离自己最近的服务器拿数据。

遗憾的是kafka中的副本只能提供`数据冗余`这一个特性。

> 因为kafka的所有的读写操作都是通过一个叫`Leader`的副本来完成的，而其他的kafka的副本是不对外提供服务的，他们只提供数据冗余。
>
> 虽然kafka的副本不对外不提供数据，但是这些副本可以从leader拉取数据并同步，从而作为leader的备胎。

副本的同步机制如下：

![](./image/9-副本复制数据机制.png)

虽然leader副不能对外提供数据服务，但是也带来了一些好处，比如：

> 1. 能够方便用户实时读取到刚才producer生产的数据；比如mysql的主从复制，从节点只用来读数据，那么有可能产生写入数据成功，但是读数据发现没有该数据。
> 2. 方便实现单调读，即不会出现一条数据一会在一会在的情况。比如当前有R1和R2两个副本，其中R1副本已经同步到数据A，但是R2并没有同步到数据A，那么就会出现请求R1副本能够获取数据A而请求R2无法获取数据A。

### 7.2 ISR(In-sync Replicas)

简单来说isr就是`leader的备胎集合`，当然leader副本本身就在这个集合里面。

满足以下条件就会让副本进入到isr集合中

> 1. replica.lag.time.max.ms;默认为10秒，当副本和能够在这个时间内保持和leader的心跳，那么就会被加入到isr中，相反会被从isr中提出去。
> 
>注意事项：
> 
>> kafka 0.9.0.0版本之前是用`replica.lag.max.messages`参数（落后于leader副本消息数）来控制副本是否能够进入isr。但是这样会造成isr中副本会频繁的进入isr或者被从isr中移除。
> >
> > 参考文章：https://developer.aliyun.com/article/918672

## 8.如何配置才能实现kafka不丢失数据呢？

消息的丢失无非有以下几种情况

> 1. 生产者发送消息了成功了，但是并没有持久化到磁盘中。比如生产者使用异步的发送。
> 2. 由于消费者的消费策略或者消费组提交offset方式不合理而造成的消费者没有消费到数据。可比如一个分区从来没有提交过offset，而消费者设置的是latest模式就有可能消费不到producer产生的数据。
> 3. kafka中的数据过期，从而让consumer无法消费到数据。

### 8.1 保障Producer一定把消息发送到broker并持久化到磁盘中

> 并且设置acks=all;这样能保证isr分区中的所有副本都能够同步到该数据。
>
> 如果只是设置acks=1,可能会因为leader副本失效而造成"数据丢失".

```java
@Override
public void send() {
        Properties props = new Properties();
        // kafka集群地址
        props.put("bootstrap.servers", "localhost:9092");
        // 同步的策略  0[异步发送], 1[同步leader],  all[副本和leader都给同步到][-1]
        // https://betterprogramming.pub/kafka-acks-explained-c0515b3b707e
        props.put("acks", "all");
        // 发送的超时时间
        props.put("delivery.timeout.ms", 60000);
        props.put("request.timeout.ms", 30000);
        // The producer maintains buffers of unsent records for each partition.
        props.put("batch.size", 16384);
        // 每毫秒发送一次和batch.size 配合使用
        props.put("linger.ms", 1);
        // 缓存的总内存，当产生的消息比传输的快的时候，这个内存会被快速消耗
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 5000; i++){
        Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>("foo-1", Integer.toString(i), "message"),
        // 使用回调函数来判断数据是否真的发送成功了
        new Callback() {
@Override
public void onCompletion(RecordMetadata metadata, Exception exception) {
        System.out.println("发送成功");
        }
        });
        }


        producer.close();
        }
```

### 8.2 Consumer端消费异常却offset提交成功

> 这个的前提是消费端设置的参数为自动提交。
>
> 比如现在一个topic有5个partition，同时有5个consumer来进行消费，并且这5个消费者提交offset的方式为自动提交。
>
> 这个时候比如有一个自动提交的时间已到，然后进行了提交，而这个时候consumer逻辑还没有处理完，这时候发送了异常，从而提交了这一批数据的offset，从而造成"消息丢失"。从表面看起来像消息丢失而已，其实是因为处理消费的策略有问题。
>
> 解决方案：
>
> 在consumer端设置参数enable.auto.commit=fasle;并采用手动提交的方式，确保消费完消息再提交。

### 8.3 主题增加分区

当增加主题的分区后，producer会优先于consumer感知到新增的分区，而这个时候consumer设置的是消费策略恰好又是`auto.offset.reset=latest`。这个时候由于consumer感知新增的分区比较慢，就会出现有一部分消息无法消费掉。

> 解决方案：
> 1.针对新增的分区开一个专门的consumer，并设置消费的策略为earlist来进行消费.
> 2.实现一个ConsumerRebalanceListener，重写onPartitionsAssigned方法，每次取offset都从数据库中读取，如果能取到则说是已有分区，否则则是新的分区，则从该分区的offset=0开始消费。

### 8.4 如果有些数据突然消费不到

比如昨天消费数据还是正常的，说明topic里面一定是有数据的；但是今天消费同样的一批数据，发现数据无法被消费到。

> 可以查看kafka中消息的过期时间，检查昨天的数据是否过期；
>
> kafka消息默认过期时间为7天；

## 9. Rebalance现象

> Rebalance现象就是分配消费者消费哪些分区的过程。

假设目前某个 Consumer Group 下有两个 Consumer，比如 A 和 B，当第三消费者 C 加入时，Kafka 会触发 Rebalance，并根据默认的分配策略重新为 A、B 和 C 分配分区，如下图所示：

![](.\image\4-rebalance.jpg)

但是rebalance会有一个缺点，即：

> 在rebalance的过程中所有的消费者都会停止工作，直到rebalance完成。
>
> 这就造成如果一个组中有很多consumer(比如几百个)，就会造成rebalance很耗时。

触发rebalance的有三个条件，满足其中任意一个就会发生。

> 1. 分区的数量变化。
> 2. 消费组中的消费者数量发生变化。
> 3. 订阅的主题数发生变化(一个consumer可以订阅多个主题)。
>
>    Consumer Group 可以使用正则表达式的方式订阅主题，比如 `consumer.subscribe(Pattern.compile(“t.*c”))` 就表明该 Group 订阅所有以字母 t 开头、字母 c 结尾的主题。在 Consumer Group 的运行过程中，你新创建了一个满足这样条件的主题，那么该 Group 就会发生 Rebalance
>
> 4. 由于网络抖动让consumer和broker之间的心跳发生了断裂，这时候无法连接心跳的consumer会被从消费组内踢出去。

如何尽可能的避免发生rebalance的发生呢？

分区数的改变，和订阅主题数的改变，这里问题我们暂时不做考虑，因为这是由于我们业务需要而发生改变而引起的我们暂时不做讨论，我们重点关注的是如何在consumer端代码层面来改善rebalance。

下面是三种方式来进行改善rebalance的情况，请**注意只是改善而已**....

第一种

> 通过设置consumer端的参数来保证防止由于网络问题，导致心跳断开而造成的consumer被从消费组剔除调。
>
> 设置session.timeout.ms 和 heartbeat.interval.ms的参数，比较推荐的数值为:
>
> - 设置 session.timeout.ms = 6s。
> - 设置 heartbeat.interval.ms = 2s。
>
> 这样设置的目的就是在consumer被判定死之前，至少发送3次心跳。从而减少由于网络抖动而造成的consumer被从消费组中踢出去。

第二种

> 由于kafka的消费逻辑太长，而造成broker认为此consumer已经死亡，从而引发的rebalance，解决方案如下:
>
> - 设置max.poll.interval.ms长一些，比如处理逻辑需要10分钟，你设置成15分钟。
> - 设置max.poll.records的最大条数小一些，从而使消费的逻辑短一些。

第三种

> consumer端频繁的发生full GC也会引发，也有可能发生rebalance。因为full gc造成jvm的stop the world，从而造成broker认为消费者已经死亡。
>
> 解决方案：
>
> > 检查consumer端是否频繁发生了full GC。

第四种

> 当consumer不指定消费具体哪个分区的时候，才会引发rebalance.所以我们可以让consumer来消费指定的分区数据。
>
> 但是此方案可能会造成某个consumer服务宕机，从而造成某个分区的数据不会被消费掉。

代码如下

```
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        // 分区编号从0开始
        TopicPartition partition=new TopicPartition("foo-1",0);
        // 订阅制定partition
        consumer.assign(Arrays.asList(partition));
```

## 10.控制器(Controller)

> 控制器组件（Controller）是kafka的核心组。它的作用是在Zookeeper的帮助下管理和协调整个kafka集群。
>
> 一个kafka集群只会有个broker能够成为控制器。

### 10.1 控制器是如何被选出来的呢？

> Broker启动的时候都会尝试去创建/controller临时节点，谁优先创建成功，哪个broker就是controller节点。

### 10.2 控制器的作用

1. 主题的管理；创建、删除、增加分区。
2. 分区重分配；比如新加的一个broker节点，由于kafka不会实现副本数据的迁移，所以可以使用重分区来实现。
3. 集群broker成员的管理，包括broker的新增，broker关闭，broker宕机。
4. 维护kafka元数据；比如副本信息元数据，分区数据元数据，当前broker存活列表等等。

### 10.3 kafka在zookeeper创建的节点

![](./image/10-kafka在zookeeper上的节点.png)

## 11. kafka和其他消息队列的对比优劣

### 11.1 kafka优势

> 1. 采用顺序写盘和0拷贝等技术实现高速写盘。kafka可以实现百万级别的的写入速度，秒杀其他mq。
> 2. kafka简单理解，它就是一个日志文件，你可以无限制的反复消费。而有些mq不支持重复消费历史数据，比如activemq。
> 3. 可以实现海量数据的堆积（因为它本身就是一个日志文件的存储），这也是为何日志收集会首选kafka的原因。
> 4. 生产和消费的最小单位为partition，可以通过增加partition的数量来增加系统的吞吐量。

### 11.2 kafka劣势

> 1. 相比于其他消费队列其本身偏重，比较消耗资源，特别是内存。
> 2. 缺失一些特性，比如事务的特性（kafka也确实有事务，但是他的事务是发送数据要不都成功，要么都失败，而不是业务上的事务）。

## 12. 市面上关于Kafka和Rocketmq对比的一些错误表述

甚至有些阿里官方描述kafka和rocketmq的对比也有迷惑性的

```
https://alibaba-cloud.medium.com/kafka-vs-rocketmq-multiple-topic-stress-test-results-d27b8cbb360f
```

《深入理解Kafka核心设计与实践原理》的测试数据如下：

发送100w每条消息的大小为1kb;并且环境配置如下：

> 1. 发送的参数设置为acks=1
> 2. 使用3台普通云主机配置分别为：内存8G，磁盘40Gb，4核主频为2600Mhz
> 3. jvm版本为1.8.0-112,linux系统版本为2.6.32-504.23.4.el6.x86_64

![](./image/7-发送性能测试.png)

消费者消费完100w条数据的性能如下：

![](./image/8-消费性能测试.png)

> 因为现实中，没有人用kafka的单点，即使是单点也没有会开256个partition。
>
> > 因为一旦开这么多的partition，肯定不用用单点的；
> >
> > 这就是rockemq测试为了测试而测试，根本不符合实际的应用场景。

其他一些关于kafka介绍的很多都是错误的文章

```
https://zhuanlan.zhihu.com/p/60196818
https://blog.csdn.net/shijinghan1126/article/details/104724407
```

## 13. 未来kafka的发展

kakfa在未来会取消zookeeper而采用KRaft

```text
https://www.infoq.com/news/2022/10/apache-kafka-kraft/
```

## 14 文章参考

- [kafka选举过程](https://cloud.tencent.com/developer/article/1790732)
- 《深入理解kafka》
- [BigData-Tutorial](https://dunwu.github.io/bigdata-tutorial/#zookeeper)



## 15.Github地址

```
https://github.com/bulingfeng/kafka
```

