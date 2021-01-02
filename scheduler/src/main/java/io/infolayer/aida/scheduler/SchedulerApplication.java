/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.infolayer.aida.scheduler;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import io.infolayer.aida.Heartbeat;

@SpringBootApplication
@EnableAutoConfiguration
public class SchedulerApplication {

	private static final Logger log = LoggerFactory.getLogger(SchedulerApplication.class);

	public static void main(String[] args) {

		ConfigurableApplicationContext context = SpringApplication.run(SchedulerApplication.class, args);
		log.info("Starting io.infolayer.aida Scheduler Service.");

		SchedulerService service = context.getBean(SchedulerService.class);
		try {
			service.start();
		} catch (SchedulerException e) {
			log.error("Scheduler service Exception: ", e.getMessage());
			int exitCode = SpringApplication.exit(context, (ExitCodeGenerator) () -> 1);
			System.exit(exitCode);
		}

		log.info("Sending heartbeat message to controller...");
		HeartbeatMessageProducer producer = context.getBean(HeartbeatMessageProducer.class);
		producer.sendHeartbeatMessage(
				new Heartbeat(Heartbeat.SERVICE_STARTING, "Executors/Sample reporting for duty.", "sample-key-master"));

		log.info("Listening to topic: Scheduler");
		KafkaSchedulerListener listener = context.getBean(KafkaSchedulerListener.class);
		listener.toString();

	}

}