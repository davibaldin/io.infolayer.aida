package io.infolayer.aida.executor.runnable;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.annotation.PluginParameter;
import io.infolayer.aida.entity.PluginCall;
import io.infolayer.aida.exception.MalformedPluginException;
import io.infolayer.aida.exception.PluginException;
import io.infolayer.aida.executor.util.OsgiUtils;
import io.infolayer.aida.executor.util.ParameterValueResolver;
import io.infolayer.aida.executor.util.PluginAnnotationParser;
import io.infolayer.aida.plugin.IPluginInputHandler;
import io.infolayer.aida.plugin.IPluginOutputHandler;
import io.infolayer.aida.plugin.IPluginService;
import io.infolayer.aida.plugin.IRunnableListener;
import io.infolayer.aida.plugin.IRunnablePlugin;
import io.infolayer.aida.plugin.OutputFlow;
import io.infolayer.aida.plugin.PluginMetadata;
import io.infolayer.aida.plugin.PluginParameterMetadata;
import io.infolayer.aida.plugin.PluginSubmissionResponse;
import io.infolayer.aida.utils.PlatformUtils;
// import io.infolayer.siteview.IKeychainService;
// import io.infolayer.siteview.KeychainSecret;

public abstract class AbstractRunnablePlugin implements IRunnablePlugin, IRunnableListener {

	private static Logger log = LoggerFactory.getLogger(IRunnablePlugin.class);

	/*
	 * Plugin configuration level
	 */

	private PluginMetadata plugin;
	private int timeout;
	private Map<String, String> environment;
	private IPluginOutputHandler[] outputHandlers;
	private IPluginInputHandler[] inputHandlers;
	private Set<IRunnableListener> listeners;

	/*
	 * Plugin instance level
	 */
	private String id;
	private OutputFlow outputFlow;
	private int status;
	private PluginCall call;

	public AbstractRunnablePlugin() {
		// this.id = PlatformUtils.getAlphaNumericString(3) +
		// (System.currentTimeMillis() - 1531620000000l);
		id = PlatformUtils.getAlphaNumericString(3) + (System.currentTimeMillis() - 1576359159231l); // 14/12/2019
																										// 18:32:39
		environment = new HashMap<String, String>();
		status = STATUS_NEW;
	}

	@Override
	public void configure(PluginMetadata plugin, int timeoutSeconds, IPluginInputHandler[] input,
			IPluginOutputHandler[] output) throws PluginException {

		if (plugin == null) {
			throw new IllegalArgumentException("Plugin instance cannot be null.");
		}
		
		this.plugin = plugin;
		this.inputHandlers = input;
		this.outputHandlers = output;
		
	}

	@Override
	public void configure(int timeoutSeconds, IPluginInputHandler[] input, IPluginOutputHandler[] output) throws PluginException {

		PluginAnnotationParser parser = new PluginAnnotationParser(this.getClass());
		try {
			PluginMetadata pluginMetadata = parser.getPlugin();
			this.configure(pluginMetadata, timeoutSeconds, input, output);
		} catch (MalformedPluginException e) {
			throw new IllegalArgumentException("Plugin class isn't annotated and should be cofigured with PluginMetadata.");
		}
		
	}
	
	@Override
	public PluginMetadata getPlugin() {
		return this.plugin;
	}
	
	/**
	 * Set execution to dry-run. If true, everything is executed except execute() method.
	 * @param dryRun
	 * @throws IllegalArgumentException
	 */
	public void setDryRun(boolean dryRun) throws IllegalArgumentException {
		if (this.status == STATUS_NEW) {
			this.environment.put(ENV_DRYRUN, "true");
		}else {
			throw new IllegalArgumentException("Only instance in STATUS_NEW can have DryRun changed.");
		}
	}
	
	/**
	 * Return true if environment contains true for ENV_DRYRUN property.
	 * @return
	 */
	public boolean isDryRun() {
		if (this.environment.containsKey(ENV_DRYRUN)) {
			try {
				return Boolean.parseBoolean(this.environment.get(ENV_DRYRUN));
			} catch (Exception e) { }
		}
		return false;
	}
	
