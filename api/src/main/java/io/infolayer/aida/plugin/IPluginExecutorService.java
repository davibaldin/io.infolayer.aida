package io.infolayer.aida.plugin;

import java.util.Set;
import java.util.concurrent.Future;

/**
 * Interface used to execute plugins.
 * @author davi@infolayer.io
 *
 */
public interface IPluginExecutorService {
	
	/**
	 * Submit for execution.
	 * @param plugin
	 * @return 
	 */
	public Future<IRunnablePlugin> submit(IRunnablePlugin plugin);

	
	/**
	 * Shutdown service. Cancel all running instances.
	 */
	public void shutdown();
	
	/**
	 * Return running queue length.
	 * @return
	 */
	public int getRunningCount();
	
	/**
	 * Return a list of running instances id.
	 * @return
	 */
	public Set<String> getRunningInstances();
	
	/**
	 * Cancel a running instance id.
	 * @param instanceId
	 * @return 
	 */
	public boolean cancel(String instanceId);

}