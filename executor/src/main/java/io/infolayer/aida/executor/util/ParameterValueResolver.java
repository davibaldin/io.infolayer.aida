package io.infolayer.aida.executor.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.entity.PluginCall;
import io.infolayer.aida.executor.exception.BeanAdapterException;
import io.infolayer.aida.plugin.OutputFlow;
//import io.infolayer.siteview.entity.InventoryItem;
//import io.infolayer.siteview.exception.BeanAdapterException;
//import io.infolayer.siteview.repository.ILookupServiceRegistry;
//import io.infolayer.siteview.util.BeanAdapter;
//import io.infolayer.siteview.util.OsgiUtils;

/**
 * Resolves Plugin Paramenter's value for a {@link PluginCall} chain.
 * @author davi@infolayer.io
 *
 */
public class ParameterValueResolver {
	
	private static Logger log = LoggerFactory.getLogger(ParameterValueResolver.class);
	
	private static final String fieldStart = "\\$\\{";
	private static final String fieldEnd = "\\}";
	private static final String regex = fieldStart + "([^}]+)" + fieldEnd;
	private static final Pattern pattern = Pattern.compile(regex);
	
	/**
	 * Update passed parameters resolving values.
	 * @param parameters
	 * @param flow
	 * @param environment
	 * @param item
	 */
	public static void updateMap(Map<String, String> parameters, OutputFlow flow, Map<String, String> environment, Map<String, String> pluginParameters, Object item) {
		
		if (parameters != null) {
			for (Entry<String, String> entry : parameters.entrySet()) {
				entry.setValue(getValue(entry.getValue(), flow, environment, pluginParameters, item));
			}
		}
	}
	
	/**
	 * Update passed text resolving values.
	 * @param data
	 * @param flow
	 * @param environment
	 * @param parameters
	 * @param item
	 * @return
	 */
	public static String updateText(String data, OutputFlow flow, Map<String, String> environment, Map<String, String> pluginParameters, Object item) {
		if (data != null) {
			return getValue(data, flow, environment, pluginParameters, item);
		}
		return null;
	}
	
	/**
	 * Parse data and inject variables. Data is retrieved from:
	 * 		Environment ${env:propertyname:defaultvalue},
	 * 		OutputFlow  ${flow:propertyname:defaultvalue},
	 *      OutputFlow's Item {@link OutputFlow.FLOW_ITEM_NAME} ${itemname:propertyname:defaultvalue},
	 *      Item (accessed by BeanAdapter) ${item:propertyname:defaultvalue} or 
	 *      LookupEntity ${lookup:entity:filter:property:defaultvalue}
	 * 
	 * @param data
	 * @param flow
	 * @param environment
	 * @param item
	 * @return
	 */
	public static String getValue(String data, OutputFlow flow, Map<String, String> environment, Map<String, String> parameters, Object item) {
		
		if (data == null) {
			return null;
		}
		
		try {
			Matcher m = pattern.matcher(data);
			StringBuffer sb = new StringBuffer();
			
			while (m.find()) {
				
				String[] value = m.group(1).split(":");
				String replace = null;
				
				if (value.length > 1) {
					
					String defaultValue = null;
					if (value.length >= 3) {
						defaultValue = value[2];
					}
			
					switch (value[0]) {
					case "env":
						replace = returnEnviroment(environment, value[1], defaultValue);
						break;
						
					case "flow":
						replace = returnFlow(flow, value[1], defaultValue);
						break;
						
					// case "item":
					// 	replace = returnBean(item, value[1], defaultValue);
					// 	break;
						
					case "param":
						replace = returnParameter(parameters, value[1], defaultValue);
						break;
						
					// case "lookup":
					// 	replace = returnLookup(value);
					// 	break;

					default:
						if (flow != null 
							&& environment != null 
							&& value[0].equals(environment.get(OutputFlow.FLOW_ITEM_NAME))) {
							
							Object flowItem = flow.getPropoerties().get(environment.get(OutputFlow.FLOW_ITEM_NAME));
							replace = returnBean(flowItem, value[1], defaultValue);
							
						} else {
							replace = value[0];
						}
						break;
					}

				}else{
					replace = value[0];
				}
				
				if (replace != null) {
					m.appendReplacement(sb, replace);
				}
				
			}
			m.appendTail(sb);
			
			return sb.toString();
		}catch (Exception e) {
			log.error("getValue() Exception: {}", e.getMessage());
		}
		
		return null;
	}
	
	// /**
	//  * LookupEntity ${lookup:entity:filter:property:defaultvalue}
	//  * @param params
	//  * @return
	//  */
	// private static String returnLookup(String[] params) {
		
	// 	if (params.length != 5) { 
	// 		log.error("Parameter lookup error: Should have format: ${lookup:entity:filter:property:defaultvalue}");
	// 	}
		
	// 	try {
	// 		ILookupServiceRegistry lookup = OsgiUtils.getOSGIService(ILookupServiceRegistry.class, true);
			
	// 		Map<String, Object> entity = lookup.lookupEntry(params[1], params[2]);
	// 		if (entity != null && entity.containsKey(params[3])) {
	// 			return entity.get(params[3]).toString();
	// 		}
			
	// 	} catch (Exception e) {
	// 		log.error("IEntityRepository service not bound.");
	// 	}
		
	// 	return params[4];
	// }
	
	private static String returnParameter(Map<String, String> parameters, String key, String defaultValue) {
		if (parameters != null) {
			return parameters.getOrDefault(key, defaultValue);
		}
		
		return defaultValue;
	}
	
	private static String returnEnviroment(Map<String, String> environment, String key, String defaultValue) {
		if (environment != null) {
			if (environment.containsKey(key)) {
				return environment.get(key);
			}
		}
		
		return defaultValue;
	}
	
	private static String returnFlow(OutputFlow flow, String key, String defaultValue) {
		if (flow != null) {
			if (flow.getPropoerties().containsKey(key)) {
				return flow.getPropoerties().get(key).toString();
			}
			
			if ("random".equals(key)) {
				return OutputFlow.newRandomString();
			}
			
		}
		
		return defaultValue;
	}
	
	private static String returnBean(Object item, String key, String defaultValue) {
		if (item != null) {
			
			try {
				BeanAdapter adapter = BeanAdapter.getInstanceAndValidate(item);
				
				// //FIXME Improve...
				// if (item instanceof InventoryItem) {
				// 	if (!key.startsWith("document.")) {
				// 		key = "document." + key;
				// 	}
				// }
				
				Object value = adapter.getRecursiveDotName(item, key, true);
				if (value!= null) {
					return value.toString();
				}
				
			} catch (BeanAdapterException e) {
				log.error("BeanAdapterException exception: {}", e.getMessage());
			}
			
		}
		
		return defaultValue;
	}

}