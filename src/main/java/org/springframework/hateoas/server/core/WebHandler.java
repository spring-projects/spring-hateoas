/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.server.core;

import static org.springframework.hateoas.TemplateVariable.VariableType.*;
import static org.springframework.hateoas.TemplateVariables.*;
import static org.springframework.hateoas.server.core.EncodingUtils.*;
import static org.springframework.web.util.UriComponents.UriTemplateVariables.*;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Utility for taking a method invocation and extracting a {@link LinkBuilder}.
 *
 * @author Greg Turnquist
 */
public class WebHandler {

	private static final MappingDiscoverer DISCOVERER = CachingMappingDiscoverer
			.of(new AnnotationMappingDiscoverer(RequestMapping.class));
	private static final AnnotatedParametersParameterAccessor PATH_VARIABLE_ACCESSOR //
			= new AnnotatedParametersParameterAccessor(new AnnotationAttribute(PathVariable.class));
	private static final AnnotatedParametersParameterAccessor REQUEST_PARAM_ACCESSOR //
			= new RequestParamParameterAccessor();

	private static final Map<AffordanceKey, List<Affordance>> AFFORDANCES_CACHE = new ConcurrentReferenceHashMap<>();

	public interface LinkBuilderCreator<T extends LinkBuilder> {
		T createBuilder(UriComponents components, TemplateVariables variables, List<Affordance> affordances);
	}

	public static <T extends LinkBuilder> Function<Function<String, UriComponentsBuilder>, T> linkTo(
			Object invocationValue, LinkBuilderCreator<T> creator) {
		return linkTo(invocationValue, creator, null);
	}

