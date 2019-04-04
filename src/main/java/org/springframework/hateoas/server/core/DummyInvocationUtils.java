/*
 * Copyright 2012-2019 the original author or authors.
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

import lombok.NonNull;
import lombok.Value;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Utility methods to capture dummy method invocations.
 *
 * @author Oliver Gierke
 */
public class DummyInvocationUtils {

	/**
	 * Method interceptor that records the last method invocation and creates a proxy for the return value that exposes
	 * the method invocation.
	 *
	 * @author Oliver Gierke
	 */
	private static class InvocationRecordingMethodInterceptor implements MethodInterceptor, LastInvocationAware {

		private static final Method GET_INVOCATIONS;
		private static final Method GET_OBJECT_PARAMETERS;

		private final Class<?> targetType;
		private final Object[] objectParameters;
		private MethodInvocation invocation;

		static {
			GET_INVOCATIONS = ReflectionUtils.findMethod(LastInvocationAware.class, "getLastInvocation");
			GET_OBJECT_PARAMETERS = ReflectionUtils.findMethod(LastInvocationAware.class, "getObjectParameters");
		}

		/**
		 * Creates a new {@link InvocationRecordingMethodInterceptor} carrying the given parameters forward that might be
		 * needed to populate the class level mapping.
		 *
		 * @param targetType must not be {@literal null}.
		 * @param parameters must not be {@literal null}.
		 */
		InvocationRecordingMethodInterceptor(Class<?> targetType, Object... parameters) {

			Assert.notNull(targetType, "Target type must not be null!");
			Assert.notNull(parameters, "Parameters must not be null!");

			this.targetType = targetType;
			this.objectParameters = parameters.clone();
		}

		/*
		 * (non-Javadoc)
		 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		public Object invoke(org.aopalliance.intercept.MethodInvocation invocation) {

			Method method = invocation.getMethod();

			if (GET_INVOCATIONS.equals(method)) {
				return getLastInvocation();
			} else if (GET_OBJECT_PARAMETERS.equals(method)) {
				return getObjectParameters();
			} else if (ReflectionUtils.isObjectMethod(method)) {
				return ReflectionUtils.invokeMethod(method, invocation.getThis(), invocation.getArguments());
			}

			this.invocation = new SimpleMethodInvocation(targetType, method, invocation.getArguments());

			Class<?> returnType = method.getReturnType();
			ClassLoader classLoader = method.getDeclaringClass().getClassLoader();

			return returnType.cast(getProxyWithInterceptor(returnType, this, classLoader));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware#getLastInvocation()
		 */
		@Override
		public MethodInvocation getLastInvocation() {
			return invocation;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware#getObjectParameters()
		 */
		@Override
		public Iterator<Object> getObjectParameters() {
			return Arrays.asList(objectParameters).iterator();
		}
	}

	/**
	 * Returns a proxy of the given type, backed by an {@link EmptyTargetSource} to simply drop method invocations but
	 * equips it with an {@link InvocationRecordingMethodInterceptor}. The interceptor records the last invocation and
	 * returns a proxy of the return type that also implements {@link LastInvocationAware} so that the last method
	 * invocation can be inspected. Parameters passed to the subsequent method invocation are generally neglected except
	 * the ones that might be mapped into the URI translation eventually, e.g.
	 * {@link org.springframework.web.bind.annotation.PathVariable} in the case of Spring MVC. Note, that the return types
	 * of the methods have to be capable to be proxied.
	 *
	 * @param type must not be {@literal null}.
	 * @param parameters parameters to extend template variables in the type level mapping.
	 * @return
	 */
	public static <T> T methodOn(Class<T> type, Object... parameters) {

		Assert.notNull(type, "Given type must not be null!");

		InvocationRecordingMethodInterceptor interceptor = new InvocationRecordingMethodInterceptor(type, parameters);
		return getProxyWithInterceptor(type, interceptor, type.getClassLoader());
	}

	@SuppressWarnings("unchecked")
	private static <T> T getProxyWithInterceptor(Class<?> type, InvocationRecordingMethodInterceptor interceptor,
			ClassLoader classLoader) {

		ProxyFactory factory = new ProxyFactory();
		factory.addAdvice(interceptor);
		factory.addInterface(LastInvocationAware.class);

		if (type.isInterface()) {
			factory.addInterface(type);
		} else {
			factory.setTargetClass(type);
			factory.setProxyTargetClass(true);
		}

		return (T) factory.getProxy(classLoader);
	}

	@Value
	private static class SimpleMethodInvocation implements MethodInvocation {

		@NonNull Class<?> targetType;
		@NonNull Method method;
		@NonNull Object[] arguments;
	}
}
