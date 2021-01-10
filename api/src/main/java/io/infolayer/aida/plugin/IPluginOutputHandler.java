package io.infolayer.aida.plugin;

import io.infolayer.aida.exception.PluginException;

/**
 * Interface for generic Plugin's output handling, if required.
 * 
 * @author davi@infolayer.io
 *
 */
public interface IPluginOutputHandler {

	public void proccess(Object result, OutputFlow flow) throws PluginException;
	
}
