package io.infolayer.aida.executor.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.annotation.Plugin;
import io.infolayer.aida.exception.MalformedPluginException;
import io.infolayer.aida.executor.PluginService;
import io.infolayer.aida.plugin.IRunnablePlugin;
import io.infolayer.aida.plugin.PluginMetadata;
import io.infolayer.aida.plugin.PluginPrototype;
import io.infolayer.aida.utils.PlatformUtils;

public class PluginScanner {

    private static final Logger log = LoggerFactory.getLogger(PluginService.class);

    public static List<PluginMetadata> scanXMLPlugins(Bundle bundle, Class<?> classRef) {
		
        BundleWiring wire = bundle.adapt(BundleWiring.class);
        
        List<PluginMetadata> found = new ArrayList<>();
		
		String path = "";
		if(bundle.getSymbolicName() != null) {
			path = bundle.getSymbolicName().replace(".", File.separator);
		}else {
			log.warn("Bundle Symbolic name not defined. Scanning all classes, jars and resources.");
		}
		
		log.info("Scanning classes at bundle's getSymbolicName() path: {}", path);
			
		if (wire != null) {
			log.debug("scanXMLPlugins by BundleWiring *.xml recursive");
			wire.listResources(path, "*.xml", BundleWiring.LISTRESOURCES_RECURSE).forEach((item) ->{
                
                log.info("Found file {}", item);

                try {
                    IPluginParser parser = new PluginXMLFileParser(new File(item), classRef);
                    if (acceptPlugin(parser)) {
                        found.add(parser.getPlugin());
                    }
                    
                } catch (MalformedPluginException e) {
                    log.debug("Ignoring exception while scanXMLPlugins(): {}", e.getMessage());
                    
                }
				
			});
        }

        return found;
    }


    public static List<PluginMetadata> findAnnotatedPlugins(Bundle bundle, Class<?> classRef) {
		
        BundleWiring wire = bundle.adapt(BundleWiring.class);
        
        List<PluginMetadata> found = new ArrayList<>();
		
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
                        if (acceptPlugin(parser)) {
                            found.add(parser.getPlugin());
                        }
					}
					
				} catch (Exception e) {
					log.debug("Ignoring exception while scanAnnotations(): {}", e.getMessage());
				}
				
			});
        }

        return found;
    }

    private static boolean acceptPlugin(IPluginParser parser) {
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
					
					if (pm.getName() == null || "".equals(pm.getName())) {
						throw new MalformedPluginException("Name cannot be null.");
					}
					
					Pattern pattern = Pattern.compile("[-a-z0-9]+");
					Matcher matcher = pattern.matcher(pm.getName());
					
					if (!matcher.matches()) {
						throw new MalformedPluginException("Plugin name is invalid. Name is: '" + pm.getName() + "'. Acceptable is: text, numbers and -");
					}
					
					//Just create and thorws any exception
					createRunnablePlugin(prototype);
                    log.info("   Plugin instantiation validation passed.");
                    return true;
					
				}catch (Exception e) {
					log.warn("Skipping Plugin '" + plugin.getName() + "' exception while trying it: " + e.getMessage());
				}
				
			}else{
				log.warn("Skipping Plugin '" + plugin.getName() + "' not supported in this platform.");
			}
			
		} catch (MalformedPluginException e) {
			log.warn("Skipping Plugin due exception {}", e.getMessage());
        }
        
        return false;
    }
    
    private static IRunnablePlugin createRunnablePlugin(PluginPrototype prototype) throws Exception {
		
		IRunnablePlugin runnablePlugin = OsgiUtils.createInstance(IRunnablePlugin.class, this.getClass(), prototype.getPluginClassName());
		
		runnablePlugin.configure(
				prototype.clonePlugin(), 
				prototype.getTimeout(), 
				null,
				null);
	
		//OsgiUtils.injectOsgiServices(runnablePlugin);
		
		return runnablePlugin;
		
	}
    
}
