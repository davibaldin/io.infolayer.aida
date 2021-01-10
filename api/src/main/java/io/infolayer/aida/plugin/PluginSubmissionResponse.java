package io.infolayer.aida.plugin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class PluginSubmissionResponse {
	
//	public static final int STATUS_SUCCESS = 0;
//	public static final int STATUS_ERROR = 1;
//	
//	private int status;
//	private long submittedTime;
//	private String pluginName;
//	private String parameterHash;
//	private String queueId;
//	private String queueName;
//	private String errorMessage;
	private Future<IRunnablePlugin> future;
	
	
	public PluginSubmissionResponse(Future<IRunnablePlugin> future) {
		this.future = future;
	}

//	public PluginSubmissionResponse(int status, String errorMessage) {
//		this.status = status;
//		this.errorMessage = errorMessage;
//	}
//
//	public PluginSubmissionResponse(int status, long submittedTime, String pluginName, String parameterHash, String queueId, String queueName,  String errorMessage) {
//		this.status = status;
//		this.submittedTime = submittedTime;
//		this.pluginName = pluginName;
//		this.parameterHash = parameterHash;
//		this.queueId = queueId;
//		this.queueName = queueName;
//		this.errorMessage = errorMessage;
//		
//	}
	
	public void waitForComplete() {
		if (future != null) {
			try {
				future.get();
			} catch (InterruptedException e) {
				future = null;
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				future = null;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
//	public Future<IRunnablePlugin> getFuture() {
//		return future;
//	}
	
//	public void setFuture(Future<IRunnablePlugin> future) {
//		this.future = future;
//	}
//
//	public int getStatus() {
//		return status;
//	}
//
//	public void setStatus(int status) {
//		this.status = status;
//	}
//
//	public long getSubmittedTime() {
//		return submittedTime;
//	}
//
//	public void setSubmittedTime(long submittedTime) {
//		this.submittedTime = submittedTime;
//	}
//
//	public String getPluginName() {
//		return pluginName;
//	}
//
//	public void setPluginName(String pluginName) {
//		this.pluginName = pluginName;
//	}
//
//	public String getParameterHash() {
//		return parameterHash;
//	}
//
//	public void setParameterHash(String parameterHash) {
//		this.parameterHash = parameterHash;
//	}
//
//	public String getQueueId() {
//		return queueId;
//	}
//
//	public void setQueueId(String queueId) {
//		this.queueId = queueId;
//	}
//
//	public String getQueueName() {
//		return queueName;
//	}
//
//	public void setQueueName(String queueName) {
//		this.queueName = queueName;
//	}
//	
//	public String getErrorMessage() {
//		return errorMessage;
//	}
//	
//	public void setErrorMessage(String errorMessage) {
//		this.errorMessage = errorMessage;
//	}

}
