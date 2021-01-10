package io.infolayer.aida.executor;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.siteview.IEvent;
import io.infolayer.siteview.IPlaybookService;
import io.infolayer.siteview.IPluginService;
import io.infolayer.siteview.ISchedulerService;
import io.infolayer.siteview.annotation.InventoryItemType;
import io.infolayer.siteview.annotation.MetricType;
import io.infolayer.siteview.entity.LocalPlaybook;
import io.infolayer.siteview.entity.LocalPlugin;
import io.infolayer.siteview.entity.MessageRule;
import io.infolayer.siteview.entity.Metric;
import io.infolayer.siteview.entity.SchedulerEntry;
import io.infolayer.siteview.entity.SiteParameter;
import io.infolayer.siteview.exception.OutputFlowException;
import io.infolayer.siteview.exception.PlaybookServiceException;
import io.infolayer.siteview.exception.PluginException;
import io.infolayer.siteview.exception.PluginNotFoundException;
import io.infolayer.siteview.exception.PluginServiceProviderException;
import io.infolayer.siteview.exception.RepositoryException;
import io.infolayer.siteview.filter.FilterToken;
import io.infolayer.siteview.playbook.Playbook;
import io.infolayer.siteview.playbook.PlaybookTrigger;
import io.infolayer.siteview.plugin.IPluginServiceProvider;
import io.infolayer.siteview.plugin.IRunnableListener;
import io.infolayer.siteview.plugin.OutputFlow;
import io.infolayer.siteview.plugin.PluginCall;
import io.infolayer.siteview.plugin.PluginMetadata;
import io.infolayer.siteview.plugin.PluginParameterMetadata;
import io.infolayer.siteview.plugin.PluginSubmissionResponse;
import io.infolayer.siteview.repository.IEntityRepository;
import io.infolayer.siteview.repository.IInventoryRepository;
import io.infolayer.siteview.repository.ITimeSeriesRepository;
import io.infolayer.siteview.util.ConfigurationResolver;
import io.infolayer.siteview.util.OsgiUtils;
import io.infolayer.siteview.util.PlatformUtils;

@Component(immediate = true, service = {
		IPluginService.class, 
		IPlaybookService.class}
)
public class PluginService implements ILocalPluginService, IPlaybookService {

	private static Logger log = LoggerFactory.getLogger(PluginService.class);
	//private static Object classLock = PluginService.class;
	
	/*
	 * Caches:
	 * PluginName -> IPluginServiceProvider.class (ref)
	 * PluginName -> Plugin (Bean)
	 */
	
	private Map<String, CacheEntry> cacheServiceProvider;
//	private Map<String, Plugin> cachePlugin;
	private String workingDirectory;
	private String outputFlowDirectory;
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private IEntityRepository repository;
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private IInventoryRepository inventoryRepository;
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private ITimeSeriesRepository timeSeriesRepository;
	
	private class CacheEntry {
		private String context;
		private IPluginServiceProvider provider;
	}
	
	public PluginService() {
		this.cacheServiceProvider = new ConcurrentHashMap<String, CacheEntry>();
	}
	
	public PluginService(String workingDirectory, String outputFlowDirectory) {
		this();
		this.workingDirectory = workingDirectory;
		this.outputFlowDirectory = outputFlowDirectory;
	}
	
	@Activate
	public void start(BundleContext ctx) throws Exception {
		
		log.info("Starting Plugin service");
		
		ConfigurationResolver config = new ConfigurationResolver(
				OsgiUtils.getConfigurationProperties(ILocalPluginService.PID, ctx, true));
		
		log.info("    Validating OutputFlow...)");
		
		File workingDir = new File(PlatformUtils.sanitizePath(config.getString(ILocalPluginService.CONF_WORKING_ROOT, "svplugins", false)));
		log.info("Plugins working directory is {}", workingDir);
		if (!workingDir.exists()) {
			workingDir.mkdirs();
			log.info("    Directories created.");
		}
		
		File outputFlowDir = new File(PlatformUtils.sanitizePath(config.getString(ILocalPluginService.CONF_OUTPUTFLOW_ROOT, "svflow", false)), "tmp");
		log.info("Flow working directory is {}", outputFlowDir);
		if (!outputFlowDir.exists()) {
			outputFlowDir.mkdirs();
			log.info("    Directories created.");
		}
		
		OutputFlow flow = OutputFlow.newInstance(outputFlowDir.getParentFile().getAbsolutePath());
		flow.finish();
		log.info("    OutputFlow success.");
		
		workingDirectory = workingDir.getCanonicalPath();
		outputFlowDirectory = outputFlowDir.getCanonicalPath();
		
		this.unregisterAllPluginProvider();
		
		log.info("Started");
	}
	
