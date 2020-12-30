package io.infolayer.aida.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import io.infolayer.aida.Heartbeat;

@SpringBootApplication
@EnableAutoConfiguration
public class ExecutorApplication {

	private static final Logger log = LoggerFactory.getLogger(ExecutorApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ExecutorApplication.class, args);

		log.info("Starting io.infolayer.aida Executor Service.");
		log.info("Specializing service for SAMPLE. Service BUS Topic is Executors/Sample.");

		MessageProducer producer = context.getBean(MessageProducer.class);

		log.info("Sending heartbeat message...");
		producer.sendHeartbeatMessage(new Heartbeat(Heartbeat.SERVICE_STARTING, "Executors/Sample reporting for duty.", "sample-key-master"));

		



	}

}
