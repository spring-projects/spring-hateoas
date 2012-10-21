package org.springframework.hateoas.util;

import java.lang.reflect.Method;

public class Invocation {

	private final Object target;
	private final Method method;
	private final Object[] args;

	public Invocation(Object target, Method method, Object[] args) {
		super();
		this.target = target;
		this.method = method;
		this.args = args;
	}

	public Method getMethod() {
		return method;
	}

	public Object getTarget() {
		return target;
	}

	public Object[] getArgs() {
		return args;
	}

}