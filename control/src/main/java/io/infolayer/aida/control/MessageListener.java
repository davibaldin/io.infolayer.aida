package io.infolayer.aida.control;

import java.util.concurrent.CountDownLatch;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.infolayer.aida.Heartbeat;

@Component
public class MessageListener {

    public CountDownLatch latch = new CountDownLatch(3);

    private CountDownLatch partitionLatch = new CountDownLatch(2);

    private CountDownLatch filterLatch = new CountDownLatch(2);

    private CountDownLatch greetingLatch = new CountDownLatch(1);

    @KafkaListener(topics = "Executors.Heartbeat", groupId = "control", containerFactory = "controlKafkaListenerContainerFactory")
    public void listenGroupControlHeartbeat(Heartbeat heartbeat) {
        System.out.println("Received Message in group 'control': " + heartbeat.getMessage());
        //latch.countDown();
    }

    // @KafkaListener(topics = "${message.topic.name}", groupId = "bar", containerFactory = "barKafkaListenerContainerFactory")
    // public void listenGroupBar(String message) {
    //     System.out.println("Received Message in group 'bar': " + message);
    //     latch.countDown();
    // }

    // @KafkaListener(topics = "${message.topic.name}", containerFactory = "headersKafkaListenerContainerFactory")
    // public void listenWithHeaders(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
    //     System.out.println("Received Message: " + message + " from partition: " + partition);
    //     latch.countDown();
    // }

    // @KafkaListener(topicPartitions = @TopicPartition(topic = "${partitioned.topic.name}", partitions = { "0", "3" }), containerFactory = "partitionsKafkaListenerContainerFactory")
    // public void listenToPartition(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
    //     System.out.println("Received Message: " + message + " from partition: " + partition);
    //     this.partitionLatch.countDown();
    // }

    // @KafkaListener(topics = "${filtered.topic.name}", containerFactory = "filterKafkaListenerContainerFactory")
    // public void listenWithFilter(String message) {
    //     System.out.println("Received Message in filtered listener: " + message);
    //     this.filterLatch.countDown();
    // }

    // @KafkaListener(topics = "${greeting.topic.name}", containerFactory = "greetingKafkaListenerContainerFactory")
    // public void greetingListener(Greeting greeting) {
    //     System.out.println("Received greeting message: " + greeting);
    //     this.greetingLatch.countDown();
    // }

}
