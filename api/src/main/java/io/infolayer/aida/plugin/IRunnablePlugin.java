package io.infolayer.aida.plugin;

import io.infolayer.aida.exception.PluginException;

/**
 * IRunnablePlugin is the commom interface for each Plugin runnable code.
 * 
 * @author davi@infolayer.io
 *
 */
public interface IRunnablePlugin extends Runnable, IPluginSubmission {
	
	public static final int STATUS_NEW 			= 0;
	public static final int STATUS_RUNNING 		= 3;
	public static final int STATUS_SUCCESS 		= 4;
	public static final int STATUS_EXCEPTION    = 5;
	public static final int STATUS_INTERRUPTED 	= 6; //Either timeout or cancel
	public static final String ENV_DRYRUN 	= "dryRun";
	
	/**
	 * Return Plugin instance.
	 * @return
	 */
	public PluginMetadata getPlugin();
	
	/**
	 * Get execution ID.
	 * @return
	 */
	public String getInstanceID();
	
	/**
	 * Return execution instance status.
	 * @return
	 */
	public int getStatus();
	
	/**
	 * Execute the Plugin and return an arbitrary object if desired.
	 * @throws PluginException
	 */
	public Object execute() throws Exception;
	
	/**
	 * Set timeout in seconds.
	 * @param seconds
	 */
	public void setTimeout(int seconds);
	
	/**
	 * Get timeout in seconds.
	 * @return
	 */
	public int getTimeout();
	
	/**
	 * Configure IRunnablePlugin instance. 
	 * @param runanbleMetada
	 * @throws PluginException 
	 */
	public void configure(PluginMetadata plugin, int timeoutSeconds, IPluginInputHandler[] input, IPluginOutputHandler[] output) throws PluginException;
	
	/**
	 * Configure IRunnablePlugin instance. 
	 * @param runanbleMetada
	 * @throws PluginException 
	 */
	public void configure(int timeoutSeconds, IPluginInputHandler[] input, IPluginOutputHandler[] output) throws PluginException;
	
	
	// /**
	//  * Configure IRunnablePlugin instance. 
	//  * @param runanbleMetada
	//  * @throws PluginException 
	//  */
	// public void configure(Object runConfigurationMetada) throws PluginException;
	
	

}