	@Deactivate
	public void stop(BundleContext ctx) {
		
		this.unregisterAllPluginProvider();
		
		log.info("Plugin service stopped.");
	}
	
	@Override
	public int getPluginsCount() {
		
		int count = 0;
		
		try {
			//IEntityRepository repository = OsgiUtils.getOSGIService(IEntityRepository.class, true);
			count = repository.searchEntities(
					LocalPlugin.class, 
					FilterToken.newFilter().equal("status", LocalPlugin.STATUS_ONLINE), null);
		}catch (Exception e) {
			log.error("Exception while getting getPluginsCount(): " + e.getMessage());
		}
		
		return count;
	}
	
	@Override
	public List<String> getPluginNames() {
		
		List<String> name = new ArrayList<String>();
		
		try {
			repository.searchEntities(
					LocalPlugin.class, 
					FilterToken.newFilter().equal("status", LocalPlugin.STATUS_ONLINE), 
					(item) -> {
						name.add(item.getProviderContext() + "." + item.getPlugin().getName());
					});
		}catch (Exception e) {
			log.error("Exception while getting getPluginsCount(): " + e.getMessage());
		}
		
		return name;
	}
	
	@Override
	public LocalPlugin getPlugin(String pluginFqdn) throws PluginNotFoundException {
		
		if (pluginFqdn == null || "".equals(pluginFqdn)) {
			throw new PluginNotFoundException("Invalid plugin name: " + pluginFqdn);
		}
		
		try {
			
			String fqdn[] = pluginFqdn.split("\\.");
			String name;
			String context;
			
			switch(fqdn.length) {
			case 1:
				name = fqdn[0];
				context = "core";
				break;
				
			case 2:
				name = fqdn[1];
				context = fqdn[0];
				break;
				
			default:
				throw new PluginNotFoundException("Invalid plugin name: " + pluginFqdn);
				
			}
			
			LocalPlugin localPlugin = repository.getEntity(
					LocalPlugin.class, 
					FilterToken.newFilter()
						.equal("status", LocalPlugin.STATUS_ONLINE)
						.and()
						.equal("plugin.name", name)
						.equal("providerContext", context)
						.getFilter(),
					true);
			
			if (localPlugin != null) {
				
				PluginMetadata plugin = localPlugin.getPlugin();
				
				plugin.getParameters().forEach((param) -> {
					SiteParameter sp = this.getSitePluginParameters(plugin.getName(), param.getName());
					if (sp != null) {
						param.setValue(sp.getValue());
						log.debug("Defined SiteParamenter: Plugin {}, Param name {}", plugin.getName(), param.getName());
					}
				});
				
				return localPlugin;
			}
		}catch (Exception e) {
			log.error("Exception while getting plugin " + pluginFqdn + ": " + e.getMessage());
		}
		
		throw new PluginNotFoundException("Plugin not found: " + pluginFqdn);
	}
	
	public void unregisterAllPluginProvider() {
		try {
			
			//IEntityRepository repository = OsgiUtils.getOSGIService(IEntityRepository.class, true);
			
			repository.searchAndExecute(LocalPlugin.class,
					null,
					(item, repo) -> {
						try {
							this.cacheServiceProvider.remove(item.getProviderClassName());
							item.setOffline();
							repo.saveEntity(LocalPlugin.class, item, true, "oid");
						} catch (RepositoryException e) {
							log.error("Exception while unregisterAllPluginProvider(): " + e.getMessage());
						}
					});

		}catch (Exception e) {
			log.error("Exception while unregisterAllPluginProvider(): " + e.getMessage());
		}
	}
	
