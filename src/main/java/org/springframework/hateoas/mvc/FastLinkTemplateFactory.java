package org.springframework.hateoas.mvc;

import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.hateoas.core.*;
import org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor.AnnotatedMethodParameter;
import org.springframework.hateoas.mvc.FastLinkTemplate.MethodArgumentAccessor;
import org.springframework.hateoas.mvc.FastLinkTemplate.Type;
import org.springframework.hateoas.mvc.FastLinks.LastInvocationHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriTemplate;

import java.lang.reflect.Method;
import java.util.*;

class FastLinkTemplateFactory {
	private static final MappingDiscoverer DISCOVERER = CachingMappingDiscoverer.of(
			new AnnotationMappingDiscoverer(RequestMapping.class));
	private static final AnnotatedParametersParameterAccessor PATH_VARIABLE_ACCESSOR = new AnnotatedParametersParameterAccessor(
			new AnnotationAttribute(PathVariable.class));
	private static final AnnotatedParametersParameterAccessor REQUEST_PARAM_ACCESSOR = new AnnotatedParametersParameterAccessor(
			new AnnotationAttribute(RequestParam.class));


	FastLinkTemplate createLinkTemplate(LastInvocationHolder invocations) {
		DummyInvocationUtils.MethodInvocation invocation = invocations.getLastInvocation();
		Method method = invocation.getMethod();

		String mapping = DISCOVERER.getMapping(invocation.getTargetType(), method);
		UriTemplate template = new UriTemplate(mapping);

		List<FastLinkTemplate.Component> pathComponents = buildPathComponents(mapping, template,
				invocations.getObjectParameters(),
				PATH_VARIABLE_ACCESSOR.getAnnotatedParameters(method));
		List<FastLinkTemplate.Component> queryComponents = buildQueryComponents(template,
				REQUEST_PARAM_ACCESSOR.getAnnotatedParameters(method));

		return new FastLinkTemplate(pathComponents, queryComponents);
	}

	List<FastLinkTemplate.Component> buildPathComponents(String mapping, UriTemplate template, List<Object> objectParameters, List<AnnotatedMethodParameter> pathParameters) {
		Map<String, AnnotatedMethodParameter> pathParamNames = index(pathParameters);
		int objectParametersUsed = 0;

		List<String> variableNames = template.getVariableNames();
		List<FastLinkTemplate.Component> pathComponents = new ArrayList<FastLinkTemplate.Component>(variableNames.size() * 2);
		int startFrom = 0;
		for(String variableName : variableNames) {
			String variableToken = "{" + variableName + "}";
			int idx = mapping.indexOf(variableToken, startFrom);
			if (idx == -1) {
				throw new IllegalStateException(variableName);
			}
			if (idx > startFrom) {
				pathComponents.add(new FastLinkTemplate.StaticPartPathComponent(mapping.substring(startFrom, idx)));
			}
			if (objectParametersUsed < objectParameters.size()) {
				pathComponents.add(buildObjectParameterComponent(objectParametersUsed));
				objectParametersUsed++;
			} else if (pathParamNames.containsKey(variableName)) {
				AnnotatedMethodParameter annotatedMethodParameter = pathParamNames.get(variableName);
				pathComponents.add(buildMethodArgumentParameterComponent(annotatedMethodParameter));
			} else {
				throw new IllegalStateException("Variable from mapping not found: "  + variableName);
			}

			startFrom = idx + variableToken.length();
		}
		if (startFrom < mapping.length()) {
			pathComponents.add(new FastLinkTemplate.StaticPartPathComponent(mapping.substring(startFrom)));
		}

		return pathComponents;
	}

	private FastLinkTemplate.ParameterPathComponent buildObjectParameterComponent(int objectParametersUsed) {
		FastLinkTemplate.ObjectParameterAccessor paramAccessor = new FastLinkTemplate.ObjectParameterAccessor(objectParametersUsed);
		return new FastLinkTemplate.ParameterPathComponent(paramAccessor);
	}

	private FastLinkTemplate.ParameterPathComponent buildMethodArgumentParameterComponent(AnnotatedMethodParameter annotatedMethodParameter) {
		MethodParameter methodParameter = annotatedMethodParameter.getParameter();
		FastLinkTemplate.Encoder encoder = buildValueEncoder(methodParameter, Type.PATH_SEGMENT);
		MethodArgumentAccessor paramAccessor = new MethodArgumentAccessor(methodParameter.getParameterIndex());
		return new FastLinkTemplate.ParameterPathComponent(paramAccessor, encoder);
	}

	private FastLinkTemplate.Encoder buildValueEncoder(MethodParameter methodParameter, Type segmentType) {
		Class<?> parameterType = methodParameter.getParameterType();
		if (isSimpleType(parameterType)) {
			return new FastLinkTemplate.ToStringValueEncoder(segmentType);
		} else if (isNotSupportedType(parameterType)) {
			return new FastLinkTemplate.NotSupportedEncoder();
		} else {
			return new FastLinkTemplate.ConversionServiceEncoder(segmentType,
					TypeDescriptor.nested(methodParameter, 0));
		}
	}

	private boolean isSimpleType(Class<?> parameterType) {
		return String.class.isAssignableFrom(parameterType)
				|| Enum.class.isAssignableFrom(parameterType)
				|| parameterType.isEnum()
				|| Collection.class.isAssignableFrom(parameterType)
				|| parameterType.isArray()
				|| Boolean.class.isAssignableFrom(parameterType)
				|| boolean.class.isAssignableFrom(parameterType)
				|| Integer.class.isAssignableFrom(parameterType)
				|| int.class.isAssignableFrom(parameterType)
				|| Long.class.isAssignableFrom(parameterType)
				|| long.class.isAssignableFrom(parameterType)
				;
	}

	private boolean isNotSupportedType(Class<?> parameterType) {
		return Map.class.isAssignableFrom(parameterType);
	}

	private List<FastLinkTemplate.Component> buildQueryComponents(UriTemplate template, List<AnnotatedMethodParameter> queryParameters) {
		List<String> variableNames = template.getVariableNames();

		List<FastLinkTemplate.Component> queryComponents = new ArrayList<FastLinkTemplate.Component>(variableNames.size() * 2);
		for(AnnotatedMethodParameter parameter: queryParameters) {
			MethodParameter methodParameter = parameter.getParameter();
			MethodArgumentAccessor paramAccessor = new MethodArgumentAccessor(methodParameter.getParameterIndex());
			FastLinkTemplate.Encoder encoder = buildValueEncoder(methodParameter, Type.QUERY_PARAM);
			queryComponents.add(new FastLinkTemplate.QueryParamComponent(parameter.getVariableName(),
					paramAccessor, encoder));
		}

		return queryComponents;
	}


	private Map<String, AnnotatedMethodParameter> index(List<AnnotatedMethodParameter> pathParameters) {
		Map<String, AnnotatedMethodParameter> names = new HashMap<String, AnnotatedMethodParameter>(pathParameters.size());
		for (AnnotatedMethodParameter parameter : pathParameters) {
			names.put(parameter.getVariableName(), parameter);
		}
		return names;
	}
}
