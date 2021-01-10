/*
 * Copyright 2008-2009 Sarbarian Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.infolayer.aida.executor.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import io.infolayer.aida.executor.exception.BeanAdapterException;

/**
 * BeanAdapter provide interface to access well formed JavaBean classes. Useful for serializing
 * Beans. Each Bean must have properties with get/set methods + Field.  
 * @author davi@sarbarian.com
 *
 */
public class BeanAdapter {
	
	private static final Map<Class<?>, BeanAdapter> cache = new ConcurrentHashMap<Class<?>, BeanAdapter>();

	private Class<?> beanClass = null;
	private BeanInfo beanInfo = null;
	private Map<String,PropertyDescriptor> properties = null;
	private static Object classLock = String.class; 
	private boolean map;
	private List<SimpleDateFormat> formatters;

	/**
	 * Get a cached Bean adapter instance.
	 * @param beanClass
	 * @return
	 * @throws BeanAdapterException
	 */
	public static BeanAdapter getInstance(Class<?> beanClass) throws BeanAdapterException {
		if (cache.containsKey(beanClass)) {
			return cache.get(beanClass);
			
		} else {
			synchronized (classLock) {
				BeanAdapter adapter = new BeanAdapter(beanClass);
				cache.put(beanClass, adapter);
				return adapter;
			}
		}
	}
	
	public static BeanAdapter getInstance(Object bean) throws BeanAdapterException {
		if (bean != null) {
			return getInstance(bean.getClass());
		}
		
		throw new BeanAdapterException("Null bean instance");
		
	}
	
	public static BeanAdapter getInstanceAndValidate(Object bean) throws BeanAdapterException {
		if (bean != null) {
			BeanAdapter instance = getInstance(bean.getClass());
			instance.isValidBean(bean);
			return instance;
		}
		
		throw new BeanAdapterException("Null bean instance");
		
	}
	
	public static void clearCache() {
		synchronized (classLock) {
			cache.clear();
		}
	}
	
	/**
	 * Construct a bean adapter based on already create Java Bean
	 * @param bean
	 * @throws BeanAdapterException
	 */
	private BeanAdapter(Class<?> beanClass) throws BeanAdapterException {
		
		if (beanClass == null) {
			throw new BeanAdapterException("Null Bean Class passed to the adapter.");
		}
		
		this.beanClass = beanClass;
		properties = new HashMap<String,PropertyDescriptor>();
		formatters = new ArrayList<SimpleDateFormat>();
		try {
			beanInfo = Introspector.getBeanInfo(this.beanClass);
			for ( PropertyDescriptor pd : beanInfo.getPropertyDescriptors() ) {
				if ("class".equals(pd.getName())) {
					continue;
				}
				
//				try {
//					Field f = bean.getClass().getDeclaredField(pd.getName());
//					
//					Annotation[] list = f.getDeclaredAnnotations();
//					
//					boolean ignore = false;
//					for (int i = 0; i < list.length; i++) {
//						//FIXME S� funcionou por class name
//						if (IgnoreField.class.getName().equals(list[i].annotationType().getName())) {
//							ignore = true;
//							continue;
//						}
//					}
//
//					if (ignore) {
//						continue;
//					}
//					
//				} catch (Exception e) {
//					//e.printStackTrace();
//				}
				
				properties.put(pd.getName(), pd);
			}
			
		} catch (IntrospectionException e) {
			throw new BeanAdapterException(e);
		}
		
		if (Map.class.isAssignableFrom(beanClass)) {
			this.map = true;
		}
		
	}

//	/**
//	 * Create and set Bean by default Bean constructor.
//	 * @param className
//	 * @throws BeanAdapterException
//	 */
//	public void setBean(String className) throws BeanAdapterException{
//		try {
//			Class<?> cls = Class.forName(className);
//			Object bean = cls.newInstance();
//			setBean(bean);
//		} catch (Exception e) {
//			throw new BeanAdapterException(e);
//		}
//	}
//
//	/**
//	 * Create and set Bean by default Bean constructor, and fill the fields based on the Map.
//	 * If the property doesn't exist, it will be ignored.
//	 * @param className
//	 * @throws BeanAdapterException
//	 */
//	public void setBean(String className, Map<String,Object> fields) throws BeanAdapterException {
//		setBean(className);
//		for (String item : fields.keySet()) {
//			String normalizedName = this.normalizeName(item);
//			if (hasBeanField(normalizedName)) {
//				this.setRawValue(normalizedName, fields.get(item));
//			}
//		}
//	}

//	/**
//	 * Set already created bean.
//	 * @param bean
//	 * @throws BeanAdapterException
//	 */
//	private void setBean(Class<?> beanClass) throws BeanAdapterException {
//		
//
//	}
	
