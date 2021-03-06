package io.infolayer.aida.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import io.infolayer.aida.Heartbeat;

@Component
public class MessageProducer {

    private static final Logger log = LoggerFactory.getLogger(MessageProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, Heartbeat> heartbeatKafkaTemplate;

    @Value(value = "Executors.Heartbeat")
    private String heartbeatTopicName;

    @Value(value = "Executors.Sample")
    private String executorTopicName;

    public void sendHeartbeatMessage(Heartbeat heartbeat) {

        ListenableFuture<SendResult<String, Heartbeat>> future = heartbeatKafkaTemplate.send(heartbeatTopicName, heartbeat);

        future.addCallback(new ListenableFutureCallback<SendResult<String, Heartbeat>>() {

            @Override
            public void onSuccess(SendResult<String, Heartbeat> result) {
                log.info("Sent message=[" + heartbeat.getMessage() + "] with offset=[" + result.getRecordMetadata()
                    .offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Unable to send message=[" + heartbeat.getMessage() + "] due to : " + ex.getMessage());
            }
        });
    }

    // public void sendHeartbeatMessage(Heartbeat greeting) {
    //     heartbeatKafkaTemplate.send(heartbeatTopicName, greeting);
    // }
}
