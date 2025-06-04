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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Strategy interface to discover a URI mapping and related {@link org.springframework.hateoas.Affordance}s for either a
 * given type or method.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author Réda Housni Alaoui
 */
public interface MappingDiscoverer {

	/**
	 * Returns the mapping associated with the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @return the type-level mapping or {@literal null} in case none is present.
	 * @since 2.0
	 */
	@Nullable
	UriMapping getUriMapping(Class<?> type);

	/**
	 * Returns the mapping associated with the given {@link Method}. This will include the type-level mapping.
	 *
	 * @param method must not be {@literal null}.
	 * @return the method mapping including the type-level one or {@literal null} if neither of them present.
	 * @since 2.0
	 */
	@Nullable
	UriMapping getUriMapping(Method method);

	/**
	 * Returns the mapping for the given {@link Method} invoked on the given type. This can be used to calculate the
	 * mapping for a super type method being invoked on a sub-type with a type mapping.
	 *
	 * @param type must not be {@literal null}.
	 * @param method must not be {@literal null}.
	 * @return the method mapping including the type-level one or {@literal null} if neither of them present.
	 * @since 2.0
	 */
	@Nullable
	UriMapping getUriMapping(Class<?> type, Method method);

	/**
	 * Returns the HTTP verbs for the given {@link Method} invoked on the given type. This can be used to build hypermedia
	 * templates.
	 *
	 * @param type
	 * @param method
	 * @return
	 */
	Collection<HttpMethod> getRequestMethod(Class<?> type, Method method);

	/**
	 * Returns the {@link MediaType}s supported for consumption.
	 *
	 * @param method must not be {@literal null}.
	 * @return can be {@literal null}.
	 * @since 1.3
	 */
	List<MediaType> getConsumes(Method method);

	/**
	 * Returns the statically declared request parameters.
	 *
	 * @param method must not be {@literal null}.
	 * @return the parameters of the mapped request, narrowing the primary mapping.
	 * @since 1.4
	 */
	String[] getParams(Method method);
}