	@Override
	public void unregisterPluginProvider(IPluginServiceProvider provider) {
		if (provider != null) {
			this.unregisterPluginProvider(provider.getClass().getName());
		}
	}
	
	@Override
	public void unregisterPluginProvider(String providerClassName) {
		if (providerClassName != null) {
			
			try {
				
				this.cacheServiceProvider.remove(providerClassName);
				
				//IEntityRepository repository = OsgiUtils.getOSGIService(IEntityRepository.class, true);
				
				repository.searchAndExecute(LocalPlugin.class,
						FilterToken.newFilter().equal("providerClassName", providerClassName),
						(item, repo) -> {
							try {
								item.setOffline();
								repo.saveEntity(LocalPlugin.class, item, true, "oid");
							} catch (RepositoryException e) {
								log.error("Exception while unregisterPlugins from  " + providerClassName + ": " + e.getMessage());
							}
						});

			}catch (Exception e) {
				log.error("Exception while unregisterPlugins from  " + providerClassName + ": " + e.getMessage());
			}
			
		}
		
	}
	
	@Override
	public void registerPluginProvider(Bundle bundle, String pluginContext, IPluginServiceProvider provider) throws PluginServiceProviderException, PluginException {
		
		if (bundle == null) {
			throw new PluginServiceProviderException("Bundle cannot be null.");
		}
		
		if (pluginContext == null || "".equals(pluginContext)) {
			throw new PluginServiceProviderException("Plugin context cannot be null.");
			
		} else if (!pluginContext.matches("[a-z0-9]+")) {
			throw new PluginServiceProviderException("Plugin context should be text and numbers. Regex is [a-z0-9]+");
		}
		
		if (provider == null) {
			throw new PluginServiceProviderException("IPluginServiceProvider cannot be null.");
		}
		
		provider.scanBundle(bundle, provider.getClass(), this.workingDirectory);
		List<PluginMetadata> plugins = provider.listKnownPlugins();
		
		try {
			
			if (plugins != null) {
				//IEntityRepository repository = OsgiUtils.getOSGIService(IEntityRepository.class, true);
				
				for (PluginMetadata plugin: plugins) {
					LocalPlugin localPlugin = repository.getEntity(LocalPlugin.class, 
							FilterToken.newFilter()
								.equal("providerClassName", provider.getClass())
								.and()
								.equal("plugin.name", plugin.getName())
								.getFilter(), true);
					
					if (localPlugin == null) {
						localPlugin = new LocalPlugin();
						localPlugin.setOid(PlatformUtils.getAlphaNumericString(5));
					}
					
					localPlugin.setPlugin(plugin);
					localPlugin.setProviderClassName(provider.getClass().getName());
					localPlugin.setProviderContext(pluginContext);
					localPlugin.setBundleId(bundle.getBundleId());
					localPlugin.setStatus(LocalPlugin.STATUS_ONLINE);
					
					repository.saveEntity(LocalPlugin.class, localPlugin, true, "oid");
					registerSitePluginParameters(plugin);
				}
				
			}
			
			this.scanAnnotations(bundle, provider.getClass());
			
			CacheEntry entry = new CacheEntry();
			entry.context = pluginContext;
			entry.provider = provider;
			
			this.cacheServiceProvider.put(provider.getClass().getName(), entry);
			
		} catch (Exception e) {
			throw new PluginException(e.getMessage());
		}
		
	}
	
