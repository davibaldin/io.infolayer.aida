package io.infolayer.aida.executor;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.plugin.IPluginExecutorService;
import io.infolayer.aida.plugin.IRunnablePlugin;

public class PluginPoolExecutorService implements IPluginExecutorService {
	
	public class PoolEntry {
		private Future<?> future;
		private IRunnablePlugin plugin;
		private long due;

		public Future<?> getFuture() {
			return future;
		}

		public void setFuture(Future<?> future) {
			this.future = future;
		}

		public IRunnablePlugin getPlugin() {
			return plugin;
		}

		public void setPlugin(IRunnablePlugin plugin) {
			this.plugin = plugin;
		}

		public long getDue() {
			return due;
		}

		public void setDue(long due) {
			this.due = due;
		}
		
	}
	
	private static Logger log = LoggerFactory.getLogger(PluginPoolExecutorService.class);
	private final ThreadPoolExecutor execService;
	private final ConcurrentHashMap<String, PoolEntry> submittedTasks;
	private boolean first = true;
	
	public PluginPoolExecutorService(int corePoolSize, int maxPoolSize) {
		submittedTasks = new ConcurrentHashMap<String, PoolEntry>();
		this.execService = new ThreadPoolExecutor(
				corePoolSize, 
				maxPoolSize, 
				1000, 
				TimeUnit.MILLISECONDS, 
				new LinkedBlockingQueue<Runnable>());
		log.info("New PluginPoolExecutorService created: corePoolSize = {}, maxPoolSize = {}",
				corePoolSize, maxPoolSize);
		
	}
	
	public void setPoolName(String poolName) {
		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
				.setNameFormat("pool-" + poolName + "-thread-%d")
				.build();
		this.execService.setThreadFactory(namedThreadFactory);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Future<IRunnablePlugin> submit(IRunnablePlugin plugin) {
		
		PoolEntry entry = new PoolEntry();
		entry.setPlugin(plugin);
		
		//At very first submission, install PluginExecutorTimeout
		if (first) {
			synchronized (execService) {
				if (first) {
					execService.submit(new PluginExecutorTimeout(submittedTasks));
					first = false;
				}
			}
		}
		
		/*
		 * 1. Submit to working queue
		 */
		Future<IRunnablePlugin> task = (Future<IRunnablePlugin>) execService.submit(plugin);
		entry.setFuture(task);
		if (plugin.getTimeout() > 0) {
			entry.setDue(System.currentTimeMillis() + plugin.getTimeout() * 1000);
		}else {
			entry.setDue(0);
		}
		
		/*
		 * 2. Register the task and its due date
		 */
		submittedTasks.put(plugin.getInstanceID(), entry);
		
//		/*
//		 * 3. register a Thread watcher 
//		 */
//		if (plugin.getTimeout() > 0) {
//			ThreadExecutorWatcher watcher = new ThreadExecutorWatcher(task, this.timeout, plugin.getInstanceID(), this);
//			this.execService.submit(watcher);
//		}
		
//		if (log.isDebugEnabled()) {
//			log.debug("Submitted plugin for execution: " + plugin.toString() + ", task is: " + task.toString() + ", timeout (ms) = " + this.timeout);
//		}
		
		return task;
	}
	
	@Override
	public void shutdown() {
		this.execService.shutdownNow();
	}
	
	@Override
	public int getRunningCount() {
		return submittedTasks.size();
	}

	@Override
	public Set<String> getRunningInstances() {
		Set<String> instances = new HashSet<String>();
	
		Enumeration<String> enu = submittedTasks.keys();
		
		while (enu.hasMoreElements()) {
			String key = enu.nextElement();
			instances.add(key);
		}
	
		return instances;
	}
	
	@Override
	public boolean cancel(String instanceId) {
		
		if (instanceId == null) {
			return false;
		}
		
		PoolEntry entry = submittedTasks.get(instanceId);
		
		if (entry != null) {
			if (!entry.getFuture().isCancelled() && !entry.getFuture().isDone()) {
				log.debug("Cancelling task {}", instanceId);
				if(entry.getFuture().cancel(true)) {
					log.debug("Removing task {}", instanceId);
					submittedTasks.remove(instanceId);
					entry.setPlugin(null);
					entry.setFuture(null);
					entry = null;
					return true;
				}
			}
		}
		
		return false;
		
	}
	
}
