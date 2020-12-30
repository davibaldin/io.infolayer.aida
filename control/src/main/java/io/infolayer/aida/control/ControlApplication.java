package io.infolayer.aida.control;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.KafkaAdmin;

@SpringBootApplication
@EnableAutoConfiguration
public class ControlApplication {

	private static final Logger log = LoggerFactory.getLogger(ControlApplication.class);

	@Autowired
	private static KafkaAdmin kafkaAdmin;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ControlApplication.class, args);

		MessageListener listener = context.getBean(MessageListener.class);

		log.info("-------------- AIDA --------------");
		log.info("Starting io.infolayer.aida Control Service.");
		log.info("-------------- AIDA --------------");

		// try {
		// 	//listener.latch.await(10, TimeUnit.SECONDS);
		// } catch (InterruptedException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }

		log.info("Creating base Service Bus topics.");

	}

}
