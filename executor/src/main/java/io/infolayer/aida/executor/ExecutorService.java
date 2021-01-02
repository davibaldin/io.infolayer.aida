package io.infolayer.aida.executor;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = ExecutorService.class)
public class ExecutorService {

	private static Logger log = LoggerFactory.getLogger(ExecutorService.class);

	public ExecutorService() {

	}
	
	@Activate
	public void start(BundleContext ctx) throws Exception {
		log.info("Starting Plugin service");
		log.info("Started");
	}
	
	@Deactivate
	public void stop(BundleContext ctx) {
		log.info("Plugin service stopped.");
	}
	
}
