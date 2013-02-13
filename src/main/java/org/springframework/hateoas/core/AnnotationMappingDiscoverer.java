/*
 * Copyright 2012 the original author or authors.
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

import org.springframework.util.Assert;

/**
 * {@link MappingDiscoverer} implementation that inspects mappings from a particular annotation.
 * 
 * @author Oliver Gierke
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

		String[] mapping = getMappingFrom(findAnnotation(type, annotationType));

		if (mapping.length > 1) {
			throw new IllegalStateException(String.format("Multiple class level mappings defined on class %s!",
					type.getName()));
		}

		return mapping.length == 0 ? null : mapping[0];
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.reflect.Method)
	 */
	@Override
	public String getMapping(Method method) {

		String[] mapping = getMappingFrom(findAnnotation(method, annotationType));

		if (mapping.length > 1) {
			throw new IllegalStateException(String.format("Multiple method level mappings defined on method %s!",
					method.toString()));
		}

		String typeMapping = getMapping(method.getDeclaringClass());

		if (mapping == null || mapping.length == 0) {
			return typeMapping;
		}

		return typeMapping == null ? mapping[0] : typeMapping + mapping[0];
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
}
