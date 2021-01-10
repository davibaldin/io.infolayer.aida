package io.infolayer.aida.executor;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.annotation.Plugin;
import io.infolayer.aida.entity.PluginCall;
import io.infolayer.aida.exception.MalformedPluginException;
import io.infolayer.aida.exception.PluginException;
import io.infolayer.aida.executor.util.IPluginParser;
import io.infolayer.aida.executor.util.JarFileUtil;
import io.infolayer.aida.executor.util.OsgiUtils;
import io.infolayer.aida.executor.util.PluginAnnotationParser;
import io.infolayer.aida.plugin.IPluginServiceProvider;
import io.infolayer.aida.plugin.IRunnableListener;
import io.infolayer.aida.plugin.IRunnablePlugin;
import io.infolayer.aida.plugin.OutputFlow;
import io.infolayer.aida.plugin.PluginMetadata;
import io.infolayer.aida.plugin.PluginSubmissionResponse;
import io.infolayer.aida.utils.PlatformUtils;
import io.infolayer.siteview.exception.PluginServiceProviderException;
import io.infolayer.siteview.plugin.PluginPrototype;
import io.infolayer.siteview.plugin.listener.StatusEventListener;
import io.infolayer.siteview.plugins.util.PluginXMLFileParser;

public abstract class AbstractPluginServiceProvider implements IPluginServiceProvider {

	private Logger log = null;
	private Map<String, PluginPrototype> pluginCache = null;
	private PluginPoolExecutorService threadPool = null;
	
	private Class<?> concreteFactoryClass;

	public AbstractPluginServiceProvider() {
		this.log = LoggerFactory.getLogger(this.getClass());
		this.pluginCache = new ConcurrentHashMap<String, PluginPrototype>();
		this.threadPool = new PluginPoolExecutorService(10, 100);
	}
	
	@Override
	public void scanBundle(Bundle bundle, Class<?> classRef, String workingDirectory) throws PluginServiceProviderException {
		
		this.concreteFactoryClass = classRef;
	
		if (bundle == null) {
			throw new PluginServiceProviderException("Atempt to scan a null Bundle. If you are you testing it, create a FakeBundle first.");
		}

		log.info("Scanning Bundle: " + bundle.getSymbolicName() + ", Source File: " + bundle.getLocation().toString());

		if (bundle.getLocation() == null || bundle.getLocation().equals("")) {
			throw new PluginServiceProviderException("Cannot work with a Bundle returning getLocation() = null");
		}
		
		//better for debug 
		this.threadPool.setPoolName(classRef.getSimpleName());

		File targetBundleFile = null;
		String location = null;

		// Bundle can be referenced as a Directory or file
		if (bundle.getLocation().startsWith("initial@reference:file:")) {
			location = bundle.getLocation().substring("initial@reference:file:".length());

		} else if (bundle.getLocation().startsWith("file:")) {
			location = bundle.getLocation().substring("file:".length());

		} else {
			location = bundle.getLocation();
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Bundle Location:  = " + location);
		}
		
		this.pluginCache.clear();
		
		//Find plugins: In case of more than one finding, the latest remains.
		
		/*
		 * 1st. Find Annotated plugins...
		 */
		this.findAnnotatedPlugins(bundle, classRef, new ArrayList<PluginMetadata>());

		
		/*
		 * 2nd. Find XML Plugins... 
		 */
		targetBundleFile = new File(location);

		if (targetBundleFile.exists() && targetBundleFile.canRead()) {

			try {

				if (targetBundleFile.isFile()) {
					// 1: Extract Plugin resources directory
					JarFileUtil.extractJarFile(bundle.getSymbolicName(), targetBundleFile, workingDirectory);

					// 2: Scan and read plugins
					log.debug("Starting find plugins from getBundleResourcesDirectory()");
					this.findXMLPlugins(JarFileUtil.getBundleResourcesDirectory(bundle.getSymbolicName(), workingDirectory));
					
				}else if (targetBundleFile.isDirectory()) {
					log.debug("Starting find plugins from " + targetBundleFile + " directory.");
					this.findXMLPlugins(targetBundleFile);
				}

			} catch (Exception e) {
				log.warn("Unable to Find XML Plugins: {}", e.getMessage());
				if (log.isDebugEnabled()) {
					e.printStackTrace();
				}
			}
			
		} else {
			throw new PluginServiceProviderException("Cannot read " + targetBundleFile.getAbsolutePath());
		}
		
	}
	
	public List<String> getPluginsName() {
		return new ArrayList<String>(this.pluginCache.keySet());
	}
	
	@Override
	public List<PluginMetadata> listKnownPlugins() {
		List<PluginMetadata> items = new ArrayList<PluginMetadata>();
		this.pluginCache.forEach((k, v) -> {
			items.add(v.clonePlugin());
		});
		return items;
	}
	
	public PluginMetadata getPlugin(String name) {
		
		if (this.pluginCache.containsKey(name)) {
			return this.pluginCache.get(name).clonePlugin();
		}
		
		return null;
		
	}
	