	@Override
	public PluginSubmissionResponse submit(PluginCall call, Map<String, String> environment, Set<IRunnableListener> listeners, OutputFlow outputFlow) throws PluginException {
		
		this.listeners = listeners;
		
		//FIXME Possible spoofing?
		if (call != null) {
			this.call = call;
			if (call.getPluginName() != null && !call.getPluginName().equals(this.getPlugin().getName())) {
				throw new PluginException(MessageFormat.format("Unexpected plugin name {0}", call.getPluginName()));
			}
		} else {
			throw new PluginException("PluginCall cannot be null.");
		}
		
		this.validateParameters();
		
		if (outputFlow == null) {
			throw new PluginException("OutputFlow cannot be null.");
		} else {
			this.outputFlow = outputFlow;
		}
		
		if (environment != null) {
			for(String key : environment.keySet()) {
				this.environment.put(key, environment.get(key));
				
				if (key.equals(ENV_DRYRUN)) {
					try {
						this.setDryRun(Boolean.parseBoolean(environment.get(key)));
					}catch (Exception e) {
						log.warn("Found dry-run environment, expected boolean but found {}, exception: {}", environment.get(key), e.getMessage());
					}
				}
			}
		}
		
		/*
		 * Resolv Parameters 
		 */
		ParameterValueResolver.updateMap(call.getPluginParams(), this.outputFlow, this.getEnvironment(), this.getParametersValue(), null);
		
		/*
		 * Resolv Environment
		 */
		ParameterValueResolver.updateMap(this.getEnvironment(), this.outputFlow, this.getEnvironment(), this.getParametersValue(), null);
		
		//In case class is annotated, inject parameters value
		this.injectParameters();
		
		return null;
	}
	
	private void validateParameters() throws PluginException {
		
		for (PluginParameterMetadata param : this.plugin.getParameters()) {
			if (param.isRequired()) {
				if (!this.getParametersValue().containsKey(param.getName())) {
					throw new PluginException("Parameter name " + param.getName() + " is required.");
				}
			}
		}
		
	}
	
	public void lifecycleRunning() {
		status = STATUS_RUNNING;
		listen("Plugin run start. " + this.toString(), status, getInstanceID(), getPlugin().getName(), null);
	}
	
	public void lifecycleSuccess(Object result) {
		status = STATUS_SUCCESS;
		listen("Plugin run success. " + this.toString(), status, getInstanceID(), getPlugin().getName(), result);
	}
	
	public void lifecycleException(Exception e) {
		status = STATUS_EXCEPTION;
		listen("Plugin run exception. " + e.getMessage(), status, getInstanceID(), getPlugin().getName(), null);
	}
	
	public void lifecycleInterrupted() {
		status = STATUS_INTERRUPTED;
		listen("Plugin run timeout. ", status, getInstanceID(), getPlugin().getName(), null);
	}
	
	@Override
	public int getStatus() {
		return this.status;
	}
	
	public Set<IRunnableListener> getListeners() {
		return listeners;
	}
	
	public PluginCall getCall() {
		return call;
	}
	
	protected PluginParameterMetadata getParameter(String name) throws PluginException {
		
		if (plugin != null) {
			PluginParameterMetadata pm = plugin.getParameter(name);
			return pm;
		}
		
		throw new PluginException("Parameter " + name + " not found.");
	}
	
	protected String getParameterValue(String name) throws PluginException {
		
		PluginParameterMetadata param = this.getParameter(name);
		return getParametersValue().getOrDefault(name, param.getValue());
	}
	
	private void injectParameters() throws PluginException {
		
		for (Field field : this.getClass().getDeclaredFields()) {
        	
            if (field.isAnnotationPresent(PluginParameter.class)) {
            	PluginParameter ann = field.getAnnotation(PluginParameter.class);
            	
            	String value;
            	String name;
            	if (PluginParameter.NAME.equals(ann.name())) {
            		name = field.getName();
            	}else {
            		name = ann.name();
            	}
            	
            	//1st. Try from Metadata (precedence)
            	
            	if (plugin != null) {
            		
        			PluginParameterMetadata pm = plugin.getParameter(name);
        			if (pm != null) {
        				value = this.getParametersValue().getOrDefault(name, pm.getValue());
        				
        				if (pm.isRequired() && value == null) {
        					throw new PluginException("Parameter " + name + " is required but not found.");
        				}
        				
        			} else {
        				value = this.getParametersValue().getOrDefault(name, ann.defaultValue());
        			}
        			
        		} else {
        			
        			//Plugin is null, only from Annotation
        			value = this.getParametersValue().getOrDefault(name, ann.defaultValue());
        			
        		}
            	
            	setFieldValue(this, field, value);

            }
        }
		
	}
	
