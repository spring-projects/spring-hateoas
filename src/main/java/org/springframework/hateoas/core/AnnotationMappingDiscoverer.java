/*
 * Copyright 2012-2014 the original author or authors.
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
package org.springframework.hateoas.core;

import static org.springframework.core.annotation.AnnotationUtils.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link MappingDiscoverer} implementation that inspects mappings from a particular annotation.
 * 
 * @author Oliver Gierke
 * @author Josh Ghiloni
 */
public class AnnotationMappingDiscoverer implements MappingDiscoverer {

	private final Class<? extends Annotation> annotationType;
	private final String mappingAttributeName;

	/**
	 * Creates an {@link AnnotationMappingDiscoverer} for the given annotation type. Will lookup the {@code value}
	 * attribute by default.
	 * 
	 * @param annotation must not be {@literal null}.
	 */
	public AnnotationMappingDiscoverer(Class<? extends Annotation> annotation) {
		this(annotation, null);
	}

	/**
	 * Creates an {@link AnnotationMappingDiscoverer} for the given annotation type and attribute name.
	 * 
	 * @param annotation must not be {@literal null}.
	 * @param mappingAttributeName if {@literal null}, it defaults to {@code value}.
	 */
	public AnnotationMappingDiscoverer(Class<? extends Annotation> annotation, String mappingAttributeName) {

		Assert.notNull(annotation);

		this.annotationType = annotation;
		this.mappingAttributeName = mappingAttributeName;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.Class)
	 */
	@Override
	public String getMapping(Class<?> type) {
		return getMapping(null, type);
	}
	
	public String getMapping(Environment environment, Class<?> type) {

		Assert.notNull(type, "Type must not be null!");

		String[] mapping = getMappingFrom(findAnnotation(type, annotationType));

		if (mapping.length > 1) {
			throw new IllegalStateException(String.format("Multiple class level mappings defined on class %s!",
					type.getName()));
		}

		return mapping.length == 0 ? null : maybeResolveValue(environment, mapping[0]);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.reflect.Method)
	 */
	@Override
	public String getMapping(Method method) {
		return getMapping(method, null);
	}
	
	public String getMapping(Method method, Environment environment) {
		Assert.notNull(method, "Method must not be null!");
		return getMapping(method.getDeclaringClass(), method, environment);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	public String getMapping(Class<?> type, Method method) {
		return getMapping(type, method, null);
	}
	
	public String getMapping(Class<?> type, Method method, Environment environment) {

		Assert.notNull(type, "Type must not be null!");
		Assert.notNull(method, "Method must not be null!");

		String[] mapping = getMappingFrom(findAnnotation(method, annotationType));

		if (mapping.length > 1) {
			throw new IllegalStateException(String.format("Multiple method level mappings defined on method %s!",
					method.toString()));
		}

		String typeMapping = getMapping(type);

		if (mapping == null || mapping.length == 0) {
			return typeMapping;
		}

		String returnValue = (typeMapping == null || "/".equals(typeMapping) ? mapping[0] : typeMapping + mapping[0]);
		return maybeResolveValue(environment, returnValue);
	}

	private String[] getMappingFrom(Annotation annotation) {

		Object value = mappingAttributeName == null ? getValue(annotation) : getValue(annotation, mappingAttributeName);

		if (value instanceof String) {
			return new String[] { (String) value };
		} else if (value instanceof String[]) {
			return (String[]) value;
		} else if (value == null) {
			return new String[0];
		}

		throw new IllegalStateException(String.format(
				"Unsupported type for the mapping attribute! Support String and String[] but got %s!", value.getClass()));
	}
	
	private String maybeResolveValue(Environment environment, String value) {
		if (environment != null && StringUtils.hasText(value)) {
			value = environment.resolvePlaceholders(value);
		}
		
		return value;
	}
}
