### [4.6 Message Delivery Semantics](https://kafka.apache.org/documentation/#semantics)(传达语义)

> Now that we understand a little about how producers and consumers work, let's discuss the semantic guarantees Kafka provides between producer and consumer. Clearly there are multiple possible message delivery guarantees that could be provided:
>
> - *At most once*—Messages may be lost but are never redelivered.
> - *At least once*—Messages are never lost but may be redelivered.
> - *Exactly once*—this is what people actually want, each message is delivered once and only once.