	private void scanAnnotations(Bundle bundle, Class<?> classRef) {
		
		BundleWiring wire = bundle.adapt(BundleWiring.class);
		
		String path = "";
		if(bundle.getSymbolicName() != null) {
			path = bundle.getSymbolicName().replace(".", File.separator);
		} else {
			log.warn("Bundle Symbolic name not defined. Scanning all classes, jars and resources.");
		}
		
		log.info("Scanning classes at bundle's getSymbolicName() path: {}", path);
		
		if (wire != null) {
			log.debug("scanAnnotations by BundleWiring *.class recursively");
			wire.listResources(path, "*.class", BundleWiring.LISTRESOURCES_RECURSE).forEach((item) ->{
				
				try {
					String className = item.replace("/", ".").replace(".class", "");
					
					Object instance = OsgiUtils.createInstance(Object.class, classRef, className);
					this.scanTypeAnnotations(instance.getClass());
					
				} catch (Exception e) {
					log.error("Exception while scanAnnotations() at file {}: {}", item, e.getMessage());
				}
				
			});
		}
	}
	
	private void scanTypeAnnotations(Class<?> clazz) {
		
		try {
			
			//IInventoryRepository inventoryRepository = OsgiUtils.getOSGIService(IInventoryRepository.class, true);
			//ITimeSeriesRepository timeSeriesRepository = OsgiUtils.getOSGIService(ITimeSeriesRepository.class, true);
			
			if (clazz != null) {
				Field[] fields = clazz.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field f = fields[i];
					
					/*
					 * InventoryType
					 */
					
					if (f.isAnnotationPresent(InventoryItemType.class)) {
						InventoryItemType annon = f.getAnnotation(InventoryItemType.class);
						
						try {
							
							io.infolayer.siteview.entity.InventoryItemType type = new io.infolayer.siteview.entity.InventoryItemType();
							
							type.setName(f.get(null).toString());
							type.setDescription(annon.description());
							type.setDistinct(annon.distinct());
							type.setSearchFields(annon.searchFields());
							type.setShowDescription(annon.showDescription());
							type.setShowName(annon.showName());
						
							inventoryRepository.registerInventoryItemType(this.getClass().getName(), type);
							log.info("Registered InventoryItemType {}", type.getName());
						
						}catch (Exception e) {
							log.error(e.getMessage());
						}
					
					/*
					 * Metric Type
					 */
					}else if (f.isAnnotationPresent(MetricType.class)) {
						
						MetricType annon = f.getAnnotation(MetricType.class);
						Metric metric = new Metric();
						metric.setAcceptPattern(annon.acceptPattern());
						metric.setDescription(annon.description());
						metric.setErrorValue(annon.errorValue() == "" ? null : annon.errorValue());
						metric.setFieldIdName(annon.fieldIdName() == "" ? null : annon.fieldIdName());
						metric.setMaxValue(annon.maxValue());
						metric.setMinValue(annon.minValue());
						metric.setName(annon.name());
						metric.setSpaceName(annon.spaceName());
						metric.setType(annon.type());
						timeSeriesRepository.createMetric(metric);
						
					}
				}
			}
			
		}catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	private void registerSitePluginParameters(PluginMetadata plugin) {
		
		try {
			
			log.info("Start registering SitePluginParameters");
			//IEntityRepository repository = OsgiUtils.getOSGIService(IEntityRepository.class, true);
			
			plugin.getParameters().forEach((param) -> {
				
				try {
					
					FilterToken filter = FilterToken.newFilterAnd()
						.equal("pluginName",  plugin.getName())
						.equal("name", param.getName())
					.getFilter();
					
					SiteParameter sp = repository.getEntity(SiteParameter.class, filter, false);
					
					//First time adding SiteParameter.
					if (sp == null) {
						sp = new SiteParameter();
						sp.setPluginName(plugin.getName());
						sp.setName(param.getName());
						sp.setValue(param.getValue());
						sp.setCredentials(param.isCredentials());
						sp.setRequired(param.isRequired());
						sp.setPluginVersion(plugin.getVersion());
						sp.setPluginDescription(plugin.getDescription());
						
					//Just update some fields...
					}else {
						sp.setCredentials(param.isCredentials());
						sp.setRequired(param.isRequired());
						sp.setPluginVersion(plugin.getVersion());
						sp.setPluginDescription(plugin.getDescription());
					}
					
					log.info("Registering site parameter: {} -> {}", sp.getPluginName(), sp.getName());
					repository.saveEntity(SiteParameter.class, sp, filter);
					
				} catch (RepositoryException e) {
					log.error("Unable to register SitePluginParameters. Exception {}", e.getMessage());
				}
				
			});
			
		} catch (Exception e) {
			log.error("Unable to register SitePluginParameters. Exception {}", e.getMessage());
		}
		
	}
	
