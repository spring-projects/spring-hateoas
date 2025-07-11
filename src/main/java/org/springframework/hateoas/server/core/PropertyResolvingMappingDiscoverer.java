/*
 * Copyright 2020-2024 the original author or authors.
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

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Property resolving adapter of {@link MappingDiscoverer}.
 *
 * @author Lars Michele
 * @author Oliver Drotbohm
 * @author Réda Housni Alaoui
 */
class PropertyResolvingMappingDiscoverer implements MappingDiscoverer {

	private final RawMappingDiscoverer delegate;

	PropertyResolvingMappingDiscoverer(RawMappingDiscoverer delegate) {

		Assert.notNull(delegate, "Delegate MappingDiscoverer must not be null!");

		this.delegate = delegate;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getUriMapping(java.lang.Class)
	 */
	@Nullable
	@Override
	public UriMapping getUriMapping(Class<?> type) {

		var mapping = delegate.getMapping(type, null);

		return mapping == null ? null : UriMapping.of(resolveProperties(mapping));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getUriMapping(java.lang.reflect.Method)
	 */
	@Nullable
	@Override
	public UriMapping getUriMapping(Method method) {

		var mapping = delegate.getMapping(method.getDeclaringClass(), method);

		return mapping == null ? null : UriMapping.of(resolveProperties(mapping));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getUriMapping(java.lang.Class, java.lang.reflect.Method)
	 */
	@Nullable
	@Override
	public UriMapping getUriMapping(Class<?> type, Method method) {

		var mapping = delegate.getMapping(type, method);

		return mapping == null ? null : UriMapping.of(resolveProperties(mapping));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getRequestMethod(java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	public Collection<HttpMethod> getRequestMethod(Class<?> type, Method method) {
		return delegate.getRequestMethod(type, method);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getConsumes(java.lang.reflect.Method)
	 */
	@Override
	public List<MediaType> getConsumes(Method method) {
		return delegate.getConsumes(method);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MappingDiscoverer#getParams(java.lang.reflect.Method)
	 */
	@Override
	public String[] getParams(Method method) {
		return delegate.getParams(method);
	}

	@Nullable
	private static String resolveProperties(@Nullable String mapping) {

		if (mapping == null) {
			return mapping;
		}

		WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();

		return context == null //
				? mapping //
				: context.getEnvironment().resolvePlaceholders(mapping);
	}
}
