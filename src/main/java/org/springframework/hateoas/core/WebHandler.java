/*
 * Copyright 2019 the original author or authors.
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

import static org.springframework.hateoas.TemplateVariable.VariableType.*;
import static org.springframework.hateoas.TemplateVariables.*;
import static org.springframework.hateoas.core.EncodingUtils.*;
import static org.springframework.web.util.UriComponents.UriTemplateVariables.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.core.MethodParameter;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.core.AnnotatedParametersParameterAccessor.BoundMethodParameter;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Utility for taking a method invocation and extracting a {@link ControllerLinkBuilder}.
 * 
 * @author Greg Turnquist
 */
public class WebHandler {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);
	private static final AnnotatedParametersParameterAccessor PATH_VARIABLE_ACCESSOR = new AnnotatedParametersParameterAccessor(
		new AnnotationAttribute(PathVariable.class));
	private static final AnnotatedParametersParameterAccessor REQUEST_PARAM_ACCESSOR = new RequestParamParameterAccessor();

	public static ControllerLinkBuilder linkTo(Object invocationValue,
											   Function<String, UriComponentsBuilder> mappingToUriComponentsBuilder,
											   BiFunction<UriComponentsBuilder, MethodInvocation, UriComponentsBuilder> additionalUriHandler) {

		Assert.isInstanceOf(LastInvocationAware.class, invocationValue);
		
		LastInvocationAware invocations = (LastInvocationAware) invocationValue;
		MethodInvocation invocation = invocations.getLastInvocation();

		String mapping = DISCOVERER.getMapping(invocation.getTargetType(), invocation.getMethod());

		UriComponentsBuilder builder = mappingToUriComponentsBuilder.apply(mapping);
		
		UriTemplate template;
		if (mapping == null) {
			template = new UriTemplate("/");
		} else {
			template = new UriTemplate(mapping);
		}

		Map<String, Object> values = new HashMap<>();

		Iterator<String> names = template.getVariableNames().iterator();
		Iterator<Object> classMappingParameters = invocations.getObjectParameters();
		
		while (classMappingParameters.hasNext()) {
			values.put(names.next(), encodePath(classMappingParameters.next()));
		}

		for (BoundMethodParameter parameter : PATH_VARIABLE_ACCESSOR.getBoundParameters(invocation)) {
			values.put(parameter.getVariableName(), encodePath(parameter.asString()));
		}

		List<String> optionalEmptyParameters = new ArrayList<>();

		for (BoundMethodParameter parameter : REQUEST_PARAM_ACCESSOR.getBoundParameters(invocation)) {

			bindRequestParameters(builder, parameter);

			if (SKIP_VALUE.equals(parameter.getValue())) {

				values.put(parameter.getVariableName(), SKIP_VALUE);

				if (!parameter.isRequired()) {
					optionalEmptyParameters.add(parameter.getVariableName());
				}
			}
		}

		for (String variable : template.getVariableNames()) {
			if (!values.containsKey(variable)) {
				values.put(variable, SKIP_VALUE);
			}
		}

		UriComponents components;
		if (additionalUriHandler == null) {
			components = builder.buildAndExpand(values);
		} else {
			components = additionalUriHandler.apply(builder, invocation).buildAndExpand(values);
		}

		TemplateVariables variables = NONE;

		for (String parameter : optionalEmptyParameters) {

			boolean previousRequestParameter = components.getQueryParams().isEmpty() && variables.equals(NONE);
			TemplateVariable variable = new TemplateVariable(parameter,
				previousRequestParameter ? REQUEST_PARAM : REQUEST_PARAM_CONTINUED);
			variables = variables.concat(variable);
		}

		return new ControllerLinkBuilder(components, variables, invocation);
	}
	
	/**
	 * Populates the given {@link UriComponentsBuilder} with request parameters found in the given
	 * {@link BoundMethodParameter}.
	 *
	 * @param builder must not be {@literal null}.
	 * @param parameter must not be {@literal null}.
	 */
	@SuppressWarnings("unchecked")
	private static void bindRequestParameters(UriComponentsBuilder builder, BoundMethodParameter parameter) {

		Object value = parameter.getValue();
		String key = parameter.getVariableName();

		if (value instanceof MultiValueMap) {

			MultiValueMap<String, String> requestParams = (MultiValueMap<String, String>) value;

			for (Map.Entry<String, List<String>> multiValueEntry : requestParams.entrySet()) {
				for (String singleEntryValue : multiValueEntry.getValue()) {
					builder.queryParam(multiValueEntry.getKey(), encodeParameter(singleEntryValue));
				}
			}

		} else if (value instanceof Map) {

			Map<String, String> requestParams = (Map<String, String>) value;

			for (Map.Entry<String, String> requestParamEntry : requestParams.entrySet()) {
				builder.queryParam(requestParamEntry.getKey(), encodeParameter(requestParamEntry.getValue()));
			}

		} else if (value instanceof Collection) {

			for (Object element : (Collection<?>) value) {
				builder.queryParam(key, encodeParameter(element));
			}

		} else if (SKIP_VALUE.equals(value)) {

			if (parameter.isRequired()) {
				builder.queryParam(key, String.format("{%s}", parameter.getVariableName()));
			}

		} else {
			builder.queryParam(key, encodeParameter(parameter.asString()));
		}
	}

	/**
	 * Custom extension of {@link AnnotatedParametersParameterAccessor} for {@link RequestParam} to allow {@literal null}
	 * values handed in for optional request parameters.
	 *
	 * @author Oliver Gierke
	 */
	private static class RequestParamParameterAccessor extends AnnotatedParametersParameterAccessor {

		public RequestParamParameterAccessor() {
			super(new AnnotationAttribute(RequestParam.class));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor#createParameter(org.springframework.core.MethodParameter, java.lang.Object, org.springframework.hateoas.core.AnnotationAttribute)
		 */
		@Override
		protected BoundMethodParameter createParameter(final MethodParameter parameter, Object value,
													   AnnotationAttribute attribute) {

			return new BoundMethodParameter(parameter, value, attribute) {

				/*
				 * (non-Javadoc)
				 * @see org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor.BoundMethodParameter#isRequired()
				 */
				@Override
				public boolean isRequired() {

					RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);

					if (parameter.isOptional()) {
						return false;
					}

					return annotation.required() //
							&& annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE);
				}
			};
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor#verifyParameterValue(org.springframework.core.MethodParameter, java.lang.Object)
		 */
		@Override
		protected Object verifyParameterValue(MethodParameter parameter, Object value) {

			RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);

			value = ObjectUtils.unwrapOptional(value);

			if (value != null) {
				return value;
			}

			if (!annotation.required() || parameter.isOptional()) {
				return SKIP_VALUE;
			}

			return annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE) ? SKIP_VALUE : null;
		}
	}
}
