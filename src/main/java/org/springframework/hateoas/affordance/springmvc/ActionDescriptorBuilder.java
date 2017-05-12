/*
 * Copyright 2014-2017 the original author or authors.
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
package org.springframework.hateoas.affordance.springmvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.affordance.ActionDescriptor;
import org.springframework.hateoas.affordance.ActionInputParameter;
import org.springframework.hateoas.affordance.ActionInputParameterVisitor;
import org.springframework.hateoas.affordance.formaction.Action;
import org.springframework.hateoas.affordance.formaction.DTOParam;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Dietrich Schulten
 * @author Greg Turnquist
 */
class ActionDescriptorBuilder {

	/**
	 * Use the details about the {@link Method} and it's Spring Web annotations to construct an {@link ActionDescriptor}.
	 *
	 * @param invokedMethod
	 * @param values
	 * @param arguments
	 * @return
	 */
	static ActionDescriptor createActionDescriptor(Method invokedMethod, Map<String, Object> values, Object... arguments) {

		SpringActionDescriptor actionDescriptor = new SpringActionDescriptor(invokedMethod);

		Action actionAnnotation = AnnotationUtils.getAnnotation(invokedMethod, Action.class);
		if (actionAnnotation != null) {
			actionDescriptor.setSemanticActionType(actionAnnotation.value());
		}

		Map<String, ActionInputParameter> requestParamMap = getRequestParamsAndDtoParams(invokedMethod, arguments);
		for (Map.Entry<String, ActionInputParameter> actionInputParameter : requestParamMap.entrySet()) {

			if (actionInputParameter.getValue() != null) {

				actionDescriptor.addRequestParam(actionInputParameter.getKey(), actionInputParameter.getValue());

				if (!actionInputParameter.getValue().isRequestBody()) {
					values.put(actionInputParameter.getKey(), actionInputParameter.getValue().getValueFormatted());
				}
			}
		}

		Map<String, ActionInputParameter> pathVariableMap = getActionInputParameters(PathVariable.class, invokedMethod,
				arguments);
		for (Map.Entry<String, ActionInputParameter> actionInputParameter : pathVariableMap.entrySet()) {

			if (actionInputParameter.getValue() != null) {

				actionDescriptor.addPathVariable(actionInputParameter.getKey(), actionInputParameter.getValue());

				if (!actionInputParameter.getValue().isRequestBody()) {
					values.put(actionInputParameter.getKey(), actionInputParameter.getValue().getValueFormatted());
				}
			}
		}

		Map<String, ActionInputParameter> requestHeadersMap = getActionInputParameters(RequestHeader.class, invokedMethod,
				arguments);
		for (Map.Entry<String, ActionInputParameter> actionInputParameter : requestHeadersMap.entrySet()) {

			if (actionInputParameter.getValue() != null) {

				actionDescriptor.addRequestHeader(actionInputParameter.getKey(), actionInputParameter.getValue());

				if (!actionInputParameter.getValue().isRequestBody()) {
					values.put(actionInputParameter.getKey(), actionInputParameter.getValue().getValueFormatted());
				}
			}
		}

		Map<String, ActionInputParameter> requestBodyMap = getActionInputParameters(RequestBody.class, invokedMethod,
				arguments);
		Assert.state(requestBodyMap.size() < 2, "found more than one request body on " + invokedMethod.getName());
		for (ActionInputParameter actionInputParameter : requestBodyMap.values()) {
			actionDescriptor.setRequestBody(actionInputParameter);
		}

		return actionDescriptor;
	}

	/**
	 * Look up the {@link ActionDescriptor}s for a given {@link Method}'s {@link RequestParam}s + {@link DTOParam}s
	 * and transform them into a {@link Map}.
	 * 
	 * @param invokedMethod
	 * @param arguments
	 * @return
	 */
	static Map<String, ActionInputParameter> getRequestParamsAndDtoParams(Method invokedMethod, Object[] arguments) {

		Map<String, ActionInputParameter> parameterMap = new HashMap<String, ActionInputParameter>();

		parameterMap.putAll(getActionInputParameters(RequestParam.class, invokedMethod, arguments));
		parameterMap.putAll(getDtoActionInputParameters(invokedMethod, arguments));

		return parameterMap;
	}

	/**
	 * Return the {@link ActionInputParameter}s based on the {@link Method} and associated {@link Annotation}.
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

			int parameterIndex = parameter.getParameterIndex();
			Object argument = parameterIndex < arguments.length ? arguments[parameterIndex] : null;

			result.put(parameter.getParameterName(),
					new SpringActionInputParameter(parameter, argument, parameter.getParameterName()));
		}

		return result;
	}

	/**
	 * Returns {@link ActionInputParameter}s contained in the method link.
	 *
	 * @param method must not be {@literal null}.
	 * @param arguments to the method link
	 * @return maps parameter names to parameter info
	 */
	private static Map<String, ActionInputParameter> getDtoActionInputParameters(Method method, Object... arguments) {

		Assert.notNull(method, "MethodInvocation must not be null!");

		final Map<String, ActionInputParameter> result = new HashMap<String, ActionInputParameter>();

		for (MethodParameter parameter : new MethodParameters(method).getParametersWith(DTOParam.class)) {

			int parameterIndex = parameter.getParameterIndex();
			Object argument = parameterIndex < arguments.length ? arguments[parameterIndex] : null;

			if (argument == null) {
				continue;
			}

			SpringActionDescriptor.recurseBeanCreationParams(argument.getClass(), null, argument, "", new HashSet<String>(),
					new ActionInputParameterVisitor() {

						@Override
						public void visit(ActionInputParameter inputParameter) {
							result.put(inputParameter.getParameterName(), inputParameter);
						}
					}, new ArrayList<ActionInputParameter>());

		}

		return result;
	}
}
