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

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.infolayer.aida.ISchedulerService;
import io.infolayer.aida.entity.SchedulerEntry;
import io.infolayer.aida.exception.SchedulerJobException;
import io.infolayer.aida.utils.PlatformUtils;

@Component
public class SchedulerService implements ISchedulerService {

	private final Logger log = LoggerFactory.getLogger(SchedulerService.class);
	private Scheduler scheduler;

	public void start() throws SchedulerException {

		if (scheduler == null) {
			scheduler = new StdSchedulerFactory().getScheduler();
		}
		
		if (!scheduler.isStarted()) {
			scheduler.start();
			log.info("Started SchedulerService");
		}else {
			log.info("Scheduler already started");
			scheduler.clear();
		}
			
	}

	public void stop() {
		try {
			if (scheduler != null && scheduler.isStarted()) {
				scheduler.shutdown();
				scheduler = null;
			}
			log.info("Stopped SchedulerService");

		} catch (SchedulerException e) {
			log.error("Exception While stopping SchedulerService: {}", e.getMessage());
		}
	}
	
	@Override
	public void pause() {
		try {
			scheduler.pauseAll();
		} catch (SchedulerException e) {
			log.error("Exception While pausing Scheduler: {}", e.getMessage());
		}
	}
	
	@Override
	public void resume() {
		try {
			scheduler.resumeAll();
		} catch (SchedulerException e) {
			log.error("Exception While pausing Scheduler: {}", e.getMessage());
		}
		
	}
	
	@Override
	public void dump() {
		try {
			this.getScheduler();
			System.out.println("       Scheduler name: " + scheduler.getSchedulerName());
			System.out.println("Scheduler instance id: " + scheduler.getSchedulerInstanceId());
			System.out.println("        Running queue: " + scheduler.getCurrentlyExecutingJobs().size());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			log.error("Exception While dumping SchedulerService: {}", e.getMessage());
		}
	}

	@Override
	public synchronized String addJob(SchedulerEntry job) throws SchedulerJobException {

		if (job == null) {
			throw new SchedulerJobException("Invalid SchedulerEntry null.");
		}

		if (job.getOid() == null || "".equals(job.getOid())) {
			String id = PlatformUtils.getAlphaNumericString(5);
			job.setOid(id);
		}
		
		if (job.getCronExpresssion() == null) {
			throw new SchedulerJobException("Job must have cron expression");
		}else {
			if (!CronExpression.isValidExpression(job.getCronExpresssion())) {
				throw new SchedulerJobException("Cron expression is not valid: '" + job.getCronExpresssion() + "'");
			}
		}
		
		if (job.getType() == null) {
			throw new SchedulerJobException("Job must have type");
		}
		
		if (job.getInstance() == null) {
			throw new SchedulerJobException("Job must have instance");
		}

		try {
			this.loadJobIntoScheduler(job);
			
		} catch (Exception e) {
			
			//In case of error, remove the job.
			this.removeJob(job.getOid());
			
			log.error("Exception while adding job to SchedulerService: {}", e.getMessage());
		}

		return job.getOid();
	}

	@Override
	public void removeJob(String jobId) {
		
		if (jobId != null) {
			JobKey key = this.getJobKey(jobId);
			if (key != null) {
				
				try {
					scheduler.deleteJob(key);
					
				} catch (Exception e) {
					log.error("Exception While removing job from SchedulerService: {}", e.getMessage());
				}
			}
			
		}

	}

	@Override
	public List<SchedulerEntry> listJobs() throws SchedulerJobException {

		try {

			Scheduler scheduler = this.getScheduler();

			scheduler.getJobKeys(null).forEach((job) -> {
				System.out.println("TODO get job " + job.getName());
			});
		
		} catch (Exception e) {
			log.error("Exception While listing jobs from SchedulerService: {}", e.getMessage());
		}

		return null;
	}

	@Override
	public Date getNextExecutionDateJob(String jobOid) {
		
		try {
			
			JobKey key = this.getJobKey(jobOid);
			
			if (key != null) {
				List<? extends Trigger> triggers = scheduler.getTriggersOfJob(key);
				if (triggers != null && triggers.size() > 0) {
					return triggers.get(0).getNextFireTime();
				}
			}

		} catch (Exception e) {
			log.error("Exception while getNextExecutionDateJob() " + e.getMessage());
		}
		
		return null;
	}
	
	@Override
	public Date getNextExecutionDateExpression(String cronExpression, Date reference) {
		try {
			CronExpression cron = new CronExpression(cronExpression);
			if (reference == null) {
				reference = new Date();
			}
			return cron.getNextValidTimeAfter(new Date());
		} catch (ParseException e) {
			log.error("Exception while getNextExecutionDateExpression() " + e.getMessage());
		}
		
		return null;
	}
	
	private JobKey getJobKey(String uuid) {
		try {
			for (String groupName : scheduler.getJobGroupNames()) {

				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
					
					if (jobKey.getName().equals(uuid)) {
						return jobKey;
					}
				}
			}

		} catch (Exception e) {
			log.error("Exception while listing jobs: {}", e.getMessage());
		}
		
		return null;
	}
	
	
	private void loadJobIntoScheduler(SchedulerEntry job) throws SchedulerJobException {
		
		this.getScheduler();
		
		JobDetail jobDetail = null;
		
		if (this.getJobKey(job.getOid()) == null) {
			
			if (SchedulerEntry.TYPE_PLAYBOOK_RUN.equals(job.getType())) {
				jobDetail = JobBuilder
						.newJob(PlaybookRunJobAdapter.class)
						.withIdentity(job.getOid()).build();
				
				jobDetail.getJobDataMap().put("instance", job.getInstance());
				
				
			} else if (SchedulerEntry.TYPE_MESH_PUSH.equals(job.getType())) {
				jobDetail = JobBuilder
						.newJob(MeshPushJobAdapter.class)
						.withIdentity(job.getOid()).build();
				
				jobDetail.getJobDataMap().put("instance", job.getInstance());
				jobDetail.getJobDataMap().put("method", job.getMethod());
				
			} else {
				
				throw new SchedulerJobException("Unknown job type " + job.getType());
			}

			Trigger trigger = TriggerBuilder
					.newTrigger()
					.withIdentity(job.getOid())
					.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpresssion())).build();
			
			try {
				
				if (this.scheduler.isStarted()) {
					this.scheduler.scheduleJob(jobDetail, trigger);
				}else {
					log.warn("Scheduler is not started.");
				}

			} catch (Exception e) {
				throw new SchedulerJobException("Job submission exception: " + e.getMessage());
			}
		}else {
			log.warn("Job id {} already loaded into scheduler.", job.getOid());
		}
		
	}
	
	private Scheduler getScheduler() throws SchedulerJobException {
		if (this.scheduler != null) {
			return this.scheduler;
		}
		
		throw new SchedulerJobException("Scheduler is not started or startup error.");
	}

	@Override
	public void removeAllJobs() {
		try {
			this.getScheduler().clear();
		} catch (Exception e) {
			log.error("Exception while removing all jobs: {}", e.getMessage());
		}
	}

}