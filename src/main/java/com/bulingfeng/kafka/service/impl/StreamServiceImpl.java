package com.bulingfeng.kafka.service.impl;

import com.bulingfeng.kafka.service.StreamService;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * @Author bulingfeng
 * @Date 2022/10/28
 * @Description 流处理逻辑
 */
@Service
public class StreamServiceImpl implements StreamService {

    @Override
    public void steam() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-stream-processing-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        builder.<String, String>stream("foo-1").mapValues(value -> String.valueOf(value.length())).to("foo-1");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
