/*
 * Copyright 2012-2014 the original author or authors.
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
import java.util.Arrays;
import java.util.Iterator;

import org.aopalliance.intercept.MethodInterceptor;
import org.objenesis.ObjenesisStd;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Utility methods to capture dummy method invocations.
 * 
 * @author Oliver Gierke
 */
public class DummyInvocationUtils {

	private static ObjenesisStd OBJENESIS = new ObjenesisStd();

	public interface LastInvocationAware {

		Iterator<Object> getObjectParameters();

		MethodInvocation getLastInvocation();
	}

	/**
	 * Method interceptor that records the last method invocation and creates a proxy for the return value that exposes
	 * the method invocation.
	 * 
	 * @author Oliver Gierke
	 */
	private static class InvocationRecordingMethodInterceptor implements MethodInterceptor, LastInvocationAware,
			org.springframework.cglib.proxy.MethodInterceptor {

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
		 * @param parameters
		 */
		public InvocationRecordingMethodInterceptor(Class<?> targetType, Object... parameters) {

			this.targetType = targetType;
			this.objectParameters = parameters.clone();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], org.springframework.cglib.proxy.MethodProxy)
		 */
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) {

			if (GET_INVOCATIONS.equals(method)) {
				return getLastInvocation();
			} else if (GET_OBJECT_PARAMETERS.equals(method)) {
				return getObjectParameters();
			} else if (Object.class.equals(method.getDeclaringClass())) {
				return ReflectionUtils.invokeMethod(method, obj, args);
			}

			this.invocation = new SimpleMethodInvocation(targetType, method, args);

			Class<?> returnType = method.getReturnType();
			return returnType.cast(getProxyWithInterceptor(returnType, this));
		}

		/* 
		 * (non-Javadoc)
		 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
		 */
		@Override
		public Object invoke(org.aopalliance.intercept.MethodInvocation invocation) throws Throwable {
			return intercept(invocation.getThis(), invocation.getMethod(), invocation.getArguments(), null);
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
	 * the ones that might be mapped into the URI translation eventually, e.g. {@linke PathVariable} in the case of Spring
	 * MVC. Note, that the return types of the methods have to be capable to be proxied.
	 * 
	 * @param type must not be {@literal null}.
	 * @param parameters parameters to extend template variables in the type level mapping.
	 * @return
	 */
	public static <T> T methodOn(Class<T> type, Object... parameters) {

		Assert.notNull(type, "Given type must not be null!");

		InvocationRecordingMethodInterceptor interceptor = new InvocationRecordingMethodInterceptor(type, parameters);
		return getProxyWithInterceptor(type, interceptor);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getProxyWithInterceptor(Class<?> type, InvocationRecordingMethodInterceptor interceptor) {

		if (type.isInterface()) {

			ProxyFactory factory = new ProxyFactory(EmptyTargetSource.INSTANCE);
			factory.addInterface(type);
			factory.addInterface(LastInvocationAware.class);
			factory.addAdvice(interceptor);

			return (T) factory.getProxy();
		}

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setInterfaces(new Class<?>[] { LastInvocationAware.class });
		enhancer.setCallbackType(org.springframework.cglib.proxy.MethodInterceptor.class);

		Factory factory = (Factory) OBJENESIS.newInstance(enhancer.createClass());
		factory.setCallbacks(new Callback[] { interceptor });
		return (T) factory;
	}

	public interface MethodInvocation {

		Object[] getArguments();

		Method getMethod();

		Class<?> getTargetType();
	}

	static class SimpleMethodInvocation implements MethodInvocation {

		private final Class<?> targetType;
		private final Method method;
		private final Object[] arguments;

		/**
		 * Creates a new {@link SimpleMethodInvocation} for the given {@link Method} and arguments.
		 * 
		 * @param method
		 * @param arguments
		 */
		private SimpleMethodInvocation(Class<?> targetType, Method method, Object[] arguments) {

			this.targetType = targetType;
			this.arguments = arguments;
			this.method = method;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation#getTargetType()
		 */
		@Override
		public Class<?> getTargetType() {
			return targetType;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation#getArguments()
		 */
		@Override
		public Object[] getArguments() {
			return arguments;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation#getMethod()
		 */
		@Override
		public Method getMethod() {
			return method;
		}
	}
}
