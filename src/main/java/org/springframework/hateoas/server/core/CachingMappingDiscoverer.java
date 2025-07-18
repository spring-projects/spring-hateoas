/*
 * Copyright 2019-2024 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

/**
 * Caching adapter of {@link MappingDiscoverer}.
 *
 * @author Michal Stochmialek
 * @author Oliver Drotbohm
 * @author Réda Housni Alaoui
 */
public class CachingMappingDiscoverer implements MappingDiscoverer {

	private static final Map<String, UriMapping> MAPPINGS = new ConcurrentReferenceHashMap<>();
	private static final Map<String, Collection<HttpMethod>> METHODS = new ConcurrentReferenceHashMap<>();
	private static final Map<String, String[]> PARAMS = new ConcurrentReferenceHashMap<>();
	private static final Map<String, List<MediaType>> CONSUMES = new ConcurrentReferenceHashMap<>();

	private final MappingDiscoverer delegate;

	private CachingMappingDiscoverer(MappingDiscoverer delegate) {
		this.delegate = delegate;
	}

	public static CachingMappingDiscoverer of(MappingDiscoverer delegate) {
		return new CachingMappingDiscoverer(delegate);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getUriMapping(java.lang.Class)
	 */
	@Nullable
	@Override
	public UriMapping getUriMapping(Class<?> type) {

		String key = key(type, null);

		return MAPPINGS.computeIfAbsent(key, __ -> delegate.getUriMapping(type));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getUriMapping(java.lang.reflect.Method)
	 */
	@Nullable
	@Override
	public UriMapping getUriMapping(Method method) {
		return MAPPINGS.computeIfAbsent(key(method), __ -> delegate.getUriMapping(method));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getUriMapping(java.lang.Class, java.lang.reflect.Method)
	 */
	@Nullable
	@Override
	public UriMapping getUriMapping(Class<?> type, Method method) {

		String key = key(type, method);

		return MAPPINGS.computeIfAbsent(key, __ -> delegate.getUriMapping(type, method));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getRequestMethod(java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	public Collection<HttpMethod> getRequestMethod(Class<?> type, Method method) {
		return METHODS.computeIfAbsent(key(type, method), __ -> delegate.getRequestMethod(type, method));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getConsumes(java.lang.reflect.Method)
	 */
	@Override
	public List<MediaType> getConsumes(Method method) {
		return CONSUMES.computeIfAbsent(key(method), __ -> delegate.getConsumes(method));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getParams(java.lang.reflect.Method)
	 */
	@Override
	public String[] getParams(Method method) {
		return PARAMS.computeIfAbsent(key(method), __ -> delegate.getParams(method));
	}

	private static String key(Method method) {
		return key(method.getDeclaringClass(), method);
	}

	private static String key(Class<?> type, @Nullable Method method) {

		StringBuilder builder = new StringBuilder(type.getName());

		if (method == null) {
			return builder.toString();
		}

		builder.append(method.getName());
		builder.append(StringUtils.arrayToCommaDelimitedString(method.getParameterTypes()));

		return builder.toString();
	}
}
