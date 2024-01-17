/*
 * Copyright 2021-2024 the original author or authors.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Simple {@link MethodInvocation} implementation that can also be directly used as {@link LastInvocationAware}.
 *
 * @author Oliver Drotbohm
 */
class DefaultMethodInvocation implements MethodInvocation, LastInvocationAware {

	private final Class<?> type;
	private final Method method;
	private final Object[] arguments;

	/**
	 * Creates a new {@link DefaultMethodInvocation} for the given type, method and parameters.
	 *
	 * @param type must not be {@literal null}.
	 * @param method must not be {@literal null}.
	 * @param arguments must not be {@literal null}.
	 */
	public DefaultMethodInvocation(Class<?> type, Method method, Object[] arguments) {

		Assert.notNull(type, "targetType must not be null!");
		Assert.notNull(method, "method must not be null!");
		Assert.notNull(arguments, "arguments must not be null!");

		this.type = type;
		this.method = method;
		this.arguments = arguments;
	}

	public DefaultMethodInvocation(Method method, Object[] arguments) {
		this(method.getDeclaringClass(), method, arguments);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MethodInvocation#getTargetType()
	 */
	public Class<?> getTargetType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MethodInvocation#getMethod()
	 */
	public Method getMethod() {
		return this.method;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.MethodInvocation#getArguments()
	 */
	public Object[] getArguments() {
		return this.arguments;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.LastInvocationAware#getLastInvocation()
	 */
	@Override
	public MethodInvocation getLastInvocation() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.LastInvocationAware#getObjectParameters()
	 */
	@Override
	public Iterator<Object> getObjectParameters() {
		return Collections.emptyIterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof DefaultMethodInvocation)) {
			return false;
		}

		DefaultMethodInvocation that = (DefaultMethodInvocation) o;

		return Objects.equals(this.type, that.type) //
				&& Objects.equals(this.method, that.method) //
				&& Arrays.equals(this.arguments, that.arguments);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = Objects.hash(this.type, this.method);
		result = 31 * result + Arrays.hashCode(this.arguments);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "DefaultMethodInvocation(targetType=" + this.type //
				+ ", method=" + this.method //
				+ ", arguments=" + Arrays.deepToString(this.arguments) + ")";
	}
}
