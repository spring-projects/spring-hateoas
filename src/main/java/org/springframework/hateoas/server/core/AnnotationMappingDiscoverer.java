/*
 * Copyright 2012-2024 the original author or authors.
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
package org.springframework.hateoas.server.core;

import static org.springframework.core.annotation.AnnotatedElementUtils.*;
import static org.springframework.core.annotation.AnnotationUtils.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * {@link MappingDiscoverer} implementation that inspects mappings from a particular annotation.
 *
 * @author Oliver Gierke
 * @author Mark Paluch
 * @author Greg Turnquist
 * @author Réda Housni Alaoui
 */
public class AnnotationMappingDiscoverer implements RawMappingDiscoverer {

	private final Class<? extends Annotation> annotationType;
	private final @Nullable String mappingAttributeName;

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
	public AnnotationMappingDiscoverer(Class<? extends Annotation> annotation, @Nullable String mappingAttributeName) {

		Assert.notNull(annotation, "Annotation must not be null!");

		this.annotationType = annotation;
		this.mappingAttributeName = mappingAttributeName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getUriMapping(java.lang.Class)
	 */
	@Override
	@Nullable
	public UriMapping getUriMapping(Class<?> type) {
		return UriMapping.of(getMapping(type));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getUriMapping(java.lang.reflect.Method)
	 */
	@Override
	@Nullable
	public UriMapping getUriMapping(Method method) {

		Assert.notNull(method, "Method must not be null!");

		return getUriMapping(method.getDeclaringClass(), method);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getUriMapping(java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	@Nullable
	public UriMapping getUriMapping(Class<?> type, Method method) {

		var mapping = getMapping(type, method);

		return mapping == null ? null : UriMapping.of(cleanup(mapping));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.RawMappingDiscoverer#getMapping(java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	public @Nullable String getMapping(@Nullable Class<?> type, @Nullable Method method) {

		String[] mapping = method == null ? new String[0] : getMappingFrom(findMergedAnnotation(method, annotationType));
		String typeMapping = getMapping(type);

		if (mapping.length == 0) {
			return typeMapping;
		}

		var result = typeMapping == null || "/".equals(typeMapping)
				? mapping[0]
				: join(typeMapping, mapping[0]);

		return cleanup(result);
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

		if (requestMethods == null) {
			return Collections.emptyList();
		}

		List<HttpMethod> requestMethodNames = new ArrayList<>();

		for (RequestMethod requestMethod : requestMethods) {
			requestMethodNames.add(HttpMethod.valueOf(requestMethod.toString()));
		}

		return requestMethodNames;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getConsumes(java.lang.reflect.Method)
	 */
	@Override
	public List<MediaType> getConsumes(Method method) {

		Annotation annotation = findMergedAnnotation(method, annotationType);
		String[] mediaTypes = (String[]) getValue(annotation, "consumes");

		return mediaTypes == null
				? Collections.emptyList()
				: Arrays.stream(mediaTypes).map(MediaType::parseMediaType).collect(Collectors.toList());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getParams(java.lang.reflect.Method)
	 */
	@Override
	public String[] getParams(Method method) {

		Annotation annotation = findMergedAnnotation(method, annotationType);
		String[] params = (String[]) getValue(annotation, "params");

		return params == null ? new String[0] : params;
	}

	private @Nullable String getMapping(@Nullable Class<?> type) {

		if (type == null) {
			return null;
		}

		String[] mapping = getMappingFrom(findMergedAnnotation(type, annotationType));

		return mapping.length == 0 ? null : mapping[0];
	}

	private String[] getMappingFrom(@Nullable Annotation annotation) {

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
		return mapping.isBlank() ? typeMapping : typeMapping.concat("/").concat(mapping);
	}

	/**
	 * @param mapping
	 * @return
	 */
	private static String cleanup(String mapping) {

		String[] parts = mapping.split("/");
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < parts.length; i++) {

			String part = parts[i];

			if (!StringUtils.hasText(part)) {
				continue;
			}

			if (i != 0) {
				result.append("/");
			}

			result.append(part.contains(":") ? cleanupPart(part) : part);
		}

		return (mapping.endsWith("/") ? result.append("/") : result).toString();
	}

	private static String cleanupPart(String part) {

		StringBuilder builder = new StringBuilder();
		int level = 0;
		boolean inRegex = false;

		for (int i = 0; i < part.length(); i++) {

			char character = part.charAt(i);

			if (character == '{') {

				level++;

				if (level == 1) {
					builder.append(character);
					continue;
				}
			}

			if (level == 1 && character == ':') {
				inRegex = true;
			}

			if (character == '}') {

				level--;

				if (level == 0) {
					inRegex = false;
				}
			}

			if (!inRegex) {
				builder.append(character);
			}
		}

		return builder.toString();
	}
}
