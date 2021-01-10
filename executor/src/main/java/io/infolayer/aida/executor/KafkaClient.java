package io.infolayer.aida.executor;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;


public class KafkaClient {

    private String group;
    private String topic;
    private KafkaConsumer<String, String> consumer;
    private boolean open;

    public KafkaClient(String group, String topic) {
        this.group = group;
        this.topic = topic;
        this.open = false;

        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("group.id", group);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topic));
        System.out.println("Subscribed to topic " + topic);
        this.open = true;
    }

    public void listen() {
        while (open) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records)
                System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());
        }
    }

    public void close() {
        open = false;
        consumer.close();
    }

}
