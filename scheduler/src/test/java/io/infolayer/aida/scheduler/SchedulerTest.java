package io.infolayer.aida.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;

import io.infolayer.aida.entity.SchedulerEntry;
import io.infolayer.aida.utils.PlatformUtils;

//@TestInstance(Lifecycle.PER_CLASS)
public class SchedulerTest {

    private SchedulerService service = null;

    public SchedulerTest() throws SchedulerException {
        service = new SchedulerService();
        service.start();
    }

    // @BeforeAll
    // public void testStart() {
    //     assertDoesNotThrow(() -> service.start());
    // }

    // @AfterAll
    // public void testStop() {
    //     assertDoesNotThrow(() -> service.stop());
    // }

    @Test
    public void testDump() {
        assertDoesNotThrow(() -> service.dump());
    }

    @Test
    public void testRemoveAll() {
        assertDoesNotThrow(() -> service.removeAllJobs());
    }

    @Test
    public void testPauseResume() {
        assertDoesNotThrow(() -> service.pause());
        assertDoesNotThrow(() -> service.resume());
    }

    @Test
    public void testAddJob() {
        SchedulerEntry entry = new SchedulerEntry();
        entry.setCronExpresssion("* 0 0 ? * * *");
        entry.setOid(PlatformUtils.getAlphaNumericString(5));
        entry.setMethod("method");
        entry.setType(SchedulerEntry.TYPE_PLAYBOOK_RUN);
        entry.setInstance("instance");

        assertDoesNotThrow(() -> service.addJob(entry));
    }

    @Test
    public void testRemoveJob() {

        String randonJobId = PlatformUtils.getAlphaNumericString(5);

        SchedulerEntry entry = new SchedulerEntry();
        entry.setCronExpresssion("0 0 * ? * * *");
        entry.setOid(randonJobId);
        entry.setMethod("method");
        entry.setType(SchedulerEntry.TYPE_PLAYBOOK_RUN);
        entry.setInstance("instance");

        assertDoesNotThrow(() -> service.addJob(entry));
        assertDoesNotThrow(() -> service.removeJob(randonJobId));
    }

}
