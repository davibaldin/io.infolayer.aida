package io.infolayer.aida.executor.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FakeOsgiFramework {

		private static Logger log = LoggerFactory.getLogger(FakeOsgiFramework.class);
		
		private static FakeOsgiFramework instance;
		private static Object classLock = FakeOsgiFramework.class;
		private Map<Class<?>, Object> services;
		
		private FakeOsgiFramework() {
			log.info("New FakeOsgiFramework class has been created.");
			this.services = new ConcurrentHashMap<Class<?>, Object>();
		}
		
		public static FakeOsgiFramework getInstance() {
			synchronized (classLock) {
				if (instance == null) {
					instance = new FakeOsgiFramework();
				}
				return instance;
			}
		}
		
		public void registerService(Class<?> type, Object object) {
			if (object != null) {
				this.services.put(type, object);
			}
		}
		
		public Object getService(Class<?> type) {
			return this.services.get(type);
		}

}
