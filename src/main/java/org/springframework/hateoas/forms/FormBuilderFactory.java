package org.springframework.hateoas.forms;

import java.lang.reflect.Method;

import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.mvc.ControllerFormBuilder;

public interface FormBuilderFactory<T extends TemplateBuilder> {
	/**
	 * Returns a {@link ControllerFormBuilder} pointing to the URI mapped to the given {@link Method} and expanding this
	 * mapping using the given parameters.
	 * 
	 * @param method must not be {@literal null}.
	 * @param parameters
	 * @return
	 */
	T formTo(Method method, Object... parameters);

	/**
	 * Returns a {@link ControllerFormBuilder} pointing to the URI mapped to the given {@link Method} assuming it was
	 * invoked on an object of the given type.
	 * 
	 * @param type must not be {@literal null}.
	 * @param method must not be {@literal null}.
	 * @param parameters
	 * @return
	 */
	T formTo(Class<?> type, Method method, Object... parameters);

	/**
	 * Returns a {@link ControllerFormBuilder} pointing to the URI mapped to the method the result is handed into this
	 * method. Use {@link DummyInvocationUtils#methodOn(Class, Object...)} to obtain a dummy instance of a controller to
	 * record a dummy method invocation on. See {@link HalFormsLinkBuilder#linkTo(Object)} for an example.
	 * 
	 * @see ControllerLinkBuilder#linkTo(Object)
	 * @param methodInvocationResult must not be {@literal null}.
	 * @return
	 */
	T formTo(Object methodInvocationResult);
}
