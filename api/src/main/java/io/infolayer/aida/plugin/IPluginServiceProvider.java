package io.infolayer.aida.plugin;

import java.util.List;
import java.util.Set;

/**
 * Interface utilizada para executar plugins do SiteView implementaados no formato OSGI.
 * Cada OSGI Bundle que implementar e registrar essa interface como um serviços disponibiliza plugins para o framework.
 * 
 * @author davi
 *
 */
public interface IPluginServiceProvider extends IPluginSubmission {
	
	/**
	 * Default execution timeout for plugins. Unit is seconds.
	 */
	public static final int DEFAULT_EXECUTION_TIMEOUT = 60;
	
	/**
	 * Return known plugins by this service provider.
	 * @return
	 */
	public List<PluginMetadata> listKnownPlugins();
	
	/**
	 * Attempt to abort an execution event ID.
	 * @param executionEventID
	 * @return
	 */
	public boolean cancel(String executionEventID);
	
	/**
	 * Shutdown Service provider and do not accept new plugin submissions. 
	 */
	public void shutdown();
	
	/**
	 * Return current execution queue.
	 * @return
	 */
	public int getQueueCount();
	
	/**
	 * Return current execution queue.
	 * @return
	 */
	public Set<String> getActiveQueue();
	
}