	public static <T extends LinkBuilder> Function<Function<String, UriComponentsBuilder>, T> linkTo(
			Object invocationValue, LinkBuilderCreator<T> creator,
			@Nullable BiFunction<UriComponentsBuilder, MethodInvocation, UriComponentsBuilder> additionalUriHandler) {

		Assert.isInstanceOf(LastInvocationAware.class, invocationValue);

		LastInvocationAware invocations = (LastInvocationAware) invocationValue;
		MethodInvocation invocation = invocations.getLastInvocation();

		return mappingToUriComponentsBuilder -> {

			String mapping = DISCOVERER.getMapping(invocation.getTargetType(), invocation.getMethod());

			UriComponentsBuilder builder = mappingToUriComponentsBuilder.apply(mapping);
			UriTemplate template = UriTemplateFactory.templateFor(mapping == null ? "/" : mapping);
			Map<String, Object> values = new HashMap<>();

			Iterator<String> names = template.getVariableNames().iterator();
			Iterator<Object> classMappingParameters = invocations.getObjectParameters();

			while (classMappingParameters.hasNext()) {
				values.put(names.next(), encodePath(classMappingParameters.next()));
			}

			for (AnnotatedParametersParameterAccessor.BoundMethodParameter parameter : PATH_VARIABLE_ACCESSOR
					.getBoundParameters(invocation)) {

				values.put(parameter.getVariableName(), encodePath(parameter.asString()));
			}

			List<String> optionalEmptyParameters = new ArrayList<>();

			for (AnnotatedParametersParameterAccessor.BoundMethodParameter parameter : REQUEST_PARAM_ACCESSOR
					.getBoundParameters(invocation)) {

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

			UriComponents components = additionalUriHandler == null //
					? builder.buildAndExpand(values)
					: additionalUriHandler.apply(builder, invocation).buildAndExpand(values);

			TemplateVariables variables = NONE;

			for (String parameter : optionalEmptyParameters) {

				boolean previousRequestParameter = components.getQueryParams().isEmpty() && variables.equals(NONE);
				TemplateVariable variable = new TemplateVariable(parameter,
						previousRequestParameter ? REQUEST_PARAM : REQUEST_PARAM_CONTINUED);
				variables = variables.concat(variable);
			}

			String href = components.toUriString().equals("") ? "/" : components.toUriString();

			List<Affordance> affordances = AFFORDANCES_CACHE.computeIfAbsent(
					AffordanceKey.of(invocation.getTargetType(), invocation.getMethod(), href),
					key -> SpringAffordanceBuilder.create(key.type, key.method, key.href, DISCOVERER));

			return creator.createBuilder(components, variables, affordances);
		};
	}

	/**
	 * Populates the given {@link UriComponentsBuilder} with request parameters found in the given
	 * {@link AnnotatedParametersParameterAccessor.BoundMethodParameter}.
	 *
	 * @param builder must not be {@literal null}.
	 * @param parameter must not be {@literal null}.
	 */
	@SuppressWarnings("unchecked")
	private static void bindRequestParameters(UriComponentsBuilder builder,
			AnnotatedParametersParameterAccessor.BoundMethodParameter parameter) {

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
				if (key != null) {
					builder.queryParam(key, encodeParameter(element));
				}
			}

		} else if (SKIP_VALUE.equals(value)) {

			if (parameter.isRequired()) {
				if (key != null) {
					builder.queryParam(key, String.format("{%s}", parameter.getVariableName()));
				}
			}

		} else {
			if (key != null) {
				builder.queryParam(key, encodeParameter(parameter.asString()));
			}
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
		protected BoundMethodParameter createParameter(final MethodParameter parameter, @Nullable Object value,
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

					return annotation != null && annotation.required() //
							&& annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE);
				}
			};
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor#verifyParameterValue(org.springframework.core.MethodParameter, java.lang.Object)
		 */
		@Override
		@Nullable
		protected Object verifyParameterValue(MethodParameter parameter, @Nullable Object value) {

			RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);

			value = ObjectUtils.unwrapOptional(value);

			if (value != null) {
				return value;
			}

			if (!(annotation != null && annotation.required()) || parameter.isOptional()) {
				return SKIP_VALUE;
			}

			return annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE) ? SKIP_VALUE : null;
		}
	}

	/**
	 * Value object to allow accessing {@link MethodInvocation} parameters with the configured
	 * {@link AnnotationAttribute}.
	 *
	 * @author Oliver Gierke
	 */
	@RequiredArgsConstructor
	private static class AnnotatedParametersParameterAccessor {

		private final @NonNull AnnotationAttribute attribute;

		/**
		 * Returns {@link BoundMethodParameter}s contained in the given {@link MethodInvocation}.
		 *
		 * @param invocation must not be {@literal null}.
		 * @return
		 */
		public List<BoundMethodParameter> getBoundParameters(MethodInvocation invocation) {

			Assert.notNull(invocation, "MethodInvocation must not be null!");

			MethodParameters parameters = MethodParameters.of(invocation.getMethod());
			Object[] arguments = invocation.getArguments();
			List<BoundMethodParameter> result = new ArrayList<>();

			for (MethodParameter parameter : parameters.getParametersWith(attribute.getAnnotationType())) {

				Object value = arguments[parameter.getParameterIndex()];
				Object verifiedValue = verifyParameterValue(parameter, value);

				if (verifiedValue != null) {
					result.add(createParameter(parameter, verifiedValue, attribute));
				}
			}

			return result;
		}

		/**
		 * Create the {@link BoundMethodParameter} for the given {@link MethodParameter}, parameter value and
		 * {@link AnnotationAttribute}.
		 *
		 * @param parameter must not be {@literal null}.
		 * @param value can be {@literal null}.
		 * @param attribute must not be {@literal null}.
		 * @return
		 */
		protected BoundMethodParameter createParameter(MethodParameter parameter, @Nullable Object value,
				AnnotationAttribute attribute) {
			return new BoundMethodParameter(parameter, value, attribute);
		}

		/**
		 * Callback to verify the parameter values given for a dummy invocation. Default implementation rejects
		 * {@literal null} values as they indicate an invalid dummy call.
		 *
		 * @param parameter will never be {@literal null}.
		 * @param value could be {@literal null}.
		 * @return the verified value.
		 */
		@Nullable
		protected Object verifyParameterValue(MethodParameter parameter, @Nullable Object value) {
			return value;
		}

		/**
		 * Represents a {@link MethodParameter} alongside the value it has been bound to.
		 *
		 * @author Oliver Gierke
		 */
		static class BoundMethodParameter {

			private static final ConversionService CONVERSION_SERVICE = new DefaultFormattingConversionService();
			private static final TypeDescriptor STRING_DESCRIPTOR = TypeDescriptor.valueOf(String.class);

			private final MethodParameter parameter;
			private final Object value;
			private final AnnotationAttribute attribute;
			private final TypeDescriptor parameterTypeDescriptor;

			/**
			 * Creates a new {@link BoundMethodParameter}
			 *
			 * @param parameter must not be {@literal null}.
			 * @param value can be {@literal null}.
			 * @param attribute can be {@literal null}.
			 */
			public BoundMethodParameter(MethodParameter parameter, @Nullable Object value, AnnotationAttribute attribute) {

				Assert.notNull(parameter, "MethodParameter must not be null!");

				boolean isOptionalWrapper = Optional.class.isAssignableFrom(parameter.getParameterType());

				this.parameter = parameter;
				this.value = value;
				this.attribute = attribute;
				this.parameterTypeDescriptor = TypeDescriptor.nested(parameter, isOptionalWrapper ? 1 : 0);
			}

			/**
			 * Returns the name of the {@link UriTemplate} variable to be bound. The name will be derived from the configured
			 * {@link AnnotationAttribute} or the {@link MethodParameter} name as fallback.
			 *
			 * @return
			 */
			@Nullable
			public String getVariableName() {

				if (attribute == null) {
					return parameter.getParameterName();
				}

				Annotation annotation = parameter.getParameterAnnotation(attribute.getAnnotationType());
				String annotationAttributeValue = annotation != null ? attribute.getValueFrom(annotation) : "";

				return StringUtils.hasText(annotationAttributeValue) ? annotationAttributeValue : parameter.getParameterName();
			}

			/**
			 * Returns the raw value bound to the {@link MethodParameter}.
			 *
			 * @return
			 */
			@Nullable
			public Object getValue() {
				return value;
			}

			/**
			 * Returns the bound value converted into a {@link String} based on default conversion service setup.
			 *
			 * @return
			 */
			public String asString() {

				Object value = this.value;

				if (value == null) {
					throw new IllegalStateException("Cannot turn null value into required String!");
				}

				Object result = CONVERSION_SERVICE.convert(value, parameterTypeDescriptor, STRING_DESCRIPTOR);

				if (result == null) {
					throw new IllegalStateException(String.format("Conversion of value %s resulted in null!", value));
				}

				return (String) result;
			}

			/**
			 * Returns whether the given parameter is a required one. Defaults to {@literal true}.
			 *
			 * @return
			 */
			public boolean isRequired() {
				return true;
			}
		}
	}

	@Value(staticConstructor = "of")
	private static class AffordanceKey {

		Class<?> type;
		Method method;
		String href;
	}
}
