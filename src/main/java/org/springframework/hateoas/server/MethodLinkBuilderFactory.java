/*
 * Copyright 2012-2014 the original author or authors.
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
package org.springframework.hateoas.server;

import java.lang.reflect.Method;

import org.springframework.hateoas.server.core.DummyInvocationUtils;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

/**
 * Extension of {@link LinkBuilderFactory} for implementations that also support creating {@link LinkBuilder}s by
 * pointing to a method.
 *
 * @author Oliver Gierke
 */
public interface MethodLinkBuilderFactory<T extends LinkBuilder> extends LinkBuilderFactory<T> {

	/**
	 * Returns a {@link LinkBuilder} pointing to the URI mapped to the given {@link Method} and expanding this mapping
	 * using the given parameters.
	 *
	 * @param method must not be {@literal null}.
	 * @param parameters
	 * @return
	 */
	T linkTo(Method method, Object... parameters);

	/**
	 * Returns a {@link LinkBuilder} pointing to the URI mapped to the given {@link Method} assuming it was invoked on an
	 * object of the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @param method must not be {@literal null}.
	 * @param parameters
	 * @return
	 */
	T linkTo(Class<?> type, Method method, Object... parameters);

	/**
	 * Returns a {@link LinkBuilder} pointing to the URI mapped to the method the result is handed into this method. Use
	 * {@link DummyInvocationUtils#methodOn(Class, Object...)} to obtain a dummy instance of a controller to record a
	 * dummy method invocation on. See {@link WebMvcLinkBuilder#linkTo(Object)} for an example.
	 *
	 * @see WebMvcLinkBuilder#linkTo(Object)
	 * @param methodInvocationResult must not be {@literal null}.
	 * @return
	 */
	T linkTo(Object methodInvocationResult);
}
