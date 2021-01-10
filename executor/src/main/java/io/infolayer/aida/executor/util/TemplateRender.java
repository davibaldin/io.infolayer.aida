package io.infolayer.aida.executor.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.exception.PluginException;

//FIXME Better memory comsumption code
public class TemplateRender {
	
	private static Logger log = LoggerFactory.getLogger(TemplateRender.class);
	
	public static String renderText(String input, Map<String, String> parameters) throws PluginException {
		
		JtwigTemplate template = JtwigTemplate.inlineTemplate(input);
        JtwigModel model = JtwigModel.newModel();
        
        if (parameters != null) {
        	for (Entry<String, String> param : parameters.entrySet()) {
        		model.with(param.getKey(), param.getValue());
        	}
        }

		return template.render(model);
	}
	
	public static void renderFile(File source, File destination, Map<String, String> parameters) throws PluginException, IOException {
		
		if (source.exists()) {
			JtwigTemplate template = JtwigTemplate.fileTemplate(source);
	        JtwigModel model = JtwigModel.newModel();
	        
	        if (parameters != null) {
	        	for (Entry<String, String> param : parameters.entrySet()) {

					if (log.isDebugEnabled()) {
						log.debug("setting attribute " + param.getKey() + " = " + param.getValue());
					}
					model.with(param.getKey(), param.getValue());
				}
	        }

			template.render(model, new FileOutputStream(destination));
		}
	}

}
