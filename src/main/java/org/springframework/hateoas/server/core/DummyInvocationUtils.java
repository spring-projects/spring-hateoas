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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.util.ReflectionUtils;

/**
 * Utility methods to capture dummy method invocations.
 *
 * @author Oliver Gierke
 */
public class DummyInvocationUtils {

	private static final ThreadLocal<ConcurrentLruCache<CacheKey, Object>> CACHE = ThreadLocal
			.withInitial(() -> new ConcurrentLruCache<CacheKey, Object>(256,
					it -> {

						InvocationRecordingMethodInterceptor interceptor = new InvocationRecordingMethodInterceptor(it.type,
								it.arguments);
						return getProxyWithInterceptor(it.type, interceptor, it.type.getClassLoader());
					}));

	/**
	 * Method interceptor that records the last method invocation and creates a proxy for the return value that exposes
	 * the method invocation.
	 *
	 * @author Oliver Gierke
	 */
	private static class InvocationRecordingMethodInterceptor implements MethodInterceptor, LastInvocationAware {

		private final Class<?> targetType;
		private final Object[] objectParameters;
		private MethodInvocation invocation;

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

			if (ReflectionUtils.isObjectMethod(method)) {
				return ReflectionUtils.invokeMethod(method, invocation.getThis(), invocation.getArguments());
			}

			this.invocation = new DefaultMethodInvocation(targetType, method, invocation.getArguments());

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
	@SuppressWarnings("unchecked")
	public static <T> T methodOn(Class<T> type, Object... parameters) {

		Assert.notNull(type, "Given type must not be null!");

		return (T) CACHE.get().get(CacheKey.of(type, parameters));
	}

	/**
	 * Returns the {@link LastInvocationAware} instance from the given source, that essentially has to be a proxy created
	 * via {@link #methodOn(Class, Object...)} and subsequent {@code linkTo(…)} calls.
	 *
	 * @param source must not be {@literal null}.
	 * @return
	 */
	@Nullable
	public static LastInvocationAware getLastInvocationAware(Object source) {

		if (InvocationRecordingMethodInterceptor.class.isInstance(source)) {
			return LastInvocationAware.class.cast(source);
		}

		return (LastInvocationAware) (Advised.class.isInstance(source)
				? ((Advised) source).getAdvisors()[0].getAdvice()
				: source);
	}

	/**
	 * Creates a simple {@link LastInvocationAware} for the given method and parameters.
	 *
	 * @param method must not be {@literal null}.
	 * @param parameters must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.3.4
	 */
	public static LastInvocationAware getLastInvocationAware(Method method, Object[] parameters) {
		return getLastInvocationAware(method.getDeclaringClass(), method, parameters);
	}

	/**
	 * Creates a simple {@link LastInvocationAware} from the given type, method and parameters.
	 *
	 * @param type must not be {@literal null}.
	 * @param method must not be {@literal null}.
	 * @param parameters must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.3.4
	 */
	public static LastInvocationAware getLastInvocationAware(Class<?> type, Method method, Object[] parameters) {
		return new DefaultMethodInvocation(type, method, parameters);
	}

	public static void resetCache() {
		CACHE.remove();
	}

	@SuppressWarnings("unchecked")
	private static <T> T getProxyWithInterceptor(Class<?> type, InvocationRecordingMethodInterceptor interceptor,
			ClassLoader classLoader) {

		if (type.equals(Object.class)) {
			return (T) interceptor;
		}

		ProxyFactory factory = new ProxyFactory();
		factory.addAdvice(interceptor);
		factory.addInterface(LastInvocationAware.class);

		if (type.isInterface()) {
			factory.addInterface(type);
		} else {
			factory.setOptimize(true);
			factory.setTargetClass(type);
			factory.setProxyTargetClass(true);
		}

		return (T) factory.getProxy(classLoader);
	}

	private static final class CacheKey {

		private final Class<?> type;
		private final Object[] arguments;

		private CacheKey(Class<?> type, Object[] arguments) {

			this.type = type;
			this.arguments = arguments;
		}

		public static CacheKey of(Class<?> type, Object[] arguments) {
			return new CacheKey(type, arguments);
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

			if (!(o instanceof CacheKey)) {
				return false;
			}

			CacheKey cacheKey = (CacheKey) o;

			return Objects.equals(this.type, cacheKey.type) //
					&& Arrays.equals(this.arguments, cacheKey.arguments);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {

			int result = Objects.hash(this.type);
			result = 31 * result + Arrays.hashCode(this.arguments);
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {

			return "DummyInvocationUtils.CacheKey(type=" + this.type //
					+ ", arguments=" + Arrays.deepToString(this.arguments) //
					+ ")";
		}
	}
}
