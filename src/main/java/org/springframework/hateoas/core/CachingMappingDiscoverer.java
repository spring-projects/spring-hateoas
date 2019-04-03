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
package org.springframework.hateoas.core;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.Map;

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

	private static final Map<String, String> CACHE = new ConcurrentReferenceHashMap<String, String>();

	private final MappingDiscoverer discoverer;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.Class)
	 */
	@Override
	public String getMapping(final Class<?> type) {

		String key = key(type, null);

		return getMapping(key, new CachedCall() {

			/*
			 * (non-Javadoc)
			 * @see org.springframework.hateoas.core.CachingMappingDiscoverer.CachedCall#getMapping()
			 */
			@Override
			public String getMapping() {
				return discoverer.getMapping(type);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.reflect.Method)
	 */
	@Override
	public String getMapping(final Method method) {

		String key = key(method.getDeclaringClass(), method);

		return getMapping(key, new CachedCall() {

			/*
			 * (non-Javadoc)
			 * @see org.springframework.hateoas.core.CachingMappingDiscoverer.CachedCall#getMapping()
			 */
			@Override
			public String getMapping() {
				return discoverer.getMapping(method);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	public String getMapping(final Class<?> type, final Method method) {

		String key = key(type, method);

		return getMapping(key, new CachedCall() {

			/*
			 * (non-Javadoc)
			 * @see org.springframework.hateoas.core.CachingMappingDiscoverer.CachedCall#getMapping()
			 */
			@Override
			public String getMapping() {
				return discoverer.getMapping(type, method);
			}
		});
	}

	private String getMapping(String key, CachedCall cachedCall) {

		if (CACHE.containsKey(key)) {

			return CACHE.get(key);

		} else {

			String mapping = cachedCall.getMapping();
			CACHE.put(key, mapping);

			return mapping;
		}
	}

	private interface CachedCall {
		String getMapping();
	}

	private static String key(Class<?> type, Method method) {

		StringBuilder builder = new StringBuilder(type.getName());

		if (method == null) {
			return builder.toString();
		}

		builder.append(method.getName());
		builder.append(StringUtils.arrayToCommaDelimitedString(method.getParameterTypes()));

		return builder.toString();
	}
}
