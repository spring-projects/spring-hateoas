package de.escalon.hypermedia.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import de.escalon.hypermedia.action.Action;
import de.escalon.hypermedia.action.DTORequestParam;
import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.ActionInputParameter;
import de.escalon.hypermedia.affordance.ActionInputParameterVisitor;

public class ActionDescriptorBuilder {

	static ActionDescriptor createActionDescriptor(Method invokedMethod, Map<String, Object> values, Object[] arguments) {

		SpringActionDescriptor actionDescriptor = new SpringActionDescriptor(invokedMethod);

		final Action actionAnnotation = AnnotationUtils.getAnnotation(invokedMethod, Action.class);
		if (actionAnnotation != null) {
			actionDescriptor.setSemanticActionType(actionAnnotation.value());
		}

		Map<String, ActionInputParameter> requestBodyMap = getActionInputParameters(RequestBody.class, invokedMethod,
				arguments);
		Assert.state(requestBodyMap.size() < 2, "found more than one request body on " + invokedMethod.getName());
		for (ActionInputParameter value : requestBodyMap.values()) {
			actionDescriptor.setRequestBody(value);
		}

		// the action descriptor needs to know the param type, value and name
		Map<String, ActionInputParameter> requestParamMap = getRequestParams(invokedMethod, arguments);
		for (Map.Entry<String, ActionInputParameter> entry : requestParamMap.entrySet()) {
			ActionInputParameter value = entry.getValue();
			if (value != null) {
				final String key = entry.getKey();
				actionDescriptor.addRequestParam(key, value);
				if (!value.isRequestBody()) {
					values.put(key, value.getValueFormatted());
				}
			}
		}

		Map<String, ActionInputParameter> pathVariableMap = getActionInputParameters(PathVariable.class, invokedMethod,
				arguments);
		for (Map.Entry<String, ActionInputParameter> entry : pathVariableMap.entrySet()) {
			ActionInputParameter actionInputParameter = entry.getValue();
			if (actionInputParameter != null) {
				final String key = entry.getKey();
				actionDescriptor.addPathVariable(key, actionInputParameter);
				if (!actionInputParameter.isRequestBody()) {
					values.put(key, actionInputParameter.getValueFormatted());
				}
			}
		}

		Map<String, ActionInputParameter> requestHeadersMap = getActionInputParameters(RequestHeader.class, invokedMethod,
				arguments);

		for (Map.Entry<String, ActionInputParameter> entry : requestHeadersMap.entrySet()) {
			ActionInputParameter actionInputParameter = entry.getValue();
			if (actionInputParameter != null) {
				final String key = entry.getKey();
				actionDescriptor.addRequestHeader(key, actionInputParameter);
				if (!actionInputParameter.isRequestBody()) {
					values.put(key, actionInputParameter.getValueFormatted());
				}
			}
		}

		return actionDescriptor;
	}

	/**
	 * Returns {@link ActionInputParameter}s contained in the method link.
	 *
	 * @param annotation to inspect
	 * @param method must not be {@literal null}.
	 * @param arguments to the method link
	 * @return maps parameter names to parameter info
	 */
	private static Map<String, ActionInputParameter> getActionInputParameters(Class<? extends Annotation> annotation,
			Method method, Object... arguments) {

		Assert.notNull(method, "MethodInvocation must not be null!");

		MethodParameters parameters = new MethodParameters(method);
		Map<String, ActionInputParameter> result = new LinkedHashMap<String, ActionInputParameter>();

		for (MethodParameter parameter : parameters.getParametersWith(annotation)) {
			final int parameterIndex = parameter.getParameterIndex();
			final Object argument;
			if (parameterIndex < arguments.length) {
				argument = arguments[parameterIndex];
			} else {
				argument = null;
			}
			result.put(parameter.getParameterName(), new SpringActionInputParameter(parameter, argument));
		}

		return result;
	}

	/**
	 * Returns {@link ActionInputParameter}s contained in the method link.
	 *
	 * @param annotation to inspect
	 * @param method must not be {@literal null}.
	 * @param arguments to the method link
	 * @return maps parameter names to parameter info
	 */
	private static Map<String, ActionInputParameter> getDTOActionInputParameters(Method method, Object... arguments) {

		Assert.notNull(method, "MethodInvocation must not be null!");

		MethodParameters parameters = new MethodParameters(method);
		final Map<String, ActionInputParameter> result = new HashMap<String, ActionInputParameter>();

		for (MethodParameter parameter : parameters.getParametersWith(DTORequestParam.class)) {
			final int parameterIndex = parameter.getParameterIndex();
			final Object argument;
			if (parameterIndex < arguments.length) {
				argument = arguments[parameterIndex];
			} else {
				argument = null;
			}
			SpringActionDescriptor.recurseBeanCreationParams(argument.getClass(), null, argument, "", new HashSet<String>(),
					new ActionInputParameterVisitor() {

						@Override
						public String visit(ActionInputParameter inputParameter) {
							result.put(inputParameter.getParameterName(), inputParameter);
							return "";
						}
					}, Collections.<ActionInputParameter> emptyList());

		}

		return result;
	}

	static Map<String, ActionInputParameter> getRequestParams(Method invokedMethod, Object[] arguments) {
		// the action descriptor needs to know the param type, value and name
		Map<String, ActionInputParameter> requestParamMap = getActionInputParameters(RequestParam.class, invokedMethod,
				arguments);
		requestParamMap.putAll(getDTOActionInputParameters(invokedMethod, arguments));
		return requestParamMap;

	}

}