	/**
	 * Return SiteParameter if defined.
	 * @param pluginName
	 * @param parameterName
	 * @return
	 */
	private SiteParameter getSitePluginParameters(String pluginName, String parameterName) {
		
		try {
			//IEntityRepository repository = OsgiUtils.getOSGIService(IEntityRepository.class, true);
			
			FilterToken filter = FilterToken.newFilterAnd()
				.equal("pluginName", pluginName)
				.equal("name", parameterName)
			.getFilter();
			
			return repository.getEntity(SiteParameter.class, filter, false);
			
		} catch (Exception e) {
			log.error("Unable to register SitePluginParameters. Exception {}", e.getMessage());
		}
		
		return null;
	}

	@Override
	public IPluginServiceProvider getServiceProviderByPluginName(String pluginFqdn) throws PluginNotFoundException {
		
		if (pluginFqdn == null || "".equals(pluginFqdn)) {
			throw new PluginNotFoundException("Invalid plugin name: " + pluginFqdn);
		}
		
		try {
			
			String fqdn[] = pluginFqdn.split("\\.");
			String name;
			String context;
			
			switch(fqdn.length) {
			case 1:
				name = fqdn[0];
				context = "core";
				break;
				
			case 2:
				name = fqdn[1];
				context = fqdn[0];
				break;
				
			default:
				throw new PluginNotFoundException("Invalid plugin name: " + pluginFqdn);
				
			}
			
			LocalPlugin localPlugin = repository.getEntity(
					LocalPlugin.class, 
					FilterToken.newFilter()
						.equal("status", LocalPlugin.STATUS_ONLINE)
						.and()
						.equal("plugin.name", name)
						.equal("providerContext", context)
						.getFilter(),
					true);
			
			if (localPlugin == null) {
				throw new PluginNotFoundException("Plugin " + pluginFqdn + " not found or not on-line.");
			}
			
			return this.getServiceProviderByName(localPlugin.getProviderClassName());
			
		}catch (Exception e) {
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
			log.error("Exception while getServiceProviderByPluginName(): " + e.getMessage());
			throw new PluginNotFoundException("Plugin " + pluginFqdn + " found but Exception: " + e.getMessage());
		}
		
	}
	
	@Override
	public IPluginServiceProvider getServiceProviderByName(String providerName) throws PluginNotFoundException {
		
		CacheEntry entry = this.cacheServiceProvider.get(providerName);
		
		if (entry == null) {
			throw new PluginNotFoundException("IPluginServiceProvider class " + providerName +" not bound.");
		}
		
		return entry.provider;
		
//		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
//		
//		if (providerName != null) {
//			
//			try {
//				Collection<ServiceReference<IPluginServiceProvider>> providers = context.getServiceReferences(IPluginServiceProvider.class, null);
//				
//				if (providers != null) {
//					for (ServiceReference<IPluginServiceProvider> serviceReference : providers) {
//						
//						IPluginServiceProvider provider = context.getService(serviceReference);
//						
//						if (provider != null && provider.getClass().getName().equals(providerName)) {
//							return provider;
//						}
//					
//					}
//				}
//				
//			} catch (InvalidSyntaxException e) { }
//			
//		}
//		
//		throw new PluginNotFoundException("ProviderName " + providerName + " not found.");
	}

	@Override
	public List<String> getIPluginServiceProviderNames() {
		
		List<String> name = new ArrayList<String>();
		
		try {
			//IEntityRepository repository = OsgiUtils.getOSGIService(IEntityRepository.class, true);
			repository.searchEntities(LocalPlugin.class,
				FilterToken.newFilter()
					.equal("status", LocalPlugin.STATUS_ONLINE)
					.getFilter(),
				(item) -> {
					if (!name.contains(item.getProviderClassName())) {
						name.add(item.getProviderClassName());
					}
				});
		}catch (Exception e) {
			log.error("Exception while getting getPluginsCount(): " + e.getMessage());
		}
		
		return name;
	}
	
