/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.support;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Greg Turnquist
 */
public class PropertyUtils {

	private final static HashSet<String> FIELDS_TO_IGNORE = new HashSet<String>() {{
		add("class");
		add("links");
	}};
	
	public static Map<String, Object> findProperties(Object object) {

		if (object.getClass().equals(Resource.class)) {
			return findProperties(((Resource<?>) object).getContent());
		}

		return Arrays.asList(BeanUtils.getPropertyDescriptors(object.getClass())).stream()
			.filter(descriptor -> !FIELDS_TO_IGNORE.contains(descriptor.getName()))
			.filter(descriptor -> hasJsonIgnoreOnTheField(object.getClass(), descriptor))
			.filter(PropertyUtils::hasJsonIgnoreOnTheReader)
			.collect(Collectors.toMap(
				FeatureDescriptor::getName,
				descriptor -> {
					try {
						return descriptor.getReadMethod().invoke(object);
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}));
	}

	public static List<String> findProperties(ResolvableType resolvableType) {

		if (resolvableType.getRawClass().equals(Resource.class)) {
			return findProperties(resolvableType.resolveGeneric(0));
		} else {
			return findProperties(resolvableType.getRawClass());
		}
	}

	public static List<String> findProperties(Class<?> clazz) {

		return Arrays.asList(BeanUtils.getPropertyDescriptors(clazz)).stream()
			.filter(descriptor -> !FIELDS_TO_IGNORE.contains(descriptor.getName()))
			.filter(descriptor -> hasJsonIgnoreOnTheField(clazz, descriptor))
			.filter(PropertyUtils::hasJsonIgnoreOnTheReader)
			.map(FeatureDescriptor::getName)
			.collect(Collectors.toList());
	}

	public static Object createObjectFromProperties(Class<?> clazz, Map<String, Object> properties) {
		
		Object obj = BeanUtils.instantiateClass(clazz);

		properties.entrySet().stream().forEach(entry -> {
			Optional<PropertyDescriptor> possibleProperty = Optional.ofNullable(BeanUtils.getPropertyDescriptor(clazz, entry.getKey()));
			possibleProperty.ifPresent(property -> {
				try {
					Method writeMethod = property.getWriteMethod();
					ReflectionUtils.makeAccessible(writeMethod);
					writeMethod.invoke(obj, entry.getValue());
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			});
		});

		return obj;
	}

	/**
	 * Check if a given {@link PropertyDescriptor} has {@link JsonIgnore} applied to the field declaration.
	 *
	 * @param object
	 * @param descriptor
	 * @return
	 */
	private static boolean hasJsonIgnoreOnTheField(Class<?> clazz, PropertyDescriptor descriptor) {

		Field descriptorField = ReflectionUtils.findField(clazz, descriptor.getName());

		return isToBeIgnored(AnnotationUtils.getAnnotations(descriptorField));
	}

	/**
	 * Check if a given {@link PropertyDescriptor} has {@link JsonIgnore} on the getter.
	 * 
	 * @param object
	 * @param descriptor
	 * @return
	 */
	private static boolean hasJsonIgnoreOnTheReader(PropertyDescriptor descriptor) {
		return isToBeIgnored(AnnotationUtils.getAnnotations(descriptor.getReadMethod()));
	}

	/**
	 * Scan a list of {@link Annotation}s for {@link JsonIgnore} annotations.
	 * 
	 * @param annotations
	 * @return
	 */
	private static boolean isToBeIgnored(Annotation[] annotations) {

		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.annotationType().equals(JsonIgnore.class)) {
					return !(Boolean) AnnotationUtils.getAnnotationAttributes(annotation).get("value");
				}
			}
		}

		return true;
	}

}
