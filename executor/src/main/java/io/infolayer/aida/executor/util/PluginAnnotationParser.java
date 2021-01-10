package io.infolayer.aida.executor.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.infolayer.aida.annotation.Env;
import io.infolayer.aida.annotation.Plugin;
import io.infolayer.aida.annotation.PluginParameter;
import io.infolayer.aida.exception.MalformedPluginException;
import io.infolayer.aida.plugin.PluginMetadata;
import io.infolayer.aida.plugin.PluginParameterMetadata;
import io.infolayer.aida.plugin.PluginPrototype;


public class PluginAnnotationParser implements IPluginParser {

	private Class<?> clazz;

	public PluginAnnotationParser(Class<?> clazz) {
		this.clazz = clazz;
	}

	private List<PluginParameterMetadata> getParameters() {

		List<PluginParameterMetadata> list = new ArrayList<PluginParameterMetadata>();
		
		for (Field field : this.clazz.getDeclaredFields()) {

			if (field.isAnnotationPresent(PluginParameter.class)) {
				
				PluginParameter ann = field.getAnnotation(PluginParameter.class);
				
				list.add(new PluginParameterMetadata(
						PluginParameter.NAME.equals(ann.name()) ? field.getName() : ann.name(), 
						ann.defaultValue(), 
						ann.required(),
						false)
				);
				
			}
		}
		
		return list;
	}
	
	private String[] getClassesName(Class<?>[] classes) {
		
		if (classes != null) {
			
			String[] result = new String[classes.length];
			
			for (int i = 0; i < classes.length; i++) {
				result[i] = classes[i].getName();
			}
			
			return result;
			
		}
		
		return null;
	}
	
	private Map<String, String> getEnvironment(Env[] envs) {
		
		Map<String, String> environment = new HashMap<String, String>();
		
		if (envs != null) {
			for (Env env : envs) {
				environment.put(env.name(), env.value());
			}
		}
		
		return environment;
		
	}
	
	@Override
	public PluginMetadata getPlugin() throws MalformedPluginException {
		
		if (clazz.isAnnotationPresent(Plugin.class)) {
			Plugin plugin = clazz.getAnnotation(Plugin.class);
			return new PluginMetadata(
					plugin.name(), 
					plugin.platform(),
					plugin.description(),
					this.getParameters());
		}
		
		throw new MalformedPluginException("Class isn't a Plugin annotated class.");
	}

	@Override
	public PluginPrototype getPluginPrototype() throws Exception {
		
		PluginMetadata pm = this.getPlugin();
		Plugin plugin = clazz.getAnnotation(Plugin.class);
		
		return new PluginPrototype(
				pm, 
				this.getEnvironment(plugin.environment()), 
				this.getClassesName(plugin.inputHandler()), 
				this.getClassesName(plugin.outputHandler()), 
				plugin.timeout(), 
				clazz.getName());

	}

}