	/**
	 * Deep check if plugin exists and inject site parameters.
	 * @param call
	 * @throws PluginException
	 * @throws PluginNotFoundException 
	 */
	private void deepCallParser(PluginCall call) throws PluginException, PluginNotFoundException {
		
		LocalPlugin plugin = this.getPlugin(call.getPluginFqdnName());
		if (plugin == null) {
			throw new PluginException("Plugin not found.");
		}
		
		Map<String, String> callParams = call.getPluginParams();
		
		//Inject site Parameters
		if (plugin.getPlugin().getParameters() != null) {
			for (PluginParameterMetadata param : plugin.getPlugin().getParameters()) {
				
				if (callParams == null) {
					
					//Create at first
					call.setPluginParams(new HashMap<String, String>());
					callParams = call.getPluginParams();
					callParams.put(param.getName(), param.getValue());
					
				} else {
					
					if (!callParams.containsKey(param.getName())) {
						callParams.put(param.getName(), param.getValue());
					}
				}
				
			}
		}
		
		if (call.getCall() != null) {
			for (PluginCall inner : call.getCall()) {
				deepCallParser(inner);
			}
		}
		
	}
	
	@Override
	public PluginSubmissionResponse submit(PluginCall call, Map<String, String> environment, Set<IRunnableListener> statusListeners, OutputFlow outputFlow) throws PluginException {
		
		if (call == null) {
			throw new PluginException("Null Chained call as PluginService level.");
		}
		
		//Deep parse call
		this.deepCallParser(call);
		
		if (outputFlow == null) {
			try {
				outputFlow = OutputFlow.newInstance(this.outputFlowDirectory);
			} catch (OutputFlowException e) {
				throw new PluginException(e);
			}
		}
		
		try {
			IPluginServiceProvider sp = this.getServiceProviderByPluginName(call.getPluginFqdnName());

			if (sp != null) {
				return sp.submit(call, environment, statusListeners, outputFlow);
			}
			
		} catch (PluginNotFoundException e) {
			throw new PluginException(e);
		}
		
		throw new PluginException("Unable to submit.");
	}
	
	@Override
	public PluginSubmissionResponse submitSingleListener(PluginCall call, Map<String, String> environment, IRunnableListener statusListener, OutputFlow outputFlow) throws PluginException {
		Set<IRunnableListener> listeners = null;
		
		if (statusListener != null) {
			listeners = new HashSet<IRunnableListener>();
			listeners.add(statusListener);
		}
		
		return this.submit(call, environment, listeners, outputFlow);
		
	}

	@Override
	public boolean cancel(String pluginName, String executionEventID) {
		
		try {
			
			IPluginServiceProvider provider = this.getServiceProviderByPluginName(pluginName);
			if (provider != null) {
				return provider.cancel(executionEventID);
			}
			
		}catch (Exception e ) {
			log.error("Exception while canceliing " + executionEventID + ": " + e.getMessage());
		}
		
		return false;
	}
	
	
	/*
	 *  Playbook implementation
	 */
	
	
	@Override
	public LocalPlaybook addPlaybook(Playbook playbook, String source, boolean forceDisabled) throws PlaybookServiceException {
		try {
			
			if (playbook == null) {
				throw new PlaybookServiceException("Null Playbook.");
			}
			
			if (playbook.getUuid() == null) {
				throw new PlaybookServiceException("Playbook UUID is required for adding. Exiting...");
			}
			
			if (forceDisabled) {
				playbook.setEnabled(false);
			}
			
			String playbookHashCode = "int:" + playbook.hashCode();
				
			//Replace the playbook
			LocalPlaybook oldPlaybook = repository.getEntity(LocalPlaybook.class, "playbook.uuid", playbook.getUuid());
			if (oldPlaybook != null) {
				
				if (oldPlaybook.getPlaybookHashCode().equals(playbookHashCode)) {
					log.info("Atempt to add and existent Playbook iod = {}, hash = {}. Skeeping.", oldPlaybook.getOid(), playbookHashCode);
					return oldPlaybook;
				}
				
				this.deletePlaybook(oldPlaybook.getOid(), false);
			}
			
			LocalPlaybook local = new LocalPlaybook();
			local.setLoadTimestamp(System.currentTimeMillis());
			local.setSource(source);
			local.setPlaybook(playbook);
			local.setOid(PlatformUtils.getAlphaNumericString(5));
			local.setPlaybookHashCode(playbookHashCode);
			
			repository.saveEntity(LocalPlaybook.class, local, FilterToken.newFilter().equal("playbook.uuid", local.getPlaybook().getUuid()));
			
			if (playbook.isEnabled()) {
				this.enablePlaybook(local.getOid());
			}
			
			return local;
			
		}catch (Exception e) {
			throw new PlaybookServiceException(e);
		}
	}

