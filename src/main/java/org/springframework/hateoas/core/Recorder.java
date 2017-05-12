package org.springframework.hateoas.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.objenesis.ObjenesisStd;
import org.springframework.util.StringUtils;

@AllArgsConstructor
public class Recorder implements MethodInterceptor {

	private static ObjenesisStd OBJENESIS = new ObjenesisStd();

	private Recorded<?> currentMock = null;
	private Method invokedMethod;
	private final List<PropertyNameDetectionStrategy> strategies;

	public Recorder() {
		this(null, null, Arrays.<PropertyNameDetectionStrategy>asList(DefaultPropertyNameDetectionStrategy.INSTANCE));
	}

	public Recorder register(PropertyNameDetectionStrategy strategy) {

		List<PropertyNameDetectionStrategy> strategies = new ArrayList<PropertyNameDetectionStrategy>(
				this.strategies.size() + 1);
		strategies.add(strategy);
		strategies.addAll(this.strategies);

		return new Recorder(currentMock, invokedMethod, strategies);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], org.springframework.cglib.proxy.MethodProxy)
	 */
	public Object intercept(Object o, Method method, Object[] os, MethodProxy mp) throws Throwable {

		if (method.getName().equals("getCurrentPropertyName")) {
			return getCurrentPropertyName();
		}

		this.invokedMethod = method;
		this.currentMock = forType(method.getReturnType());

		return currentMock.getObject();
	}

	String getPropertyName(Method method) {

		for (PropertyNameDetectionStrategy strategy : strategies) {

			String propertyName = strategy.getPropertyName(method);

			if (propertyName != null) {
				return propertyName;
			}
		}

		return null;
	}

	public String getCurrentPropertyName() {

		String propertyName = getPropertyName(invokedMethod);

		if (currentMock == null) {
			return propertyName;
		}

		String next = currentMock.getCurrentPropertyName();

		return StringUtils.hasText(next) ? propertyName.concat(".").concat(next) : propertyName;
	}

	@SuppressWarnings("unchecked")
	public static <T> Recorded<T> forType(Class<T> type) {

		Recorder recordingObject = new Recorder();

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallbackType(Recorder.class);

		Factory factory = (Factory) OBJENESIS.newInstance(enhancer.createClass());
		factory.setCallbacks(new Callback[] { recordingObject });

		return new Recorded<T>((T) factory, recordingObject);
	}

	public interface PropertyNameDetectionStrategy {
		String getPropertyName(Method method);
	}

	enum DefaultPropertyNameDetectionStrategy implements PropertyNameDetectionStrategy {

		INSTANCE;

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.Recorder.PropertyNameDetectionStrategy#getPropertyName(java.lang.reflect.Method)
		 */
		@Override
		public String getPropertyName(Method method) {
			return getPropertyName(method.getReturnType(), method.getName());
		}

		static String getPropertyName(Class<?> type, String methodName) {

			String pattern = getPatternFor(type);
			String replaced = methodName.replaceFirst(pattern, "");

			return StringUtils.uncapitalize(replaced);
		}

		private static String getPatternFor(Class<?> type) {
			return "^(get|set".concat(type.equals(boolean.class) ? "|is)" : ")");
		}
	}

	@ToString
	@RequiredArgsConstructor
	public static class Recorded<T> {

		private final T t;
		private final Recorder recorder;

		public String getCurrentPropertyName() {
			return recorder.getCurrentPropertyName();
		}

		/**
		 * Applies the given Converter to the recorded value and remembers the property accessed.
		 * 
		 * @param converter must not be {@literal null}.
		 * @return
		 */
		public <S> Recorded<S> apply(Converter<T, S> converter) {
			return new Recorded<S>(converter.convert(t), recorder);
		}

		private T getObject() {
			return t;
		}
	}
}
