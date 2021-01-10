package io.infolayer.aida.executor.util;

import io.infolayer.aida.exception.MalformedPluginException;
import io.infolayer.aida.plugin.PluginMetadata;
//import io.infolayer.siteview.plugin.PluginPrototype;
import io.infolayer.aida.plugin.PluginPrototype;

public interface IPluginParser {
	
	public PluginPrototype getPluginPrototype() throws Exception;

	public PluginMetadata getPlugin() throws MalformedPluginException;

}
