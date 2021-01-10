package io.infolayer.aida.plugin;

import java.util.Map;
import java.util.Set;

import io.infolayer.aida.entity.PluginCall;
import io.infolayer.aida.exception.PluginException;

/**
 * Commom interface for plugin submission. This interface is implemented by: 
 * 		{@link IPluginService},
 * 		{@link IPluginServiceProvider} and 
 * 		{@link IRunnablePlugin} instances.
 * @author davi@infolayer.io
 *
 */
public interface IPluginSubmission {
	
	/**
	 * Submit for execution
	 * @param call Chained plugin call for execution.
	 * @param environment Environment variables required for this submission.
	 * @param statusListeners Listeners to be called.
	 * @param outputFlow OutputFlow for output processing.
	 * @throws PluginException
	 */
	public PluginSubmissionResponse submit(PluginCall call,  Map<String, String> environment,  Set<IRunnableListener> listeners,  OutputFlow outputFlow) throws PluginException;

}
