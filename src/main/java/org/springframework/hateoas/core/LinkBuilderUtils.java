/*
 * Copyright 2012 the original author or authors.
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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.util.ReflectionUtils;

/**
 * @author Oliver Gierke
 */
public class LinkBuilderUtils {

	public interface LastInvocationAware {

		MethodInvocation getLastInvocation();
	}

	private static class InvocationRecordingMethodInterceptor implements MethodInterceptor, LastInvocationAware {

		private static final Method GET_INVOCATIONS;
		private MethodInvocation invocation;

		static {
			GET_INVOCATIONS = ReflectionUtils.findMethod(LastInvocationAware.class, "getLastInvocation");
		}

		/* 
		 * (non-Javadoc)
		 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
		 */
		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {

			if (GET_INVOCATIONS.equals(invocation.getMethod())) {
				return getLastInvocation();
			} else if (Object.class.equals(invocation.getMethod().getDeclaringClass())) {
				return invocation.proceed();
			}

			this.invocation = invocation;

			Class<?> returnType = invocation.getMethod().getReturnType();

			ProxyFactory factory = new ProxyFactory(EmptyTargetSource.INSTANCE);
			factory.setTargetClass(returnType);
			factory.addInterface(LastInvocationAware.class);
			factory.setProxyTargetClass(true);
			factory.addAdvice(this);

			return returnType.cast(factory.getProxy());
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.LinkBuilderUtils.LastInvocationAware#getLastInvocation()
		 */
		@Override
		public MethodInvocation getLastInvocation() {
			return invocation;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T methodOn(Class<T> type) {

		MethodInterceptor interceptor = new InvocationRecordingMethodInterceptor();
		ProxyFactory factory = new ProxyFactory(EmptyTargetSource.INSTANCE);

		factory.setProxyTargetClass(true);
		factory.setTargetClass(type);
		factory.addInterface(LastInvocationAware.class);
		factory.addAdvice(interceptor);

		return (T) factory.getProxy();
	}
}
