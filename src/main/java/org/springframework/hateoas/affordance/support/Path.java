/*
 * Copyright 2013-2017 the original author or authors.
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

package org.springframework.hateoas.affordance.support;

import static org.springframework.hateoas.core.DummyInvocationUtils.*;

import java.util.Collection;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.core.DummyInvocationUtils.*;
import org.springframework.util.Assert;

/**
 * Callbacks to build up path to various attributes of an affordance definition.
 *
 * @see {@link org.springframework.hateoas.core.DummyInvocationUtils}
 * 
 * @author Dietrich Schulten
 * @author Greg Turnquist
 */
public class Path {

	static ThreadLocal<InvocationRecordingMethodInterceptor> interceptorThreadLocal = new ThreadLocal<InvocationRecordingMethodInterceptor>();

	public static <T> T on(Class<T> type) {
		return on(type, true);
	}

	public static <T> T on(Class<T> type, boolean init) {

		if (init) {
			interceptorThreadLocal.remove();
			interceptorThreadLocal.set(new InvocationRecordingMethodInterceptor(type));
		}
		return getProxyWithInterceptor(type, interceptorThreadLocal.get(), type.getClassLoader());
	}

	public static <T> T on(Class<T> type, InvocationRecordingMethodInterceptor rmi) {
		return getProxyWithInterceptor(type, rmi, type.getClassLoader());
	}

	public static String path(Object obj) {

		InvocationRecordingMethodInterceptor interceptor = interceptorThreadLocal.get();
		Assert.notNull(interceptor, "Path.on(Class) should be called first");

		interceptorThreadLocal.remove();
		return interceptor.getLastInvocation().toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> T collection(Collection<? extends T> collection) {

		Assert.isInstanceOf(LastInvocationAware.class, collection);

		ResolvableType resolvable = ResolvableType
				.forMethodReturnType(((LastInvocationAware) collection).getLastInvocation().getMethod());

		return on((Class<T>) resolvable.getGeneric(0).getRawClass(), false);
	}

}
