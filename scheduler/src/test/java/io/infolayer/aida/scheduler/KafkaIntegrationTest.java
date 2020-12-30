package io.infolayer.aida.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import io.infolayer.aida.ControlChannel;
import io.infolayer.aida.Heartbeat;
import io.infolayer.aida.entity.SchedulerEntry;

@SpringBootTest
@DirtiesContext
//@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://kafka:9092", "port=9092" })
class KafkaIntegrationTest {

    // @Autowired
    // public KafkaTemplate<String, String> template;

    @Autowired
    private KafkaTemplate<String, Heartbeat> heartbeatKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, SchedulerEntry> schedulerKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, ControlChannel> controlKafkaTemplate;

    // @Autowired
    // private KafkaSchedulerListener listener;

    @Test
    public void testSendHeartbeat() throws Exception {
        heartbeatKafkaTemplate.send("Executors.Heartbeat",
            "control", new Heartbeat("Hello test"));
        assert(true);
    }

    @Test
    public void testControlReset() throws Exception {
        controlKafkaTemplate.send("Scheduler",
            "PUT", new ControlChannel(ControlChannel.CMD_RESET_CONFIG));
        assert(true);
    }

    @Test
    public void testControlShutdown() throws Exception {
        controlKafkaTemplate.send("Scheduler",
            "PUT", new ControlChannel(ControlChannel.CMD_SHUTDOWN));
        assert(true);
    }

    @Test
    public void testScheduleTask() throws Exception {

        SchedulerEntry entry = new SchedulerEntry();
        entry.setCronExpresssion("0 * 0 ? * * *");
        entry.setMethod("method");
        entry.setInstance("instance");
        entry.setOid("oid");
        entry.setType("type");

        schedulerKafkaTemplate.send("Scheduler", 
            "PUT", entry);
    }

    @Test
    public void testRemoveScheduledTask() throws Exception {
        SchedulerEntry entry = new SchedulerEntry();
        entry.setOid("oid");

        schedulerKafkaTemplate.send("Scheduler", 
            "DELETE", entry);
    }

}