	public boolean isValidBean(Object bean) throws BeanAdapterException {
		
		if (bean == null) {
			throw new BeanAdapterException("Null bean");
		}
		
		if (!bean.getClass().equals(beanClass)) {
			throw new BeanAdapterException("Bean class is different from bean adapter's class.");
		}
		
		return true;
		
	}
	
	
	public void addMapValue(Object bean, String property, String key, Object value) throws BeanAdapterException {
		
		throw new BeanAdapterException("implementar");
		
//		PropertyDescriptor prop = properties.get(property);
//		
//		Class<?> class1 = prop.getPropertyType();
//		Class<?> class2 = value.getClass();
//		
//		if (class1 == Map.class) {
//			
//			System.out.println("IMPLEMENTAR");
//			
//		} else {
//			System.out.println("Not Map<?> property");
//		}
//		
//		
		
	}
	
	/**
	 * Set real value accordingly to its parameter class type.
	 * @param property
	 * @param value
	 * @throws BeanAdapterException
	 */
	public void setValue(Object bean, String property, Object value) throws BeanAdapterException {
		
		if (value == null) {
			return;
		}
		
		PropertyDescriptor prop = properties.get(property);
		Class<?> class1 = prop.getPropertyType();
		Class<?> class2 = value.getClass();
		
		if (class1 == class2) {
			//Better case
			this.setRawValue(bean, property, value);
			return;
			
		}
		
		if (class1 == String.class) {
			
			//Strings as Strings...
			this.setRawValue(bean, property, value.toString());
			return;
			
		}
		
		if (class1 == Boolean.class || class1 == boolean.class) {
			
			//Boolean, the easy one...
			this.setRawValue(bean, property, Boolean.parseBoolean(value.toString()));
			return;
			
		}
		
		if (class1 == Double.class || class1 == double.class) {
			this.setRawValue(bean, property, Double.parseDouble(value.toString()));
			return;
			
		}
		
		//Number fields, very known
		else if (class2 == BigDecimal.class) {
			
			BigDecimal bigValue = (BigDecimal) value;
			
			if (class1 == Integer.class || class1 == int.class) {
				this.setRawValue(bean, property, bigValue.intValue()); 
				return;
				
			}else if (class1 == Long.class || class1 == long.class) {
				this.setRawValue(bean, property, bigValue.longValue()); 
				return;
				
			}else if (class1 == Double.class || class1 == double.class) {
				this.setRawValue(bean, property, bigValue.longValue()); 
				return;
				
			}else if (class1 == Float.class || class1 == float.class) {
				this.setRawValue(bean, property, bigValue.longValue()); 
				return;
				
			}
			
		}
		
		//Raw strings. This is probably a error, lets try to cast it
		if (class2 == String.class) {
			
			if (class1 == Integer.class || class1 == int.class) {
				this.setRawValue(bean, property, Integer.parseInt(value.toString())); 
				return;
				
			}else if (class1 == Long.class || class1 == long.class) {
				this.setRawValue(bean, property, Long.parseLong(value.toString())); 
				return;
				
			}else if (class1 == Double.class || class1 == double.class) {
				this.setRawValue(bean, property, Double.parseDouble(value.toString())); 
				return;
				
			}else if (class1 == Float.class || class1 == float.class) {
				this.setRawValue(bean, property, Float.parseFloat(value.toString())); 
				return;
			}
			
		}
		
		if (prop.getPropertyType() == Date.class && value.getClass() == String.class) {
			
			for (SimpleDateFormat sdf : formatters) {
				try {
					this.setRawValue(bean, property, sdf.parse(value.toString()));
					return;
				} catch (ParseException e) {
					//trying next...
				}
			}
				
			System.out.println("Cannot Parse any SimpleDateFormat format");
			
			
		}
		
		
		
		System.out.println("PANIC UNPLEMENTED CLASS TYPE COMBINATION");
		
		System.out.println("Property type = " + prop.getPropertyType().getName());
		System.out.println("Value    type = " + value.getClass().getName());
		System.out.println("Value is      = " + value.toString());
		
	}

