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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import io.infolayer.aida.ControlChannel;
import io.infolayer.aida.entity.SchedulerEntry;
import io.infolayer.aida.exception.SchedulerJobException;

@Component
@KafkaListener(topics = "Scheduler", groupId = "scheduler")
public class KafkaSchedulerListener {

	private static final Logger log = LoggerFactory.getLogger(SchedulerApplication.class);

    @Autowired
	SchedulerService service;
	
	@Autowired
	ConfigurableApplicationContext context;

	@KafkaHandler
	public void listenForControl(
		@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
		@Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
		@Payload ControlChannel control) {

		if (control == null) {
			log.warn("Ignoring received message with null control data.");
			return;
		}

		switch (control.getCommand()) {

			case ControlChannel.CMD_RESET_CONFIG:
				service.removeAllJobs();
				break;

			case ControlChannel.CMD_SHUTDOWN:
				log.info("Scheduler service received shutdown message.");
				service.stop();
				//int exitCode = SpringApplication.exit(context, (ExitCodeGenerator) () -> 0);
				System.exit(0);
				break;

			default:
				log.info("Ignoring non implemented control command {}.", control.getCommand());

		}


	}

    @KafkaHandler
	public void listenForScheduler(
		@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
		@Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
		@Payload SchedulerEntry entry) {

		switch (key) {
			case "PUT":
				try {
					service.addJob(entry);
					log.info("Created new schedule for {}. Expression is {}.", entry.getOid(), entry.getCronExpresssion());
				} catch (SchedulerJobException e) {
					log.error("Exception while adding new schedule: {}", e.getMessage());
				}	
				break;

			case "DELETE":
				service.removeJob(entry.getOid());
				log.info("Removed schedule {}.", entry.getOid());
				break;
		
			default:
				log.info("Received non supported control key {}.", key);
				break;
		}

	}

	@KafkaHandler(isDefault = true)
	public void listenForSchedulerDefault(
		@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
		@Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
		@Payload Object entry) {

			log.warn("Received and discarded unknown object type {}", entry.getClass().getName());
	}
    
}
