/*
 * Copyright 2012-2017 the original author or authors.
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

import static org.springframework.core.annotation.AnnotatedElementUtils.*;
import static org.springframework.core.annotation.AnnotationUtils.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * {@link MappingDiscoverer} implementation that inspects mappings from a particular annotation.
 * 
 * @author Oliver Gierke
 * @author Mark Paluch
 * @author Greg Turnquist
 */
public class AnnotationMappingDiscoverer implements MappingDiscoverer {

	private static final Pattern MULTIPLE_SLASHES = Pattern.compile("\\/{2,}");

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

		Assert.notNull(annotation, "Annotation must not be null!");

		this.annotationType = annotation;
		this.mappingAttributeName = mappingAttributeName;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.Class)
	 */
	@Override
	public String getMapping(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");

		String[] mapping = getMappingFrom(findMergedAnnotation(type, annotationType));

		return mapping.length == 0 ? null : mapping[0];
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.reflect.Method)
	 */
	@Override
	public String getMapping(Method method) {

		Assert.notNull(method, "Method must not be null!");
		return getMapping(method.getDeclaringClass(), method);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	public String getMapping(Class<?> type, Method method) {

		Assert.notNull(type, "Type must not be null!");
		Assert.notNull(method, "Method must not be null!");

		String[] mapping = getMappingFrom(findMergedAnnotation(method, annotationType));
		String typeMapping = getMapping(type);

		if (mapping == null || mapping.length == 0) {
			return typeMapping;
		}

		return typeMapping == null || "/".equals(typeMapping) ? mapping[0] : join(typeMapping, mapping[0]);
	}

	/**
	 * Extract {@link org.springframework.web.bind.annotation.RequestMapping}'s list of {@link RequestMethod}s into an
	 * array of {@link String}s.
	 * 
	 * @param type
	 * @param method
	 * @return
	 */
	@Override
	public Collection<HttpMethod> getRequestMethod(Class<?> type, Method method) {

		Assert.notNull(type, "Type must not be null!");
		Assert.notNull(method, "Method must not be null!");

		Annotation mergedAnnotation = findMergedAnnotation(method, annotationType);
		Object value = getValue(mergedAnnotation, "method");

		RequestMethod[] requestMethods = (RequestMethod[]) value;

		List<HttpMethod> requestMethodNames = new ArrayList<HttpMethod>();

		for (RequestMethod requestMethod : requestMethods) {
			requestMethodNames.add(HttpMethod.valueOf(requestMethod.toString()));
		}

		return requestMethodNames;
	}

	private String[] getMappingFrom(Annotation annotation) {

		if (annotation == null) {
			return new String[0];
		}

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

	/**
	 * Joins the given mappings making sure exactly one slash.
	 * 
	 * @param typeMapping must not be {@literal null} or empty.
	 * @param mapping must not be {@literal null} or empty.
	 * @return
	 */
	private static String join(String typeMapping, String mapping) {
		return MULTIPLE_SLASHES.matcher(typeMapping.concat("/").concat(mapping)).replaceAll("/");
	}
}
