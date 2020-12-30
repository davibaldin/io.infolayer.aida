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
package io.infolayer.aida;

import java.util.Date;
import java.util.List;

import io.infolayer.aida.entity.SchedulerEntry;
import io.infolayer.aida.exception.SchedulerJobException;

/**
 * Define how to handle scheduled jobs.
 */
public interface ISchedulerService {

	/**
	 * Print to Stdout debug information.
	 */
	public void dump();
	
	/**
	 * Add Job into in-memory scheduler.
	 * @param job
	 * @return
	 * @throws SchedulerJobException
	 */
	public String addJob(SchedulerEntry job) throws SchedulerJobException;
	
	/**
	 * Remove Job from in-memory scheduler.
	 * @param jobId
	 */
	public void removeJob(String jobId);
	
	/**
	 * List known scheduled jobs into in-memory scheduler.
	 * @return
	 * @throws SchedulerJobException
	 */
	public List<SchedulerEntry> listJobs() throws SchedulerJobException;
	
	/**
	 * Retrive the next execution date of the job.
	 * @param jobOid
	 * @return
	 */
	public Date getNextExecutionDateJob(String jobOid);
	
	/**
	 * Retrive the next execution date of a cron expression. 
	 * @param cronExpression
	 * @param reference
	 * @return
	 */
	public Date getNextExecutionDateExpression(String cronExpression, Date reference);
	
	/**
	 * Remove all jobs from in-memory scheduler.
	 */
	public void removeAllJobs();

	/**
	 * Pause triggering jobs.
	 */
	public void pause();
	
	/**
	 * Resume triggering jobs.
	 */
	public void resume();

}
