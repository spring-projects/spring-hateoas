/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.mediatype;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.support.WebStack;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Greg Turnquist
 */
public class PropertyUtils {

	private final static HashSet<String> FIELDS_TO_IGNORE = new HashSet<>();

	static {
		FIELDS_TO_IGNORE.add("class");
		FIELDS_TO_IGNORE.add("links");
	}

	public static Map<String, Object> findProperties(@Nullable Object object) {

		if (object == null) {
			return Collections.emptyMap();
		}

		if (object.getClass().equals(EntityModel.class)) {
			return findProperties(((EntityModel<?>) object).getContent());
		}

		return getPropertyDescriptors(object.getClass()) //
				.collect(HashMap::new, //
						(hashMap, descriptor) -> {
							try {
								Method readMethod = descriptor.getReadMethod();
								ReflectionUtils.makeAccessible(readMethod);
								hashMap.put(descriptor.getName(), readMethod.invoke(object));
							} catch (IllegalAccessException | InvocationTargetException e) {
								throw new RuntimeException(e);
							}
						}, //
						HashMap::putAll);
	}

	public static List<String> findPropertyNames(ResolvableType resolvableType) {

		if (WebStack.WEBFLUX.isAvailable()) {
			if (Mono.class.equals(resolvableType.getRawClass()) || Flux.class.equals(resolvableType.getRawClass())) {
				ResolvableType generic = resolvableType.getGeneric(0);
				return findPropertyNames(generic);
			}
		}

		if (resolvableType.getRawClass() == null) {
			return Collections.emptyList();
		}

		if (resolvableType.getRawClass().equals(EntityModel.class)) {
			Class<?> genericEntityModelParameter = resolvableType.resolveGeneric(0);

			if (genericEntityModelParameter == null) {
				return Collections.emptyList();
			}

			return findPropertyNames(genericEntityModelParameter);
		} else {
			return findPropertyNames(resolvableType.getRawClass());
		}
	}

	public static List<String> findPropertyNames(Class<?> clazz) {

		return getPropertyDescriptors(clazz) //
				.map(FeatureDescriptor::getName) //
				.collect(Collectors.toList());
	}

	public static <T> T createObjectFromProperties(Class<T> clazz, Map<String, Object> properties) {

		T obj = BeanUtils.instantiateClass(clazz);

		properties.forEach((key, value) -> {
			Optional<PropertyDescriptor> possibleProperty = Optional.ofNullable(BeanUtils.getPropertyDescriptor(clazz, key));
			possibleProperty.ifPresent(property -> {
				try {
					Method writeMethod = property.getWriteMethod();
					ReflectionUtils.makeAccessible(writeMethod);
					writeMethod.invoke(obj, value);
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			});
		});

		return obj;
	}

	/**
	 * Take a {@link Class} and find all properties that are NOT to be ignored, and return them as a {@link Stream}.
	 * 
	 * @param clazz
	 * @return
	 */
	private static Stream<PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) {

		return Arrays.stream(BeanUtils.getPropertyDescriptors(clazz))
				.filter(descriptor -> !FIELDS_TO_IGNORE.contains(descriptor.getName()))
				.filter(descriptor -> !descriptorToBeIgnoredByJackson(clazz, descriptor))
				.filter(descriptor -> !toBeIgnoredByJackson(clazz, descriptor.getName()))
				.filter(descriptor -> !readerIsNotToBeIgnoredByJackson(descriptor));
	}

	/**
	 * Check if a given {@link PropertyDescriptor} has {@link JsonIgnore} applied to the field declaration.
	 *
	 * @param clazz
	 * @param descriptor
	 * @return
	 */
	private static boolean descriptorToBeIgnoredByJackson(Class<?> clazz, PropertyDescriptor descriptor) {

		Field descriptorField = ReflectionUtils.findField(clazz, descriptor.getName());

		if (descriptorField == null) {
			return false;
		}

		return toBeIgnoredByJackson(AnnotationUtils.getAnnotations(descriptorField));
	}

	/**
	 * Check if a given {@link PropertyDescriptor} has {@link JsonIgnore} on the getter.
	 * 
	 * @param descriptor
	 * @return
	 */
	private static boolean readerIsNotToBeIgnoredByJackson(PropertyDescriptor descriptor) {
		return toBeIgnoredByJackson(AnnotationUtils.getAnnotations(descriptor.getReadMethod()));
	}

	/**
	 * Scan a list of {@link Annotation}s for {@link JsonIgnore} annotations.
	 * 
	 * @param annotations
	 * @return
	 */
	private static boolean toBeIgnoredByJackson(@Nullable Annotation[] annotations) {

		if (annotations == null) {
			return false;
		}

		return Arrays.stream(annotations) //
				.filter(annotation -> annotation.annotationType().equals(JsonIgnore.class)) //
				.findFirst() //
				.map(annotation -> (Boolean) AnnotationUtils.getAnnotationAttributes(annotation).get("value")) //
				.orElse(false);
	}

	/**
	 * Check if a field name is to be ignored due to {@link JsonIgnoreProperties}.
	 * 
	 * @param clazz
	 * @param field
	 * @return
	 */
	private static boolean toBeIgnoredByJackson(Class<?> clazz, String field) {

		Annotation[] annotations = AnnotationUtils.getAnnotations(clazz);

		if (annotations == null) {
			return false;
		}

		return Arrays.stream(annotations) //
				.filter(annotation -> annotation.annotationType().equals(JsonIgnoreProperties.class)) //
				.map(annotation -> (String[]) AnnotationUtils.getAnnotationAttributes(annotation).get("value")) //
				.flatMap(Arrays::stream) //
				.anyMatch(propertyName -> propertyName.equalsIgnoreCase(field));
	}

}