	public void registerAnnotatedPlugin(Object instance) {
		
		if (instance != null) {
			
			if (instance.getClass().isAnnotationPresent(Plugin.class)) {
				log.debug("Found plugin: {}", instance.getClass());
				
				IPluginParser parser = new PluginAnnotationParser(instance.getClass());
				this.acceptPlugin(parser);
				
			}
			
		}
		
	}

	private void findXMLPlugins(File path) {

		if (path == null) {
			log.warn("Unable to find plugins at NULL. Path must be directory.");
			return;
		}
		
		if (!path.exists()) {
			log.warn("Path does not exist " + path.getAbsolutePath());
			return;
		}

		if (!path.canRead()) {
			log.warn("Cannot read " + path.getAbsolutePath());
			return;
		}

		if (path.isFile()) {
			log.warn(
					"Unable to find plugins at " + path.getAbsolutePath() + ". Path is a file and must be a diretory.");
			return;
		}

		log.info("Starting findPlugins at " + path.getAbsolutePath() + ". Filtering only plugins supported in this platform.");
		int count = 0;
		long tStart = System.currentTimeMillis();

		//this.pluginCache.clear();
		// Parse all plugins. If OK put on the plugin list.
		for (File file : FileUtils.listFiles(path, new String[] { "xml" }, true)) {
			
			try {
				IPluginParser parser = new PluginXMLFileParser(file, this.concreteFactoryClass);
				this.acceptPlugin(parser);
				
			} catch (MalformedPluginException e) {
				if (log.isDebugEnabled()) {
					e.printStackTrace();
				}
				
			}

		}
		
		long tStop = System.currentTimeMillis();
		log.info(MessageFormat.format("findXMLPlugins() done. {0} Plugin(s) added. Elapsed time {1} ms.", count,
				(tStop - tStart)));
		
	}
	
	private void findAnnotatedPlugins(Bundle bundle, Class<?> classRef, List<PluginMetadata> found) {
		
		BundleWiring wire = bundle.adapt(BundleWiring.class);
		
		String path = "";
		if(bundle.getSymbolicName() != null) {
			path = bundle.getSymbolicName().replace(".", File.separator);
		}else {
			log.warn("Bundle Symbolic name not defined. Scanning all classes, jars and resources.");
		}
		
		log.info("Scanning classes at bundle's getSymbolicName() path: {}", path);
			
		if (wire != null) {
			log.debug("scanAnnotations by BundleWiring *.class recursive");
			wire.listResources(path, "*.class", BundleWiring.LISTRESOURCES_RECURSE).forEach((item) ->{
				
				try {
					String className = item.replace("/", ".").replace(".class", "");
					Class<?> clazz = OsgiUtils.getClass(classRef, className);
					
					if (clazz.isAnnotationPresent(Plugin.class)) {
						log.debug("Found plugin: {}", className);
						
						IPluginParser parser = new PluginAnnotationParser(clazz);
						this.acceptPlugin(parser);
						
					}
					
				} catch (Exception e) {
					log.debug("Ignoring exception while scanAnnotations(): {}", e.getMessage());
				}
				
			});
		}
		
//		Reflections reflections = new Reflections("plugin.core");
//		
////		Reflections reflections = new Reflections(new ConfigurationBuilder()
////				  .setUrls(ClasspathHelper.forPackage("io.infolayer"))
////				  .setScanners(new TypeAnnotationsScanner())
////			);
//		
//		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Plugin.class, true);
//		
//		for (Class<?> clazz : annotated) {
//			
//			System.out.println("found " + clazz.getName());
//			
//			IPluginParser parser = new PluginAnnotationParser(clazz);
//			this.acceptPlugin(parser, found);
//			
//		}
		
	}
	
	private void acceptPlugin(IPluginParser parser) {
		try {
			PluginMetadata plugin = parser.getPlugin();

			if (PlatformUtils.isPlatformCompatible(plugin.getPlatform())) {
				
				try {
					
					log.info("   Found plugin: " + plugin.getName() + ", testing it.");
					
					PluginPrototype prototype = parser.getPluginPrototype();
					
					PluginMetadata pm = prototype.clonePlugin();
					
					if (pm.getPlatform() == null || "".equals(pm.getPlatform())) {
						throw new MalformedPluginException("Platform cannot be null.");
					}
					
					if (pm.getVersion() == null || "".equals(pm.getVersion())) {
						throw new MalformedPluginException("Version must be N.N[.N] format.");
					}
					
					if (pm.getName() == null || "".equals(pm.getName())) {
						throw new MalformedPluginException("Name cannot be null.");
					}
					
					Pattern pattern = Pattern.compile("[-a-z0-9]+");
					Matcher matcher = pattern.matcher(pm.getName());
					
					if (!matcher.matches()) {
						throw new MalformedPluginException("Plugin name is invalid. Name is: '" + pm.getName() + "'. Acceptable is: text, numbers and -");
					}
					
					//Just create and thorws any exception
					this.createRunnablePlugin(prototype);
					
					//Plugin is resolvable, adding it...
					if (this.pluginCache.containsKey(plugin.getName())) {
						log.info("Replacing plugin prototype for plugin: {}", plugin.getName());
						this.pluginCache.replace(plugin.getName(), prototype);
						
					}else {
						this.pluginCache.put(plugin.getName(), prototype);
					}
					
					log.info("   Plugin instantiation validation passed.");
					
				}catch (Exception e) {
					//if (log.isDebugEnabled()) {
						e.printStackTrace();
					//}
					log.warn("Skipping Plugin '" + plugin.getName() + "' exception while trying it: " + e.getMessage());
				}
				
				
			}else{
				log.warn("Skipping Plugin '" + plugin.getName() + "' not supported in this platform.");
			}
			
		} catch (MalformedPluginException e) {
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			
		}
	}
	
