package io.infolayer.aida.plugin;

import java.util.Map;

import io.infolayer.aida.exception.PluginException;

/**
 * Interface for generic Plugin's input handling, if required.
 * @author davi@infolayer.io
 *
 */
public interface IPluginInputHandler {

	public void proccess(PluginMetadata plugin, Map<String, String> params, OutputFlow flow) throws PluginException;
	
}
