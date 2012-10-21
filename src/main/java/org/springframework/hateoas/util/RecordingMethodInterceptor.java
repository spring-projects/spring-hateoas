package org.springframework.hateoas.util;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;


class RecordingMethodInterceptor implements MethodInterceptor {

	Invocations invocations = new InvocationsImpl();

	public RecordingMethodInterceptor(Invocations invocations) {
		super();
		this.invocations = invocations;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

		Method getInvocations = Invocations.class.getMethod("getInvocations");
		if (getInvocations.equals(method)) {
			return invocations.getInvocations();
		} else {
			Invocation invocation = new Invocation(obj, method, args);
			invocations.getInvocations().add(invocation);
			Class<?> returnType = method.getReturnType();
			Object returnProxy = Enhancer.create(returnType, new Class<?>[] { Invocations.class }, this);
			return returnProxy;
		}

	}

}