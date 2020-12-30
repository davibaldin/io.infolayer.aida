package io.infolayer.aida.scheduler;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import io.infolayer.aida.ControlChannel;
import io.infolayer.aida.Heartbeat;
import io.infolayer.aida.entity.SchedulerEntry;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value(value = "${server.address}")
    private String bootstrapAddress;

    // /*
    //  * PRODUCER CODE 
    //  */

    @Bean
    public ProducerFactory<String, Heartbeat> heartbeatProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, SchedulerEntry> schedulerProducerFactory2() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, ControlChannel> schedulerProducerFactory3() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Heartbeat> heartbeatKafkaTemplate() {
        return new KafkaTemplate<>(heartbeatProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, SchedulerEntry> schedulerKafkaTemplate2() {
        return new KafkaTemplate<>(schedulerProducerFactory2());
    }

    @Bean
    public KafkaTemplate<String, ControlChannel> schedulerKafkaTemplate3() {
        return new KafkaTemplate<>(schedulerProducerFactory3());
    }

    // /*
    //  * CONSUMER CODE 
    //  */
    //application.properties configured

}