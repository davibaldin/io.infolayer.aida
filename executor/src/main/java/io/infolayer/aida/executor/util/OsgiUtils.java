package io.infolayer.aida.executor.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
//import org.osgi.service.event.EventAdmin;
//import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.infolayer.aida.plugin.IPluginService;

//import io.infolayer.siteview.IPluginService;
//import io.infolayer.siteview.annotation.SiteviewService;

public class OsgiUtils {
	
	private static Logger log = LoggerFactory.getLogger(OsgiUtils.class);
	
	private static Object classLock = OsgiUtils.class;
	//private static EventAdmin eventAdmin = null;
	
	/**
	 * Return true if running inside OSGI Framework.
	 * @return
	 */
	public static boolean isOSGIFramework() {
		
		Bundle myself = FrameworkUtil.getBundle(OsgiUtils.class);
		
		if (myself != null) {
			if (myself.getBundleContext() != null) {
				return true;
			}
			
		}
		
		return false;
	}
	
	
	/**
	 * Get Service from Framework. BundleContext in this case is related to OsgiUtils.class's bundle.
	 * @param <T>
	 * @param serviceInterface
	 * @param throwsException in case service cannot be found. Otherwise return null value.
	 * @return
	 * @throws Exception
	 */
	public static <T> T getOSGIService(Class<T> serviceInterface, boolean throwsException) throws Exception {
		
		Bundle myself = FrameworkUtil.getBundle(OsgiUtils.class);
		BundleContext context = null;
		
		if (myself != null) {
			context = myself.getBundleContext();
		}
		
		return getOSGIService(serviceInterface, context, throwsException);
		
	}
	
