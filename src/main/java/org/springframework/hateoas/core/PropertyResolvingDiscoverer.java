/*
 * Copyright 2017-2019 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.core.env.PropertyResolver;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

/**
 * Take any other {@link MappingDiscoverer} and wrap it with an attempt to resolve properties via {@link PropertyResolver}.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
public class PropertyResolvingDiscoverer implements MappingDiscoverer {

	private final MappingDiscoverer discoverer;
	private final PropertyResolver resolver;

	public PropertyResolvingDiscoverer(MappingDiscoverer discoverer, PropertyResolver resolver) {

		Assert.notNull(discoverer, "MappingDiscoverer must not be null!");
		Assert.notNull(resolver, "PropertyResolver must not be null!");

		this.discoverer = discoverer;
		this.resolver = resolver;
	}

	/**
	 * Returns the mapping associated with the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @return the type-level mapping or {@literal null} in case none is present.
	 */
	@Override
	public String getMapping(Class<?> type) {
		return attemptToResolve(this.discoverer.getMapping(type));
	}

	/**
	 * Returns the mapping associated with the given {@link Method}. This will include the type-level mapping.
	 *
	 * @param method must not be {@literal null}.
	 * @return the method mapping including the type-level one or {@literal null} if neither of them present.
	 */
	@Override
	public String getMapping(Method method) {
		return attemptToResolve(this.discoverer.getMapping(method));
	}

	/**
	 * Returns the mapping for the given {@link Method} invoked on the given type. This can be used to calculate the
	 * mapping for a super type method being invoked on a sub-type with a type mapping.
	 *
	 * @param type must not be {@literal null}.
	 * @param method must not be {@literal null}.
	 * @return the method mapping including the type-level one or {@literal null} if neither of them present.
	 */
	@Override
	public String getMapping(Class<?> type, Method method) {
		return attemptToResolve(this.discoverer.getMapping(type, method));
	}

	/**
	 * Returns the HTTP verbs for the given {@link Method} invoked on the given type. This can be used to build hypermedia
	 * templates.
	 *
	 * @param type
	 * @param method
	 * @return
	 */
	@Override
	public Collection<HttpMethod> getRequestMethod(Class<?> type, Method method) {
		return this.discoverer.getRequestMethod(type, method);
	}

	/**
	 * Use the {@link PropertyResolver} to substitute values into the link.
	 *
	 * @param value
	 * @return
	 */
	private String attemptToResolve(String value) {
		return this.resolver.resolvePlaceholders(value);
	}
}