	private void setFieldValue(Object instance, Field field, String value) throws PluginException {
		
		boolean modified = false;
		if (!field.isAccessible()) {
			field.setAccessible(true);
			modified = true;
		}
		
		try {
			
			Class<?> clazz = field.getType();
			
			if (clazz.equals(String.class)) {
				field.set(instance, value);
				
			} else if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
				field.set(instance, Boolean.parseBoolean(value));
				
			} else if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
				field.set(instance, Integer.parseInt(value));
				
			} else if (clazz.equals(Long.class) || clazz.equals(long.class)) {
				field.set(instance, Long.parseLong(value));
				
			} else if (clazz.equals(Double.class) || clazz.equals(double.class)) {
				field.set(instance, Double.parseDouble(value));
				
			// } else if (clazz.equals(KeychainSecret.class)) {
			// 	field.set(instance, this.getSecret(value));
				
			} else {
				throw new PluginException("Type " + field.getType() + " is not a valid type for PluginParameter.");
			} 
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new PluginException(e.getMessage());
		
		}finally {
			if (modified) {
				field.setAccessible(false);
			}
		}
		
	}
		
//		this.params.clear();
//		
//		for (PluginParameter p1 : this.plugin.getParameters()) {
//			
//			String value = this.getParamValue(values, p1);
//			
//			if (log.isDebugEnabled()) {
//				if (values != null && values.containsKey(p1.getName())) {
//					log.debug("Loading parameters: {} -> {}", p1.getName(), value);
//				}else {
//					log.debug("Loading parameters with default values: {} -> {}", p1.getName(), p1.getValue());
//					
//				}
//			}
//			
//			this.params.add(new PluginParameter(p1.getName(),  value, p1.isRequired(),  p1.isCredentials()));
//			
//			if (p1.getName().equals("timeout")) {
//				try {
//					this.setTimeout(Integer.parseInt(value) + 2);
//					log.info("Parameter timeout is present. Setting execution timeout to " + this.getTimeout() + "sec.");
//				}catch (Exception e) {}
//			}
//			
//			if (p1.isCredentials()) {
//				this.addCredentials(p1, value);
//			}
//
//		}
		
//	}
	
//	private String getParamValue(Map<String, String> values, PluginParameter parameter) {
//		
//		if (values != null) {
//			if (values.containsKey(parameter.getName())) {
//				return values.get(parameter.getName());
//			}else {
//				return parameter.getValue();
//			}
//		}
//		
//		return parameter.getValue();
//	}
	
	// private KeychainSecret getSecret(String credentialsTag) {
		
	// 	try {
	// 		IKeychainService keychain = OsgiUtils.getOSGIService(IKeychainService.class, false);
	// 		if (keychain != null) {
				
	// 			if (log.isDebugEnabled()) {
	// 				log.debug("IKeychainService found.");
	// 			}
				
	// 			return keychain.getKeychainSecret(credentialsTag);
				
	// 		}else {
	// 			log.warn("keychain Service is null. Unable to translate credentials parameter.");
	// 		}
		
	// 	}catch (Exception e) {
	// 		log.warn(e.getMessage());
	// 	}
		
	// 	return null;
		
	// }
	
	public OutputFlow getOutputFlow(IRunnablePlugin instance) {
		if (instance != null) {
			outputFlow.attach(instance);
		}
		return outputFlow;
	}
	
	@Override
	public int getTimeout() {
		return this.timeout;
	}
	
	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	
	
	public Map<String, String> getEnvironment() {
		return this.environment;
	}
	
