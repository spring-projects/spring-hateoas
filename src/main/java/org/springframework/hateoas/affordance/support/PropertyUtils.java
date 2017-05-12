/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.hateoas.affordance.support;

import static org.springframework.core.annotation.AnnotationUtils.*;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.ReflectionUtils;

/**
 * Convenience methods to work with Java bean properties.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 */
public final class PropertyUtils {

	/**
	 * Find all the {@link PropertyDescriptor}s and collect them into a math based on property name.
	 * 
	 * @param bean
	 * @return {@link Map} of properties
	 */
	public static Map<String, PropertyDescriptor> getPropertyDescriptors(Object bean) {

		try {
			Map<String, PropertyDescriptor> results = new HashMap<String, PropertyDescriptor>();

			for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors()) {
				results.put(propertyDescriptor.getName(), propertyDescriptor);
			}
			
			return results;
		} catch (IntrospectionException e) {
			throw new RuntimeException("failed to get property descriptors of bean " + bean, e);
		}
	}

	/**
	 * Find the constructor that has no arguments.
	 * 
	 * @param clazz - class to use for searching constructors
	 * @return
	 */
	public static Constructor<?> findDefaultConstructor(Class<?> clazz) {

		for (Constructor<?> candidate : clazz.getConstructors()) {
			if (candidate.getParameterTypes().length == 0) {
				return candidate;
			}
		}

		return null;
	}

	/**
	 * Find a {@link Constructor} that has a specific {@link Annotation} applied.
	 *
	 * @param clazz
	 * @param markerAnnotation
	 * @return
	 */
	public static Constructor<?> findConstructorByAnnotation(Class<?> clazz,
															 Class<? extends Annotation> markerAnnotation) {

		for (Constructor<?> candidate : clazz.getConstructors()) {
			if (getAnnotation(candidate, markerAnnotation) != null) {
				return candidate;
			}
		}

		return null;
	}

	/**
	 * With a given object, look up either a property value or a field name's value.
	 *
	 * TODO: Remove current method for propertyDescriptors, cache search results
	 *
	 * @param currentCallValue
	 * @param propertyOrFieldName
	 * @return
	 */
	public static Object getPropertyOrFieldValue(Object currentCallValue, String propertyOrFieldName) {

		if (currentCallValue == null) {
			return null;
		}

		Object propertyValue = getBeanPropertyValue(currentCallValue, propertyOrFieldName);

		if (propertyValue == null) {
			Field field = ReflectionUtils.findField(currentCallValue.getClass(), propertyOrFieldName);
			ReflectionUtils.makeAccessible(field);
			try {
				propertyValue = field.get(currentCallValue);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Failed to read field " + propertyOrFieldName + " from " + currentCallValue.toString(), e);
			}
		}

		return propertyValue;
	}

	/**
	 * Look up {@link java.beans.BeanInfo} for an object, and then find the getter.
	 *
	 * @param currentCallValue
	 * @param paramName
	 * @return results of {@literal paramName}'s getter or {@link null}
	 */
	private static Object getBeanPropertyValue(Object currentCallValue, String paramName) {
		
		if (currentCallValue == null) {
			return null;
		}

		try {
			for (PropertyDescriptor pd : Introspector.getBeanInfo(currentCallValue.getClass()).getPropertyDescriptors()) {
				if (paramName.equals(pd.getName())) {
					Method readMethod = pd.getReadMethod();
					if (readMethod != null) {
						ReflectionUtils.makeAccessible(readMethod);
						return readMethod.invoke(currentCallValue);
					}
				}
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException("Failed to read property " + paramName + " from " + currentCallValue.toString(), e);
		}
	}
}
