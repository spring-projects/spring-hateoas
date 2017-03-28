/*
 * Copyright 2012-2016 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.springframework.hateoas.TemplateVariable.VariableType.*;
import static org.springframework.hateoas.TemplateVariables.*;
import static org.springframework.hateoas.core.EncodingUtils.*;
import static org.springframework.web.util.UriComponents.UriTemplateVariables.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MethodLinkBuilderFactory;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor.BoundMethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Factory for {@link LinkBuilderSupport} instances based on the request mapping annotated on the given controller.
 * 
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Dietrich Schulten
 * @author Kamill Sokol
 * @author Ross Turner
 * @author Oemer Yildiz
 * @author Kevin Conaway
 * @author Andrew Naydyonock
 */
public class ControllerLinkBuilderFactory implements MethodLinkBuilderFactory<ControllerLinkBuilder> {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);
	private static final AnnotatedParametersParameterAccessor PATH_VARIABLE_ACCESSOR = new AnnotatedParametersParameterAccessor(
			new AnnotationAttribute(PathVariable.class));
	private static final AnnotatedParametersParameterAccessor REQUEST_PARAM_ACCESSOR = new RequestParamParameterAccessor();

	private List<UriComponentsContributor> uriComponentsContributors = new ArrayList<UriComponentsContributor>();

	/**
	 * Configures the {@link UriComponentsContributor} to be used when building {@link Link} instances from method
	 * invocations.
	 * 
	 * @see #linkTo(Object)
	 * @param uriComponentsContributors the uriComponentsContributors to set
	 */
	public void setUriComponentsContributors(List<? extends UriComponentsContributor> uriComponentsContributors) {
		this.uriComponentsContributors = Collections.unmodifiableList(uriComponentsContributors);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class)
	 */
	@Override
	public ControllerLinkBuilder linkTo(Class<?> controller) {
		return ControllerLinkBuilder.linkTo(controller);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.lang.Object[])
	 */
	@Override
	public ControllerLinkBuilder linkTo(Class<?> controller, Object... parameters) {
		return ControllerLinkBuilder.linkTo(controller, parameters);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.util.Map)
	 */
	@Override
	public ControllerLinkBuilder linkTo(Class<?> controller, Map<String, ?> parameters) {
		return ControllerLinkBuilder.linkTo(controller, parameters);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(java.lang.Class, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public ControllerLinkBuilder linkTo(Class<?> controller, Method method, Object... parameters) {
		return ControllerLinkBuilder.linkTo(controller, method, parameters);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(java.lang.Object)
	 */
	@Override
	public ControllerLinkBuilder linkTo(Object invocationValue) {

		Assert.isInstanceOf(LastInvocationAware.class, invocationValue);
		LastInvocationAware invocations = (LastInvocationAware) invocationValue;

		MethodInvocation invocation = invocations.getLastInvocation();
		Iterator<Object> classMappingParameters = invocations.getObjectParameters();
		Method method = invocation.getMethod();

		String mapping = DISCOVERER.getMapping(invocation.getTargetType(), method);
		UriComponentsBuilder builder = ControllerLinkBuilder.getBuilder().path(mapping);

		UriTemplate template = new UriTemplate(mapping);
		Map<String, Object> values = new HashMap<String, Object>();
		Iterator<String> names = template.getVariableNames().iterator();

		while (classMappingParameters.hasNext()) {
			values.put(names.next(), encodePath(classMappingParameters.next()));
		}

		for (BoundMethodParameter parameter : PATH_VARIABLE_ACCESSOR.getBoundParameters(invocation)) {
			values.put(parameter.getVariableName(), encodePath(parameter.asString()));
		}

		List<String> optionalEmptyParameters = new ArrayList<String>();

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

		UriComponents components = applyUriComponentsContributer(builder, invocation).buildAndExpand(values);
		TemplateVariables variables = NONE;

		for (String parameter : optionalEmptyParameters) {

			boolean previousRequestParameter = components.getQueryParams().isEmpty() && variables.equals(NONE);
			TemplateVariable variable = new TemplateVariable(parameter,
					previousRequestParameter ? REQUEST_PARAM : REQUEST_PARAM_CONTINUED);
			variables = variables.concat(variable);
		}

		return new ControllerLinkBuilder(components, variables);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public ControllerLinkBuilder linkTo(Method method, Object... parameters) {
		return ControllerLinkBuilder.linkTo(method, parameters);
	}

	/**
	 * Applies the configured {@link UriComponentsContributor}s to the given {@link UriComponentsBuilder}.
	 * 
	 * @param builder will never be {@literal null}.
	 * @param invocation will never be {@literal null}.
	 * @return
	 */
	protected UriComponentsBuilder applyUriComponentsContributer(UriComponentsBuilder builder,
			MethodInvocation invocation) {

		MethodParameters parameters = new MethodParameters(invocation.getMethod());
		Iterator<Object> parameterValues = Arrays.asList(invocation.getArguments()).iterator();

		for (MethodParameter parameter : parameters.getParameters()) {
			Object parameterValue = parameterValues.next();
			for (UriComponentsContributor contributor : uriComponentsContributors) {
				if (contributor.supportsParameter(parameter)) {
					contributor.enhance(builder, parameter, parameterValue);
				}
			}
		}

		return builder;
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

					if (parameter.getParameterType().getName().equals("java.lang.Optional")) {
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

			if (value != null) {
				return value;
			}

			if (!annotation.required()) {
				return SKIP_VALUE;
			}

			return annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE) ? SKIP_VALUE : null;
		}
	}
}