	public static DataSourceFactory getOSGIDataSourceFactory(String jdbcDriverClass, boolean throwsException) throws Exception {
		
		Bundle myself = FrameworkUtil.getBundle(OsgiUtils.class);
		BundleContext context = null;
		
		if (myself != null) {
			context = myself.getBundleContext();
			
			if (context != null && jdbcDriverClass != null ) {
				
				String filter = "(&(objectClass=org.osgi.service.jdbc.DataSourceFactory)(osgi.jdbc.driver.class=" + jdbcDriverClass + "))";
				
				/*
				 * Get the service
				 */
				Collection<ServiceReference<DataSourceFactory>> srs = context.getServiceReferences(DataSourceFactory.class, filter);
				if (srs != null && srs.size() > 0) {
					//get the first one
					ServiceReference<DataSourceFactory> sr = srs.iterator().next();
					
					DataSourceFactory dsf = context.getService(sr);
					
					if (dsf != null) {
						return dsf;
					}else {
						if (throwsException) {
							throw new Exception("Null DataSourceFactory");
						}else {
							return null;
						}
					}

				}
				
			}
		}
				
		if (throwsException) {
			throw new Exception("Null getOSGIDataSourceFactory()");
		}else {
			return null;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getOSGIService(Class<T> serviceInterface, BundleContext context, boolean throwsException) throws Exception {
		
		/*
		 * Deal with BundleContext null. In this case, maybe we are running outside a pure OSGi Framework.
		 * If so, we need a little extra help to resolve services, like in test cases.
		 */
		if (context == null) {
			
			Object service = FakeOsgiFramework.getInstance().getService(serviceInterface);
			if (service == null && throwsException) {
				
				if (throwsException) {
					throw new Exception("Unable to find serviceInterface " + serviceInterface.getName() + " using FakeOsgiFramework.");
				}else {
					return null;
				}
				
			}else {
				return (T) service;
			}
			
		}else {
			
			ServiceReference<T> sr = (ServiceReference<T>) context.getServiceReference(serviceInterface);
			
			if (sr == null) {
				log.debug("Unable to find ServiceReference to {}", serviceInterface.getName());
				if (throwsException) {
					throw new Exception("Unable to find ServiceReference");
				}
			}
			
			T instance = null;
			
			try {
				instance = context.getService(sr);
			}catch (Exception e) {
				System.out.println("BUG " + sr.toString() + ", Context = " + context.toString());
			}
			
			if (instance == null) {
				log.debug("Unable to get Service for {}", serviceInterface.getName());
				if (throwsException) {
					throw new Exception("Unable to get service instance");
				}
			}
			
			return instance;
		}
		
	}
	
	public static Class<?> getClass(Class<?> classReference, String className) throws Exception {
		
		Class<?> clazz;
		
		Bundle b1 = FrameworkUtil.getBundle(classReference); 	//Class reference Level
		Bundle b2 = FrameworkUtil.getBundle(OsgiUtils.class); 	//APIs Bundle level
		Bundle b3 = null;
		
		//Service
		if (b2 != null) {
			IPluginService service = getOSGIService(IPluginService.class, b2.getBundleContext(), false);
			if (service != null) {
				b3 = FrameworkUtil.getBundle(service.getClass());
			}
		}
		
		
		/*
		 * Another problem for testing plugins outside OSGI Framework.
		 * Lets delegate to OSGIUtils this ability.
		 */
		
		//1st Try: From ClassRef Bundle
		try {
			if (b1 != null) {
				clazz = b1.loadClass(className);
				log.debug("Instance found at classReference level.");
				return clazz;
			}
			
		}catch (ClassNotFoundException e) {
			
			//2nd Try: From API Bundle
			try {
				if (b2 != null) {
					clazz = b2.loadClass(className);
					log.debug("Instance found at plugins API Bundle.");
					return clazz;
				}
				
			}catch (ClassNotFoundException ee) { 
				
				//3nd Try: From Plugins Bundle
				try {
					if (b3 != null) {
						clazz = b3.loadClass(className);
						log.debug("Instance found at plugins Plugins Bundle.");
						return clazz;
					}
					
				}catch (ClassNotFoundException eee) { }
				
			}
			
		}
		
		//3nd Try: From Class loader
		try {
			clazz = Class.forName(className);
			log.debug("Instance found at main ClassPath.");
			return clazz;
			
		}catch (ClassNotFoundException eee) {
			
			throw new Exception(MessageFormat.format("Class {0} not found. No more tries.",  className));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T createInstance(Class<T> classInterface, Class<?> classReference, String className) throws Exception {
		
		return (T) getClass(classReference, className).newInstance();
		
	}
	
	// public static void injectOsgiServices(Object instance) {
		
	// 	if (instance == null) {
	// 		log.warn("Unable to use injectOsgiServices() at null instance.");
	// 		return;
	// 	}
		
	// 	//BundleContext context = FrameworkUtil.getBundle(instance.getClass()).getBundleContext();
		
	// 	log.debug("Injecting services on " + instance.getClass().getName());
	// 	Field[] fields = getAnnotatedDeclaredFields(instance.getClass(), SiteviewService.class, true);
	// 	for (int i = 0; i < fields.length; i++) {
			
	// 		boolean modified = false;
	// 		Field f = fields[i];
			
	// 		if (!f.isAccessible()) {
	// 			f.setAccessible(true);
	// 			modified = true;
	// 		}
			
	// 		try {
	// 			f.set(instance, OsgiUtils.getOSGIService(f.getType(), true));
				
	// 		} catch (Exception e) {
	// 			log.error("Error injecting service on field " + f.getName() + ": " + e.getMessage());
				
	// 		}finally {
				
	// 			if (modified) {
	// 				f.setAccessible(false);
	// 			}
				
	// 		}
	// 	}
		
	// }	
	
	// private static boolean hasEventAdmin() {
	// 	if (eventAdmin == null) {
	// 		synchronized (classLock) {
	// 			try {
	// 				eventAdmin = getOSGIService(EventAdmin.class, true);
	// 				return true;
	// 			} catch (Exception e) {
	// 				log.warn("EventAdmin service not found: {}", e.getMessage());
	// 				return false;
	// 			}
	// 		}
	// 	}
	// 	return true;
	// }
	
	// public static void postEvent(String namespace, String topic, Object payload) {

	// 	if (hasEventAdmin() && payload != null) {
			
	// 		HashMap<String, Object> data = new HashMap<String, Object>();
	// 		data.put(payload.getClass().getName(), payload);
	// 		Event event = new Event(namespace + "/" + topic, data);
	// 		eventAdmin.postEvent(event);
	// 	}
	// }
	
	// public static void postEvent(String namespace, String topic, String message) {
		
	// 	Map<String, Object> payload = new HashMap<String, Object>();
	// 	payload.put("message", message == null ? "null" : message );
		
	// 	postEvent(namespace, topic, payload);
	// }
	
//	@Deprecated
//	public static void createEvent(String eventFor, String severity, String message) {
//		
//		
//		try {
//			IEventRepositoryOLD repo = getOSGIService(IEventRepositoryOLD.class, true);
//			repo.createEvent(severity, message, null, null, eventFor);
//		} catch (Exception e) {
//			log.error("Unable to create event: Event{}, Message: {}, Exception is: {}", eventFor, message, e.getMessage());
//		}
//	}
	
	private static Field[] getDeclaredFields(Class<?> clazz, boolean recursively) {
		List<Field> fields = new LinkedList<Field>();
		Field[] declaredFields = clazz.getDeclaredFields();
		Collections.addAll(fields, declaredFields);

		Class<?> superClass = clazz.getSuperclass();

		if (superClass != null && recursively) {
			Field[] declaredFieldsOfSuper = getDeclaredFields(superClass, recursively);
			if (declaredFieldsOfSuper.length > 0)
				Collections.addAll(fields, declaredFieldsOfSuper);
		}

		return fields.toArray(new Field[fields.size()]);
	}

	private static Field[] getAnnotatedDeclaredFields(Class<?> clazz, Class<? extends Annotation> annotationClass,
			boolean recursively) {
		Field[] allFields = getDeclaredFields(clazz, recursively);
		List<Field> annotatedFields = new LinkedList<Field>();

		for (Field field : allFields) {
			if (field.isAnnotationPresent(annotationClass))
				annotatedFields.add(field);
		}

		return annotatedFields.toArray(new Field[annotatedFields.size()]);
	}
	
//	private static Field getAnnotatedDeclaredField(Class<?> clazz, Class<? extends Annotation> annotationClass, String fieldName) {
//		Field[] allFields = getDeclaredFields(clazz, false);
//
//		for (Field field : allFields) {
//			if (field.isAnnotationPresent(annotationClass) && field.getName().equals(fieldName)) {
//				return field;
//			}
//		}
//		
//		return null;
//	}

}
