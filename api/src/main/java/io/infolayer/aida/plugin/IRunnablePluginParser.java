package io.infolayer.aida.plugin;

import java.io.File;

import org.w3c.dom.Node;

import io.infolayer.aida.exception.MalformedPluginException;

public interface IRunnablePluginParser {
	
	public Object parseConfiguration(Node xmlNode, File pluginXmlFile) throws MalformedPluginException;

}
