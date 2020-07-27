/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import static java.util.Optional.ofNullable;

import org.springframework.core.env.PropertyResolver;
import org.springframework.hateoas.server.core.MappingDiscoverer;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.web.context.ContextLoader;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Property resolving adapter of {@link MappingDiscoverer}.
 *
 * @author Lars Michele
 */
public class PropertyResolvingMappingDiscoverer implements MappingDiscoverer {

	private final MappingDiscoverer delegate;

	private PropertyResolvingMappingDiscoverer(MappingDiscoverer delegate) {
		this.delegate = delegate;
	}

	public static PropertyResolvingMappingDiscoverer of(MappingDiscoverer delegate) {
		return new PropertyResolvingMappingDiscoverer(delegate);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.Class)
	 */
	@Nullable
	@Override
	public String getMapping(Class<?> type) {
		return ofNullable(delegate.getMapping(type)).map(getPropertyResolver()::resolvePlaceholders).orElse(null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.reflect.Method)
	 */
	@Nullable
	@Override
	public String getMapping(Method method) {
		return ofNullable(delegate.getMapping(method)).map(getPropertyResolver()::resolvePlaceholders).orElse(null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getMapping(java.lang.Class, java.lang.reflect.Method)
	 */
	@Nullable
	@Override
	public String getMapping(Class<?> type, Method method) {
		return ofNullable(delegate.getMapping(type, method)).map(getPropertyResolver()::resolvePlaceholders).orElse(null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.MappingDiscoverer#getRequestMethod(java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	public Collection<HttpMethod> getRequestMethod(Class<?> type, Method method) {
		return delegate.getRequestMethod(type, method);
	}

	private static PropertyResolver getPropertyResolver() {
		return ContextLoader.getCurrentWebApplicationContext().getEnvironment();
	}
}