	@Override
	public void deletePlaybook(String oidOrSource, boolean source) throws PlaybookServiceException {
		
		if (oidOrSource == null) {
			throw new PlaybookServiceException("Playbook oid/source cannot be null.");
		}
		
		try {
			//Remove the playbook
			LocalPlaybook oldPlaybook = null;
			if (source) {
				oldPlaybook = repository.getEntity(LocalPlaybook.class, "source", oidOrSource);
			}else {
				oldPlaybook = repository.getEntity(LocalPlaybook.class, "oid", oidOrSource);
			}
			
			if (oldPlaybook != null) {
				
				this.disablePlaybook(oldPlaybook.getOid());
				repository.deleteEntities(LocalPlaybook.class, "oid", oldPlaybook.getOid());
				
			}
		}catch (Exception e) {
			throw new PlaybookServiceException(e);
		}
		
	}

	@Override
	public void disablePlaybook(String oid) throws PlaybookServiceException {
		try {
			
			FilterToken filter = FilterToken.newFilter().equal("oid", oid).getFilter();
			
			LocalPlaybook playbook = repository.getEntity(LocalPlaybook.class, filter, false);
			
			if (playbook != null) {
				playbook.setEnabled(false);
				repository.saveEntity(LocalPlaybook.class, playbook, filter);
				
				SchedulerEntry entry = repository.getEntity(SchedulerEntry.class, "instance", playbook.getOid());
				if (entry != null) {
					getSchedulerService().removeJob(entry.getOid());
				}
				
				long rules = repository.deleteEntities(MessageRule.class, "label", oid);
				if (rules > 0) {
					OsgiUtils.postEvent(IEvent.NS_MESSAGE, IEvent.EVENT_RULES_RELOAD, "reload");
				}
				

			}else {
				throw new PlaybookServiceException("Playbook not found.");
			}
			
		}catch (Exception e) {
			throw new PlaybookServiceException(e);
		}

	}

