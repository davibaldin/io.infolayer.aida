package io.infolayer.aida.executor.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.plugin.IRunnableListener;

public class SimpleLogListener implements IRunnableListener {
	
	private static Logger log = LoggerFactory.getLogger(SimpleLogListener.class);
	
	@Override
	public void listen(String message, int status, String instanceId, String pluginName, Object instance) {
		log.info("Status: {}, Message: {}", status, message);
	}

}