	/**
	 * Set property value without casting its Type. This will throw class cast exception if type doest match.
	 * @param property
	 * @param value
	 * @throws BeanAdapterException
	 */
	public void setRawValue(Object bean, String property, Object value) throws BeanAdapterException {
		try {
			
			//FIXME Nao sabe lidar com thisIsAlreadyNormalized
			String normalizedName = this.normalizeName(property);
			//String normalizedName = property;
			
			if (properties.get(normalizedName) != null) {
				Method m = properties.get(normalizedName).getWriteMethod();
				if (m != null) {
					m.invoke(bean, value);
				}else{
					throw new BeanAdapterException("Getter method not found for property: " + normalizedName);
				}
			}else {
				throw new BeanAdapterException("Property name " + normalizedName + " not found.");
			}

		} catch (Exception e) {
			throw new BeanAdapterException(e);
		}
	}
	
	/**
	 * Return a property value or throws BeanAdapterException if property not found.
	 * @param property
	 * @return
	 * @throws BeanAdapterException
	 */
	public Object get(Object bean, String property) throws BeanAdapterException {
		if (map) {
			return getFromMap(bean, property);
		}else {
			return getFromObject(bean, property);
		}
	}
	
	private Object getFromObject(Object bean, String property) throws BeanAdapterException {
		
		try {
			PropertyDescriptor pd = properties.get(property);
			Method m = pd.getReadMethod();
			if (m != null) {
				return m.invoke(bean);
			}else{
				throw new BeanAdapterException("Getter method not found for property: " + property);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BeanAdapterException(e);
		}
	}
	
	private Object getFromMap(Object bean, String property) throws BeanAdapterException {

		Map<?,?> object = (Map<?, ?>) bean;
		return object.get(property);
	}
	
	/**
	 * Return value from recursive name with dots.
	 * @param bean
	 * @param propertyName
	 * @param recursive
	 * @return
	 * @throws BeanAdapterException
	 */
	public Object getRecursiveDotName(Object bean, String propertyName, boolean recursive) throws BeanAdapterException {
		
		if (propertyName == null) {
			return null;
		}
		
		if (recursive) {
			
			int c = propertyName.indexOf('.');
			if (c <= 0) {
				return get(bean, propertyName);
			}
			
			String data[] = new String[2];
			data[0] = propertyName.substring(0, c);
			data[1] = propertyName.substring(c+1);
			
			Object value = get(bean, data[0]);
			
			if (value != null) {
				BeanAdapter adapter = BeanAdapter.getInstance(value.getClass());
				//if (adapter.hasBeanField(data[1])) {
					return adapter.getRecursiveDotName(value, data[1], true);
				//}
			}
			
			return null;
			
		}else {
			return get(bean, propertyName);
		}
	}
	
	/**
	 * Return the expected class for the bean's property.
	 * @param property
	 * @return
	 */
	public Class<?> getPropertyType(String property) {
		PropertyDescriptor pd = properties.get(property);
		if (pd == null) {
			return null;
		}
		Method m = pd.getReadMethod();
		if (m == null) {
			return null;
		}
		return m.getReturnType();
	}

	/**
	 * Call a method name without any parameter and get their response.
	 * @param methodName
	 * @return
	 * @throws BeanAdapterException
	 */
	public Object callMethod(Object bean, String methodName) throws BeanAdapterException {
		try {
			Method m = bean.getClass().getMethod(methodName);
			return m.invoke(bean);
		}catch (Exception e) {
			throw new BeanAdapterException(e);
		}
	}

	/**
	 * Return only the fields where there is the correct get/set method declared in the Bean. 
	 * @return
	 */
	public Set<String> getBeanFields() {
		return properties.keySet();
	}

	/**
	 * Return true if Bean contains the field name.
	 * If field name is path to other properties recursively "property.property" check just the beginning of the path.
	 * @param beanFieldName
	 * @return
	 * @throws BeanAdapterException 
	 */
	public boolean hasBeanField (Object bean, String beanFieldName) throws BeanAdapterException {
		
		if (beanFieldName != null) {
			
			String key;
			int c =  beanFieldName.indexOf(".");
			if (c > 0 && beanFieldName.length() > c) {
				key = beanFieldName.substring(0, c);
			}else {
				key = beanFieldName;
			}
			
			if (map) {
				Object value = this.getFromMap(bean, key);
				if (value != null) {
					return true;
				}
			}else {
				return this.properties.containsKey(key);
			}		
			
		}
		return false;
	}

	/**
	 * Dump all bean fields
	 */
	public String toString(Object bean) {
		StringBuffer buff = new StringBuffer();
		buff.append("\n");
		Iterator<String> ite = getBeanFields().iterator();
		while (ite.hasNext()) {
			String field = ite.next();
			try {
				Object value = get(bean, field);
				if (value == null) {
					buff.append(field + " = NULL");
				}else{
					buff.append(field + " = '" + get(bean, field).toString() + "'");
				}
			} catch (BeanAdapterException e) { }
			if (ite.hasNext()) {
				//buff.append("; ");
				buff.append("\n");
			}
		}
		return "Bean properties: " + buff.toString();
	}


	public boolean isEmptyBean(Object bean) {
		for (String field: this.getBeanFields()) {
			try {
				//IF any single field is non empty, so the bean isn't empty.
				//System.out.println("isEmptyBean() Field : " + field);
				if (!this.isEmpty(this.get(bean, field))) {
					return false;
				}
			} catch (BeanAdapterException e) {
				//System.out.println("isEmptyBean() exception: " + e.getMessage());
				//In case of exception, I cannot tell you that the bean is empty.
				return false;
			}
		}
		return true;
	}
	
	@Deprecated
	//FIXME nao sei se esta em uso
	public Map<String, Object> getBeanPropertiesMap(Object bean) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		for (String field : this.getBeanFields()) {
			try {
				map.put(field, this.get(bean, field));
			} catch (BeanAdapterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return map;
		
	}

	private boolean isEmpty(Object object) {
		if (object == null) {
			//System.out.println("isEmpty? == null, true");
			return true;
		}

		Class<?> type = object.getClass();
		if (String.class.equals(type)) {
			if (object.equals("")) {
				//System.out.println("isEmpty? == \"\", true");
				return true;
			}
		} else if (Number.class.isAssignableFrom(type)) {
			
			//If nay type of number is not 0, then a float is not a 0.
			Number num = (Number) object;
			if (num.floatValue() == 0) {
				//System.out.println("isEmpty? == 0, true");
				return true;
			}
		} else if (Boolean.class.equals(type)) {
			//System.out.println("isEmpty == boolean, " + Boolean.parseBoolean(object.toString()));
			return Boolean.parseBoolean(object.toString());
		}
		
		//System.out.println("isEmpty? no more options, no.");
		return false;
	}
	
	/**
	 * Normalize name, converting "This is a Sample text" to thisIsASampleText.
	 * @param value
	 * @return
	 */
	public String normalizeName(String value) {
		
		if (value != null && value.contains(" ")) {
			return StringUtils.uncapitalize(WordUtils.capitalizeFully(value).replace(" ", ""));
		}
		
		return value;
	}
	
	public void addDateFormat(String pattern) {
		
		boolean exists = false;
		for(SimpleDateFormat sdf : formatters) {
			if (sdf.toPattern().equals(pattern)) {
				exists = true;
				break;
			}
		}
		
		if (!exists) {
			formatters.add(new SimpleDateFormat(pattern));
		}
	}

}