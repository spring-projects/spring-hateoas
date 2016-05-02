/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import de.escalon.hypermedia.action.Action;
import de.escalon.hypermedia.action.Cardinality;
import de.escalon.hypermedia.action.ResourceHandler;
import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.ActionInputParameter;
import de.escalon.hypermedia.affordance.PartialUriTemplate;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.MethodLinkBuilderFactory;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.http.HttpEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Factory for {@link AffordanceBuilder}s in a Spring MVC rest service. Normally one should use the static methods
 * of AffordanceBuilder to get an AffordanceBuilder.
 * Created by dschulten on 03.10.2014.
 */
public class AffordanceBuilderFactory implements MethodLinkBuilderFactory<AffordanceBuilder> {

	private static final MappingDiscoverer MAPPING_DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);

	@Override
	public AffordanceBuilder linkTo(Method method, Object... parameters) {
		return linkTo(method.getDeclaringClass(), method, parameters);
	}

	@Override
	public AffordanceBuilder linkTo(Class<?> controller, Method method, Object... parameters) {

		String pathMapping = MAPPING_DISCOVERER.getMapping(controller, method);

		final List<String> params = getRequestParamNames(method);
		String query = join(params);
		String mapping = StringUtils.isEmpty(query) ? pathMapping : pathMapping + "{?" + query + "}";

		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(AffordanceBuilder.getBuilder()
				.build()
				.toString() + mapping);

		Map<String, Object> values = new HashMap<String, Object>();

		Iterator<String> names = partialUriTemplate.getVariableNames()
				.iterator();
		// there may be more or less mapping variables than arguments
		for (Object parameter : parameters) {
			if (!names.hasNext()) {
				break;
			}
			values.put(names.next(), parameter);
		}

		ActionDescriptor actionDescriptor = createActionDescriptor(method, values, parameters);

		return new AffordanceBuilder(partialUriTemplate.expand(values), Collections.singletonList(actionDescriptor));
	}

	private String join(List<String> params) {
		StringBuilder sb = new StringBuilder();
		for (String param : params) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(param);
		}
		return sb.toString();
	}

	@Override
	public AffordanceBuilder linkTo(Class<?> target) {
		return linkTo(target, new Object[0]);
	}

	@Override
	public AffordanceBuilder linkTo(Class<?> controller, Object... parameters) {
		Assert.notNull(controller);

		String mapping = MAPPING_DISCOVERER.getMapping(controller);

		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(mapping == null ? "/" : mapping);

		Map<String, Object> values = new HashMap<String, Object>();
		Iterator<String> names = partialUriTemplate.getVariableNames()
				.iterator();
		// there may be more or less mapping variables than arguments
		for (Object parameter : parameters) {
			if (!names.hasNext()) {
				break;
			}
			values.put(names.next(), parameter);
		}
		return new AffordanceBuilder().slash(partialUriTemplate.expand(values));
	}

	@Override
	public AffordanceBuilder linkTo(Class<?> controller, Map<String, ?> parameters) {
		String mapping = MAPPING_DISCOVERER.getMapping(controller);
		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(mapping == null ? "/" : mapping);
		return new AffordanceBuilder().slash(partialUriTemplate.expand(parameters));
	}

	@Override
	public AffordanceBuilder linkTo(Object invocationValue) {

		Assert.isInstanceOf(DummyInvocationUtils.LastInvocationAware.class, invocationValue);
		DummyInvocationUtils.LastInvocationAware invocations = (DummyInvocationUtils.LastInvocationAware) invocationValue;

		DummyInvocationUtils.MethodInvocation invocation = invocations.getLastInvocation();
		Method invokedMethod = invocation.getMethod();

		String pathMapping = MAPPING_DISCOVERER.getMapping(invokedMethod);

		List<String> params = getRequestParamNames(invokedMethod);
		String query = join(params);
		String mapping = StringUtils.isEmpty(query) ? pathMapping : pathMapping + "{?" + query + "}";

		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(AffordanceBuilder.getBuilder()
				.build()
				.toString() + mapping);

		Iterator<Object> classMappingParameters = invocations.getObjectParameters();

		Map<String, Object> values = new HashMap<String, Object>();
		Iterator<String> names = partialUriTemplate.getVariableNames()
				.iterator();
		while (classMappingParameters.hasNext()) {
			values.put(names.next(), classMappingParameters.next());
		}

		for (Object argument : invocation.getArguments()) {
			if (names.hasNext()) {
				values.put(names.next(), argument);
			}
		}

		ActionDescriptor actionDescriptor = createActionDescriptor(
				invocation.getMethod(), values, invocation.getArguments());

		return new AffordanceBuilder(partialUriTemplate.expand(values), Collections.singletonList(actionDescriptor));
	}

	private List<String> getRequestParamNames(Method invokedMethod) {
		MethodParameters parameters = new MethodParameters(invokedMethod);
		final List<MethodParameter> requestParams = parameters.getParametersWith(RequestParam.class);
		List<String> params = new ArrayList<String>(requestParams.size());
		for (MethodParameter requestParam : requestParams) {
			params.add(requestParam.getParameterName());
		}
		return params;
	}

	private ActionDescriptor createActionDescriptor(Method invokedMethod,
													Map<String, Object> values, Object[] arguments) {
		RequestMethod httpMethod = getHttpMethod(invokedMethod);
		Type genericReturnType = invokedMethod.getGenericReturnType();

		SpringActionDescriptor actionDescriptor =
				new SpringActionDescriptor(invokedMethod.getName(), httpMethod.name(), getContentType(invokedMethod));

		actionDescriptor.setCardinality(getCardinality(invokedMethod, httpMethod, genericReturnType));

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
		Map<String, ActionInputParameter> requestParamMap =
				getActionInputParameters(RequestParam.class, invokedMethod, arguments);
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

		Map<String, ActionInputParameter> pathVariableMap =
				getActionInputParameters(PathVariable.class, invokedMethod, arguments);
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

		Map<String, ActionInputParameter> requestHeadersMap =
				getActionInputParameters(RequestHeader.class, invokedMethod, arguments);

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

	private Cardinality getCardinality(Method invokedMethod, RequestMethod httpMethod, Type genericReturnType) {
		Cardinality cardinality;

		ResourceHandler resourceAnn = AnnotationUtils.findAnnotation(invokedMethod, ResourceHandler.class);
		if (resourceAnn != null) {
			cardinality = resourceAnn.value();
		} else {
			if (RequestMethod.POST == httpMethod || containsCollection(genericReturnType)) {
				cardinality = Cardinality.COLLECTION;
			} else {
				cardinality = Cardinality.SINGLE;
			}
		}
		return cardinality;
	}

	private boolean containsCollection(Type genericReturnType) {
		final boolean ret;
		if (genericReturnType instanceof ParameterizedType) {
			ParameterizedType t = (ParameterizedType) genericReturnType;
			Type rawType = t.getRawType();
			Assert.state(rawType instanceof Class<?>, "raw type is not a Class: " + rawType.toString());
			Class<?> cls = (Class<?>) rawType;
			if (HttpEntity.class.isAssignableFrom(cls)) {
				Type[] typeArguments = t.getActualTypeArguments();
				ret = containsCollection(typeArguments[0]);
			} else if (Resources.class.isAssignableFrom(cls) ||
					Collection.class.isAssignableFrom(cls)) {
				ret = true;
			} else {
				ret = false;
			}
		} else if (genericReturnType instanceof GenericArrayType) {
			ret = true;
		} else if (genericReturnType instanceof WildcardType) {
			WildcardType t = (WildcardType) genericReturnType;
			ret = containsCollection(getBound(t.getLowerBounds())) || containsCollection(getBound(t.getUpperBounds()));
		} else if (genericReturnType instanceof TypeVariable) {
			ret = false;
		} else if (genericReturnType instanceof Class) {
			Class<?> cls = (Class<?>) genericReturnType;
			ret = Resources.class.isAssignableFrom(cls) ||
					Collection.class.isAssignableFrom(cls);
		} else {
			ret = false;
		}
		return ret;
	}

	private Type getBound(Type[] lowerBounds) {
		Type ret;
		if (lowerBounds != null && lowerBounds.length > 0) {
			ret = lowerBounds[0];
		} else {
			ret = null;
		}
		return ret;
	}

	private static RequestMethod getHttpMethod(Method method) {
		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		RequestMethod requestMethod;
		if (methodRequestMapping != null) {
			RequestMethod[] methods = methodRequestMapping.method();
			if (methods.length == 0) {
				requestMethod = RequestMethod.GET;
			} else {
				requestMethod = methods[0];
			}
		} else {
			requestMethod = RequestMethod.GET; // default
		}
		return requestMethod;
	}
	
	private static String getContentType(Method method) {
		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		if (methodRequestMapping != null) {
			StringBuilder sb = new StringBuilder();
			for (String consume : methodRequestMapping.consumes()) {
				sb.append(consume).append(",");
			}
			if (sb.length() > 1) {
				sb.setLength(sb.length() - 1);
				return sb.toString();
			}
		}
		return null;
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
																			  Method method, Object... arguments
	) {

		Assert.notNull(method, "MethodInvocation must not be null!");

		MethodParameters parameters = new MethodParameters(method);
		Map<String, ActionInputParameter> result = new HashMap<String, ActionInputParameter>();

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
}
