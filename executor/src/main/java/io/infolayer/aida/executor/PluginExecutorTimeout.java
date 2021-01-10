package io.infolayer.aida.executor;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.executor.PluginPoolExecutorService.PoolEntry;

/**
 * Watch for running plugins and cancel an instance due timeout.
 * @author davi@infolayer.io
 *
 */
public class PluginExecutorTimeout implements Runnable {
	
	private static Logger log = LoggerFactory.getLogger(PluginExecutorTimeout.class);
	private final ConcurrentHashMap<String, PoolEntry> submittedTasks;
	
	protected PluginExecutorTimeout(ConcurrentHashMap<String, PoolEntry> submittedTasks) {
		this.submittedTasks = submittedTasks;
	}

	@Override
	public void run() {
		try {
			
			do {
				
				long now = System.currentTimeMillis();
				
				if (submittedTasks != null) {
					Iterator<Entry<String, PoolEntry>> iterator = 
							submittedTasks.entrySet().iterator();
					
					while (iterator.hasNext()) {
						PoolEntry entry = iterator.next().getValue();
						
						if (entry.getDue() > 0 && now > entry.getDue()) {
							if (entry.getPlugin() != null && entry.getFuture() != null) {
								log.info("IRunnablePlugin instance {} cancelled due timeout {}sec.", entry.getPlugin().getInstanceID(), entry.getPlugin().getTimeout());
								entry.getFuture().cancel(true);
							}
						}
						
						if (entry.getFuture().isCancelled() || entry.getFuture().isDone()) {
							try {
								log.debug("IRunnablePlugin instance {} cleanup.", entry.getPlugin().getInstanceID());
								entry.setPlugin(null);
								entry.setFuture(null);
								iterator.remove();
							}catch (Exception e) {
								if (log.isDebugEnabled()) {
									e.printStackTrace();
								}
								log.warn("Unexpected exception while cleaning up: {}", e.getMessage());
							}
							

						}
						
					}
					
				}
				
				//FIXME Hardcode
				Thread.sleep(1000);
				
			}while (true);
			
		} catch (InterruptedException e) {
			log.info("PluginExecutorTimeout interrupted."); 
		}
	}

}