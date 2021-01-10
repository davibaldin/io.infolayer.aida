package io.infolayer.aida.executor;

import org.junit.jupiter.api.Test;

public class TestKafkaClient {

    public static void main(String[] args) {
        TestKafkaClient c = new TestKafkaClient();
        c.testConnectionAndPool();
    }

    @Test
    public void sendCommand() {
        SimpleKafkaProducer producer = new SimpleKafkaProducer();
        producer.send("Executors.Sample1", "some key", "some value");
        producer.close();
    }
    
    @Test
    public void testConnectionAndPool() {
        KafkaClient client = new KafkaClient("teste", "Executors.Sample1");
        client.listen();
        client.close();
    }
}