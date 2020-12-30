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
package io.infolayer.aida.entity;

public class SchedulerEntry {
	
	public static final String TYPE_PLAYBOOK_RUN = "playbook-run";
	
	public static final String TYPE_MESH_PUSH = "mesh-push";
	
	private String oid;
	private String cronExpresssion;
	private String type;
	private String instance;
	private String method;

	public String getOid() {
		return oid;
	}
	
	public void setOid(String oid) {
		this.oid = oid;
	}
	
	public String getCronExpresssion() {
		return cronExpresssion;
	}

	public void setCronExpresssion(String cronExpresssion) {
		this.cronExpresssion = cronExpresssion;
	}
	
	public String getInstance() {
		return instance;
	}
	
	public String getType() {
		return type;
	}
	
	public void setInstance(String instance) {
		this.instance = instance;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}

}