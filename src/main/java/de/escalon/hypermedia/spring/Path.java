package de.escalon.hypermedia.spring;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.objenesis.ObjenesisStd;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

public class Path {

	public static ThreadLocal<RecordingMethodInterceptor> interceptorThreadLocal;

	public static <T> T on(Class<T> type) {
		return on(type, true);
	}

	public static <T> T on(Class<T> type, boolean init) {
		if (interceptorThreadLocal == null) {
			interceptorThreadLocal = new ThreadLocal<Path.RecordingMethodInterceptor>();
		}
		if (init) {
			interceptorThreadLocal.remove();
			interceptorThreadLocal.set(new RecordingMethodInterceptor(type));
		}
		return getProxyWithInterceptor(type, interceptorThreadLocal.get(), type.getClassLoader());
	}

	public static <T> T on(Class<T> type, RecordingMethodInterceptor rmi) {
		return getProxyWithInterceptor(type, rmi, type.getClassLoader());
	}

	@SuppressWarnings("unchecked")
	public static <T> T collection(Collection<? extends T> collection) {
		Assert.isInstanceOf(LastInvocationAware.class, collection);

		ResolvableType resolvable = ResolvableType
				.forMethodReturnType(((LastInvocationAware) collection).getLastInvocation().getMethod());

		return on((Class<T>) resolvable.getGeneric(0).getRawClass(), false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T collection(Collection<? extends T> collection, RecordingMethodInterceptor rmi) {
		Assert.isInstanceOf(LastInvocationAware.class, collection);

		ResolvableType resolvable = ResolvableType
				.forMethodReturnType(((LastInvocationAware) collection).getLastInvocation().getMethod());

		return on((Class<T>) resolvable.getGeneric(0).getRawClass(), rmi);
	}

	public static String path(Object obj) {
		RecordingMethodInterceptor interceptor = interceptorThreadLocal.get();
		Assert.notNull(interceptor);
		interceptorThreadLocal.remove();
		return interceptor.getLastInvocation().toString();
	}

	public static class RecordingMethodInterceptor
			implements MethodInterceptor, LastInvocationAware, org.springframework.cglib.proxy.MethodInterceptor {

		private static final Method GET_INVOCATIONS;

		private static final Method GET_OBJECT_PARAMETERS;

		private final Class<?> targetType;

		private final Object[] objectParameters;

		private MethodInvocation invocation;

		static {
			GET_INVOCATIONS = ReflectionUtils.findMethod(LastInvocationAware.class, "getLastInvocation");
			GET_OBJECT_PARAMETERS = ReflectionUtils.findMethod(LastInvocationAware.class, "getObjectParameters");
		}

		public RecordingMethodInterceptor(Class<?> targetType, Object... objectParameters) {
			this.targetType = targetType;
			this.objectParameters = objectParameters;
		}

		@Override
		public Iterator<Object> getObjectParameters() {
			return Arrays.asList(objectParameters).iterator();
		}

		@Override
		public MethodInvocation getLastInvocation() {
			return invocation;
		}

		@Override
		public Object invoke(org.aopalliance.intercept.MethodInvocation invocation) throws Throwable {
			return intercept(invocation.getThis(), invocation.getMethod(), invocation.getArguments(), null);
		}

		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy arg3) throws Throwable {
			if (GET_INVOCATIONS.equals(method)) {
				return getLastInvocation();
			} else if (GET_OBJECT_PARAMETERS.equals(method)) {
				return getObjectParameters();
			} else if (Object.class.equals(method.getDeclaringClass())) {
				return ReflectionUtils.invokeMethod(method, obj, args);
			}
			invocation = new SimpleMethodInvocation(targetType, method, args, getLastInvocation());

			Class<?> returnType = method.getReturnType();
			if (Modifier.isFinal(returnType.getModifiers())) {
				return null;
			} else {
				return returnType.cast(getProxyWithInterceptor(returnType, this, obj.getClass().getClassLoader()));
			}
		}

	}

	private static ObjenesisStd OBJENESIS = new ObjenesisStd();

	private static <T> T getProxyWithInterceptor(Class<?> type, RecordingMethodInterceptor interceptor,
			ClassLoader classLoader) {
		return getProxyWithInterceptor(type, interceptor, classLoader, false);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getProxyWithInterceptor(Class<?> type, RecordingMethodInterceptor interceptor,
			ClassLoader classLoader, boolean isfinal) {

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
		enhancer.setClassLoader(classLoader);

		Factory factory = (Factory) OBJENESIS.newInstance(enhancer.createClass());
		factory.setCallbacks(new Callback[] { interceptor });
		return (T) factory;
	}

	static class SimpleMethodInvocation implements MethodInvocation {

		private final Class<?> targetType;

		private final Method method;

		private final Object[] arguments;

		private final MethodInvocation invocation;

		/**
		 * Creates a new {@link SimpleMethodInvocation} for the given {@link Method} and arguments.
		 * 
		 * @param method
		 * @param arguments
		 */
		private SimpleMethodInvocation(Class<?> targetType, Method method, Object[] arguments,
				MethodInvocation invocation) {

			this.targetType = targetType;
			this.arguments = arguments;
			this.method = method;
			this.invocation = invocation;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.hateoas.core.DummyInvocationUtils. MethodInvocation#getTargetType()
		 */
		@Override
		public Class<?> getTargetType() {
			return targetType;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.hateoas.core.DummyInvocationUtils. MethodInvocation#getArguments()
		 */
		@Override
		public Object[] getArguments() {
			return arguments;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.hateoas.core.DummyInvocationUtils. MethodInvocation#getMethod()
		 */
		@Override
		public Method getMethod() {
			return method;
		}

		@Override
		public String toString() {
			return (invocation != null ? invocation.toString() + "." : "") + getPropertyFromMethod(method);
		}

		private String getPropertyFromMethod(Method method) {
			String name = method.getName();
			if (name.length() > 0 && name.toLowerCase().startsWith("get")) {
				char c[] = name.replace("get", "").toCharArray();
				c[0] = Character.toLowerCase(c[0]);
				return new String(c);
			}
			return "";
		}
	}

	public static class PathBuilder {

		private RecordingMethodInterceptor rmi;

		public <T> T on(Class<T> clazz) {
			rmi = new RecordingMethodInterceptor(clazz);
			T on = Path.on(clazz, rmi);
			return on;
		}

		public String build(Object object) {
			return build();
		}

		public String build() {
			return rmi.getLastInvocation().toString();
		}

		public <E> E collection(Collection<? extends E> collection) {
			return Path.collection(collection, rmi);
		}

	}
}