	@Override
	public void enablePlaybook(String oid) throws PlaybookServiceException {
		try {
			FilterToken filter = FilterToken.newFilter().equal("oid", oid).getFilter();
			
			LocalPlaybook playbook = repository.getEntity(LocalPlaybook.class, filter, false);
			
			if (playbook != null) {
				playbook.setEnabled(true);
				repository.saveEntity(LocalPlaybook.class, playbook, filter);
				
				if (playbook.getPlaybook().getTrigger() != null) {
					
					//Its cron
					if (playbook.getPlaybook().getTrigger().isType(PlaybookTrigger.CRON)) {
						
						SchedulerEntry job = new SchedulerEntry();
						job.setCronExpresssion(playbook.getPlaybook().getTrigger().getExpr());
						job.setType("playbook-run");
						job.setInstance(playbook.getOid());
						getSchedulerService().addJob(job);
				
					} else if (playbook.getPlaybook().getTrigger().isType(PlaybookTrigger.EVENT)) {
						
						String expr = playbook.getPlaybook().getTrigger().getExpr();
						
						if (expr == null || "".equals(expr)) {
							throw new PlaybookServiceException("Playbook trigger event requires expr");
						}
						
						MessageRule rule = new MessageRule();
						
						if (expr.contains(":")) {
							String data[] = expr.split(":");
							
							rule.setPrecedence(Integer.parseInt(data[0]));
							rule.setWhen(data[1]);
							
						} else {
							
							rule.setWhen(expr);
						}
						
						rule.setAction("runPlaybook");
						rule.setLabel(playbook.getOid());
						rule.setName("Playbook run " + playbook.getPlaybook().getName());
						
						repository.saveEntity(MessageRule.class, rule, true, "label");
						OsgiUtils.postEvent(IEvent.NS_MESSAGE, IEvent.EVENT_RULES_RELOAD, "reload");
						
					}
				}
				
			} else {
				
				throw new PlaybookServiceException("Playbook not found.");
			}
		}catch (Exception e) {
			throw new PlaybookServiceException(e);
		}
	}
	
	@Override
	public LocalPlaybook getPlaybookByOid(String oid) throws RepositoryException {
		
		FilterToken filter = FilterToken.newFilter().equal("oid", oid).getFilter();
		LocalPlaybook local = repository.getEntity(LocalPlaybook.class, filter, false);
		return local;
	}
	
	@Override
	public LocalPlaybook getPlaybookByUuid(String uuid) throws RepositoryException {
		
		FilterToken filter = FilterToken.newFilter().equal("playbook.uuid", uuid).getFilter();
		LocalPlaybook local = repository.getEntity(LocalPlaybook.class, filter, false);
		return local;
	}
	
	@Override
	public void runPlaybook(String oid, boolean forceEnable, IRunnableListener listener,
			Map<String, String> environment, Object flowObject) throws PlaybookServiceException, OutputFlowException {
		
		
		OutputFlow outputFlow = null;
		
		if (flowObject != null) {
			
			outputFlow = OutputFlow.newInstance(this.outputFlowDirectory);
			
			if (environment != null) {
				if (environment.containsKey(OutputFlow.FLOW_ITEM_NAME)) {
					outputFlow.addProperty(environment.get(OutputFlow.FLOW_ITEM_NAME), flowObject);
				}
			}
			
			internalRunPlaybook(oid, forceEnable, listener, environment, outputFlow);
			
		} else {
			internalRunPlaybook(oid, forceEnable, listener, environment, null);
		}
		
	}
	
	@Override
	public void runPlaybook(String oid, boolean forceEnable, IRunnableListener listener,
			Map<String, String> environment) throws PlaybookServiceException, OutputFlowException {
		
		internalRunPlaybook(oid, forceEnable, listener, environment, null);
	}

	private void internalRunPlaybook(String oid, boolean forceEnable, IRunnableListener listener, Map<String, String> environment, OutputFlow flow) throws PlaybookServiceException {
		try {
			
			FilterToken filter = FilterToken.newFilter().equal("oid", oid).getFilter();
			
			LocalPlaybook local = repository.getEntity(LocalPlaybook.class, filter, false);
			
			if (local != null) {
				
				if (forceEnable) {
					local.setEnabled(true);
				}
				
				Playbook playbook = local.getPlaybook();
				
				if (playbook.isEnabled()) {
					
					if (playbook.getCall() != null) {
						for (PluginCall call : playbook.getCall()) {
							this.submitSingleListener(call, environment, listener, flow);
						}
					}
				}else {
					throw new PlaybookServiceException("Playbook not enabled.");
				}
				
				
			}else {
				throw new PlaybookServiceException("Playbook not found.");
			}
			
		}catch (Exception e ) {
			throw new PlaybookServiceException(e);
		}

	}
	
	/*
	 * FIXME Converter para Eventos
	 */
	@Deprecated
	private ISchedulerService getSchedulerService() throws Exception {
		return OsgiUtils.getOSGIService(ISchedulerService.class, true);
	}
	
}