	private IRunnablePlugin createRunnablePlugin(PluginPrototype prototype) throws Exception {
		
		IRunnablePlugin runnablePlugin = OsgiUtils.createInstance(IRunnablePlugin.class, this.getClass(), prototype.getPluginClassName());
		
		runnablePlugin.configure(
				prototype.clonePlugin(), 
				prototype.getTimeout(), 
				prototype.cloneInputHandlers(this.getClass()),
				prototype.cloneOutputHandlers(this.getClass()));
	
		OsgiUtils.injectOsgiServices(runnablePlugin);
		
		return runnablePlugin;
		
	}
	
	@Override
	public PluginSubmissionResponse submit(PluginCall call, Map<String, String> environment, Set<IRunnableListener> listeners, OutputFlow outputFlow) throws PluginException {
		
		long tStart = System.currentTimeMillis();
		
		try {
			
			if (call == null) {
				throw new PluginException("Call cannot be null");
			}
			
			if (outputFlow == null) {
				throw new PluginException("OutputFlow cannot be null");
			}
			
			PluginPrototype prototype =  this.pluginCache.get(call.toPluginSimpleName());
			
			if (prototype == null) {
				throw new PluginException(MessageFormat.format("Plugin not found at provider cache: {0}", call.getPluginFqdnName()));
			}
			
			Map<String, String> env = prototype.cloneEnvironment();
			if (environment != null) {
				env.putAll(environment);
			}
			
//			//Enforce ResourceGroup ID. Each Siteview instance has its own TID (probably local).
//			//FIXME This should be done at RepositoryService...
//			
//			IEntityRepository repo = OsgiUtils.getOSGIService(IEntityRepository.class, false);
//			if (repo != null) {
//				ResourceGroup resourceGroup = repo.getEntity(ResourceGroup.class, "uuid", SiteviewInstance.getUUID());
//				if (resourceGroup == null) {
//					env.put(InventoryItem.FIELD_RID, ResourceGroup.LOCAL_RID);
//				} else {
//					env.put(InventoryItem.FIELD_RID, resourceGroup.getRid());
//				}
//			}
//			
//			//End of fix me
			
			Set<IRunnableListener> pluginListeners = new HashSet<IRunnableListener>();
			if (listeners != null) {
				pluginListeners.addAll(listeners);
			}
			
			//Enforce StatusEventListener
			IRunnableListener eventListener = new StatusEventListener();
			OsgiUtils.injectOsgiServices(eventListener);
			pluginListeners.add(eventListener);
			
			IRunnablePlugin runnablePlugin = this.createRunnablePlugin(prototype);
			
			runnablePlugin.submit(call, env, pluginListeners, outputFlow);
			
			Future<IRunnablePlugin> future = this.threadPool.submit(runnablePlugin);
			PluginSubmissionResponse response = new PluginSubmissionResponse(future);
			
			log.info(MessageFormat.format("Plugin submission: Instance {0}, params {1}, environment {2}, elapsed {3} ms.", 
					runnablePlugin.getInstanceID(), 
					call.getPluginParams() != null ? call.getPluginParams().size() : -1, 
					env != null ? env.size() : -1,
					(System.currentTimeMillis() - tStart)));
			
			return response;
			
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			log.error(MessageFormat.format("Plugin submission exception: Plugin name {0}, params {1}, elapsed {2} ms.", 
					call.getPluginFqdnName(), 
					call.getPluginParams() != null ? call.getPluginParams().size() : -1, 
					(System.currentTimeMillis() - tStart)));
			throw new PluginException(e);
		}
		
	}

	@Override
	public void shutdown(Bundle bundle) {
		try {
			this.pluginCache.clear();
			this.threadPool.shutdown();
		}catch(Exception e) {
			log.error(e.getMessage());
		}	
	}
	
	@Override
	public int getQueueCount() {
		return this.threadPool.getRunningCount();
	}
	
	@Override
	public Set<String> getActiveQueue() {
		return this.threadPool.getRunningInstances();
	}

	@Override
	public boolean cancel(String executionEventID) {
		return this.threadPool.cancel(executionEventID);
	}

}
