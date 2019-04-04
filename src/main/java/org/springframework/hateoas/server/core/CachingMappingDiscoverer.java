/*
 * Copyright 2019 the original author or authors.
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

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

/**
 * Caching adapter of {@link MappingDiscoverer}.
 *
 * @author Michal Stochmialek
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor(staticName = "of")
public class CachingMappingDiscoverer implements MappingDiscoverer {

	private static final Map<String, String> MAPPINGS = new ConcurrentReferenceHashMap<>();
	private static final Map<String, Collection<HttpMethod>> METHODS = new ConcurrentReferenceHashMap<>();

	private final MappingDiscoverer delegate;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.Class)
	 */
	@Nullable
	@Override
	public String getMapping(Class<?> type) {

		String key = key(type, null);

		return MAPPINGS.computeIfAbsent(key, __ -> delegate.getMapping(type));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.reflect.Method)
	 */
	@Nullable
	@Override
	public String getMapping(Method method) {

		String key = key(method.getDeclaringClass(), method);

		return MAPPINGS.computeIfAbsent(key, __ -> delegate.getMapping(method));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.Class, java.lang.reflect.Method)
	 */
	@Nullable
	@Override
	public String getMapping(Class<?> type, Method method) {

		String key = key(type, method);

		return MAPPINGS.computeIfAbsent(key, __ -> delegate.getMapping(type, method));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getRequestMethod(java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	public Collection<HttpMethod> getRequestMethod(Class<?> type, Method method) {
		return METHODS.computeIfAbsent(key(type, method), __ -> delegate.getRequestMethod(type, method));
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
