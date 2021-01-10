package io.infolayer.aida.executor.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.infolayer.aida.exception.MalformedPluginException;
import io.infolayer.aida.plugin.IPluginInputHandler;
import io.infolayer.aida.plugin.IPluginOutputHandler;
import io.infolayer.aida.plugin.IRunnablePluginParser;
import io.infolayer.aida.plugin.PluginMetadata;
import io.infolayer.aida.plugin.PluginParameterMetadata;
import io.infolayer.aida.plugin.PluginPrototype;

/**
 * Parses a plugin defined using XML File format.
 * @author davi
 *
 */
public class PluginXMLFileParser implements IPluginParser {
	
	private static Logger log = LoggerFactory.getLogger(PluginXMLFileParser.class);
	
	private File file = null;
	private PluginMetadata plugin = null;
	private Element root = null;
	private Class<?> classReference = null;
	
	private class PluginRunMetadata {
		private int timeout = 0;
		private String pluginclassName = null;
		private Object configuration;
	}
	
	public PluginXMLFileParser(File file, Class<?> classReference) throws MalformedPluginException {
		
		if (file == null || !file.exists() || !file.canRead()) {
			throw new MalformedPluginException("Cannot read file.");
		}
		this.file = file;
		this.classReference = classReference;
		
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;

			dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(file);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			this.root = doc.getDocumentElement();
			
		} catch (Exception e) {
			throw new MalformedPluginException(e.getMessage());
		}	
	}
	
	private File getFile() {
		return file;
	}
	
	/**
	 * Retrieve Map<String,String> from tag env:
	 * <env name="" value="">
	 * @return
	 * @throws MalformedPluginException
	 */
	private Map<String,String> getEnvironment() throws MalformedPluginException {
		
		HashMap<String, String> env = new HashMap<String, String>();
		
		NodeList nList = root.getElementsByTagName("env");
		if (nList != null) {
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				NamedNodeMap attrs = nNode.getAttributes();
				if (attrs.getNamedItem("name") != null) {
					env.put(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("value").getNodeValue());
				}
			}
				
		}
		
		return env;
	}
	
	/**
	 * Get plugin POJO.
	 * @return
	 * @throws MalformedPluginException
	 */
	@Override
	public PluginMetadata getPlugin() throws MalformedPluginException {
		
		if (this.plugin != null) {
			return this.plugin;
		}

		String name = null;
		String platform = null;
		String version = null;
		String description = null;
		List<PluginParameterMetadata> params = null;

		try {

			name = root.getAttribute("name");
			platform = root.getAttribute("platform");
			description = root.getAttribute("description");

			// Parameters
			NodeList nList = root.getElementsByTagName("parameters");
			if (nList.getLength() > 0) {
				params = this.parseParameters(nList.item(0));
			}
			
			this.plugin = new PluginMetadata(name, platform, description, params);
			return this.plugin;

		} catch (Exception e) {
			throw new MalformedPluginException(e.getMessage());
		}

	}
	
	/**
	 * Well known tag names are: run and validate
	 * @param tagName
	 * @return
	 * @throws Exception 
	 */
	private PluginRunMetadata getRunNode(String tagName) throws Exception {
		NodeList nList = root.getElementsByTagName(tagName);
		if (nList.getLength() == 1) {

			PluginRunMetadata metadata = new PluginRunMetadata();

			NamedNodeMap attrs = nList.item(0).getAttributes();

			if (attrs.getNamedItem("timeout") != null) {
				metadata.timeout = (Integer.parseInt(attrs.getNamedItem("timeout").getTextContent().trim()));
			}

			if (attrs.getNamedItem("class") != null) {
				metadata.pluginclassName = (attrs.getNamedItem("class").getTextContent().trim());
			}
			
			if (attrs.getNamedItem("configurationParser") != null) {
				String name = (attrs.getNamedItem("configurationParser").getTextContent().trim());
				if (name != null) {
					IRunnablePluginParser configurer = OsgiUtils.createInstance(
							IRunnablePluginParser.class, this.classReference, name);
					
					Object config = configurer.parseConfiguration(nList.item(0), getFile());
					
					metadata.configuration = config;
					
				}
			}
			
			return metadata;
			
		}

		return null;
	}

	
	private String[] getInputHandlers() throws MalformedPluginException {
		String clazz = null;
		ArrayList<String> listeners = new ArrayList<String>();
		
		try {

			NodeList nList = root.getElementsByTagName("handler");
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				
				NamedNodeMap attrs = nNode.getAttributes();
				if (attrs.getNamedItem("class") != null) {
					
					clazz = attrs.getNamedItem("class").getTextContent().trim();
					
					if (classReference != null) {
						try {
							IPluginInputHandler instance = OsgiUtils.createInstance(IPluginInputHandler.class, classReference, clazz);
							//OsgiUtils.injectOsgiServices(instance);
							listeners.add(clazz);
						}catch (Exception e) {
							log.debug("Ignoring class {} not implementing IPluginInputHandler interface.", clazz);
						}
						
					}
				}
			}

		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			throw new MalformedPluginException(e.getMessage());
		}
		
		return listeners.toArray(new String[listeners.size()]);
	}
	
	private String[] getOutputHandlers() throws MalformedPluginException {
		String clazz = null;
		ArrayList<String> listeners = new ArrayList<String>();
		
		try {

			NodeList nList = root.getElementsByTagName("handler");
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				
				NamedNodeMap attrs = nNode.getAttributes();
				if (attrs.getNamedItem("class") != null) {
					
					clazz = attrs.getNamedItem("class").getTextContent().trim();
					
					if (classReference != null) {
						try {
							IPluginOutputHandler instance = OsgiUtils.createInstance(IPluginOutputHandler.class, classReference, clazz);
							//OsgiUtils.injectOsgiServices(instance);
							listeners.add(clazz);
						}catch (Exception e) {
							log.debug("Ignoring class {} not implementing IPluginOutputHandler interface.", clazz);
						}
						
					}
				}
			}

		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			throw new MalformedPluginException(e.getMessage());
		}
		
		return listeners.toArray(new String[listeners.size()]);
	}
	
	private List<PluginParameterMetadata> parseParameters(Node root) {
		if (root != null) {
			
			List<PluginParameterMetadata> params = new LinkedList<PluginParameterMetadata>();
			
			NodeList nList = root.getChildNodes();
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeName().equals("parameter")) {
					
					NamedNodeMap attrs = nNode.getAttributes();
					
					String defaultValue = null;
					if (attrs.getNamedItem("defaultValue") != null) {
						defaultValue = nNode.getAttributes().getNamedItem("defaultValue").getTextContent().trim();
					}
					
					boolean required = false;
					boolean credentials = false;
					
					if (attrs.getNamedItem("required") != null) {
						required = Boolean.parseBoolean(attrs.getNamedItem("required").getTextContent().trim());
					}
					
					if (attrs.getNamedItem("credentials") != null) {
						credentials = Boolean.parseBoolean(attrs.getNamedItem("credentials").getTextContent().trim());
					}
					
					PluginParameterMetadata p = new PluginParameterMetadata(
							attrs.getNamedItem("name").getTextContent().trim(), 
							defaultValue,
							required,
							credentials
							);
					
					params.add(p);
					
				}
			}
			
			return params;
		}
		
		return null;
	}
	
	@Override
	public PluginPrototype getPluginPrototype() throws Exception {
		
		PluginRunMetadata runMetadata = getRunNode("run");
		
		Map <String, String> env = this.getEnvironment();
		
		if (env.containsKey("plugin_resource_path")) {
			
			//Plugin file is always relative to resources.
			File resources = this.file.getParentFile().getParentFile();
			env.put("plugin_resource_path", resources.getCanonicalPath());
			
		}
		
		return new PluginPrototype(
				this.plugin, 
				env, 
				this.getInputHandlers(),
				this.getOutputHandlers(),
				runMetadata.timeout, 
				runMetadata.pluginclassName);
		
	}

}
