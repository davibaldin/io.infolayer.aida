package io.infolayer.aida.plugin;

import java.util.HashMap;
import java.util.Map;

public class PluginPrototype {
	
	private PluginMetadata plugin;
	private Map<String, String> environment;
	private String[] inputHandlerClasses;
	private String[] outputHandlerClasses;
	private int timeout = 0;
	private String pluginClassName = null;
	
	public PluginPrototype(PluginMetadata plugin, Map<String, String> environment, String[] inputHandlerClasses, String[] outputHandlerClasses, int timeout,
			String pluginClassName) {
		this.plugin = plugin;
		this.environment = environment;
		this.inputHandlerClasses = inputHandlerClasses;
		this.outputHandlerClasses = outputHandlerClasses;
		this.timeout = timeout;
		this.pluginClassName = pluginClassName;
	}

	public Map<String, String> cloneEnvironment() {
		return new HashMap<String, String>(environment);
	}
	
	public PluginMetadata clonePlugin() {
		PluginMetadata p = null;
		try {
			p = plugin.clone();
		} catch (CloneNotSupportedException e) { }
		return p;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	public String getPluginClassName() {
		return pluginClassName;
	}

	
	
}