//	@Override
//	public String getPluginName() {
//		return this.plugin.getName();
//	}
	
	@Override
	public String getInstanceID() {
		return id;
	}

	@Override
	public void run() {
		
		lifecycleRunning();
		
		try {
			
			this.outputFlow.attach(this);
			this.outputFlow.addAllEnvironments(this.getEnvironment());
			
			if (this.inputHandlers != null) {
				for (IPluginInputHandler input : inputHandlers) {
					input.proccess(getPlugin(), this.getParametersValue(), getOutputFlow(this));
				}
			}
			
			/*
			 * PLUGIN RUNNING CODE
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 */
			
			Object result = null;
			if (!isDryRun()) {
				result = this.execute();
			}
			
			/*
			 * PLUGIN RUNNING CODE
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 * PLUGIN RUNNING CODE 
			 */
			
			if (this.outputHandlers != null) {
				for (IPluginOutputHandler output : outputHandlers) {
					output.proccess(result, getOutputFlow(this));
				}
			}

			if (this.call.getCall() != null) {
				
				log.debug(MessageFormat.format("Looping execution chain for plugin instance {0}", this.getInstanceID()));
				for (PluginCall innerCall : this.call.getCall()) {
					
					PluginCall innerCallClone = innerCall.clone();
					
					/*
					 * Resolv Parameters
					 */
					ParameterValueResolver.updateMap(innerCallClone.getPluginParams(), this.outputFlow, this.getEnvironment(), this.getParametersValue(), result);
					
					IPluginService service = OsgiUtils.getOSGIService(IPluginService.class, true);
					service.submit(innerCallClone, this.getEnvironment(), listeners, outputFlow);
					
				}
			}
			
			lifecycleSuccess(result);
			
		}catch (InterruptedException e) {
			
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			
			lifecycleInterrupted();
			
		} catch (Exception e) {

			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			
			lifecycleException(e);
			
		} finally {
			
			this.outputFlow.detach(this);
			this.environment = null;
			//this.params = null;
			this.listeners = null;
			this.outputHandlers = null;
			this.plugin = null;
			this.outputFlow = null;
		}
		
	}
	
	protected Map<String, String> getParametersValue() {
		return this.call.getPluginParams();
	}
	
//	protected List<PluginParameter> getParameters() {
//		return this.params;
//	}
//	
//	protected PluginParameter getParameter(String name) {
//		if (name != null) {
//			for (PluginParameter p : this.params) {
//				if (p.getName().equals(name)) {
//					return p;
//				}
//			}
//		}
//		return null;
//	}
	
	public void logInfo(String format, Object ...arguments) {
		log.info(format, arguments);
	}
	
	public void logInfo(String msg) {
		log.info(msg);
	}
	
	public void logDebug(String format, Object ...arguments) {
		log.debug(format, arguments);
	}
	
	public void logDebug(String msg) {
		log.debug(msg);
	}
	
	public void logWarn(String format, Object ...arguments) {
		log.warn(format, arguments);
	}
	
	public void logWarn(String msg) {
		log.warn(msg);
	}
	
	public void logError(String format, Object ...arguments) {
		log.error(format, arguments);
	}
	
	public void logError(String msg) {
		log.error(msg);
	}
	
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}
	
	@Override
	public void listen(String message, int status, String instanceId, String pluginName, Object instance) {
		if (this.listeners != null) {
			listeners.forEach((item) -> {
				item.listen(message, status, instanceId, pluginName, instance);
			});
			
		}
	}
	
//	@Override
//	public String getParametersHash() {
//		if (this.params != null) {
//			
//			StringBuilder builder = new StringBuilder();
//			
//			this.params.forEach((param) ->{
//				builder.append(PlatformUtils.getHash(param.getValue()));
//			});
//			
//			return PlatformUtils.getHash(builder.toString());
//			
//		}
//		return null;
//	}
	
	
	
	@Override
	public String toString() {
		return MessageFormat.format("{0}: Plugin name: {1}, Instance: {2}", 
				this.getClass().getSimpleName(),
				this.getPlugin() == null ? "<null>" : this.getPlugin().getName(),
				this.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractRunnablePlugin other = (AbstractRunnablePlugin) obj;
		return Objects.equals(id, other.id);
	}

}