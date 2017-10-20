/*
 * Copyright 2012-2017 the original author or authors.
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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ConcurrentReferenceHashMap.ReferenceType;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriTemplate;

/**
 * Value object to allow accessing {@link MethodInvocation} parameters with the configured {@link AnnotationAttribute}.
 * 
 * @author Oliver Gierke
 */
@RequiredArgsConstructor
class AnnotatedParametersParameterAccessor {

	private static final Map<Method, MethodParameters> METHOD_PARAMETERS_CACHE = new ConcurrentReferenceHashMap<>(
			16, ReferenceType.WEAK);

	private final @NonNull AnnotationAttribute attribute;

	/**
	 * Returns {@link BoundMethodParameter}s contained in the given {@link MethodInvocation}.
	 * 
	 * @param invocation must not be {@literal null}.
	 * @return
	 */
	public List<BoundMethodParameter> getBoundParameters(MethodInvocation invocation) {

		Assert.notNull(invocation, "MethodInvocation must not be null!");

		MethodParameters parameters = getOrCreateMethodParametersFor(invocation.getMethod());
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
	protected BoundMethodParameter createParameter(MethodParameter parameter, Object value,
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
	protected Object verifyParameterValue(MethodParameter parameter, Object value) {
		return value;
	}

	/**
	 * Returns the {@link MethodParameters} for the given {@link Method}.
	 * 
	 * @param method
	 * @return
	 */
	private static MethodParameters getOrCreateMethodParametersFor(Method method) {
		return METHOD_PARAMETERS_CACHE.computeIfAbsent(method, MethodParameters::new);
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
		public BoundMethodParameter(MethodParameter parameter, Object value, AnnotationAttribute attribute) {

			Assert.notNull(parameter, "MethodParameter must not be null!");

			this.parameter = parameter;
			this.value = value;
			this.attribute = attribute;
			this.parameterTypeDescriptor = TypeDescriptor.nested(parameter, parameter.isOptional() ? 1 : 0);
		}

		/**
		 * Returns the name of the {@link UriTemplate} variable to be bound. The name will be derived from the configured
		 * {@link AnnotationAttribute} or the {@link MethodParameter} name as fallback.
		 * 
		 * @return
		 */
		public String getVariableName() {

			if (attribute == null) {
				return parameter.getParameterName();
			}

			Annotation annotation = parameter.getParameterAnnotation(attribute.getAnnotationType());
			String annotationAttributeValue = attribute.getValueFrom(annotation);

			return StringUtils.hasText(annotationAttributeValue) ? annotationAttributeValue : parameter.getParameterName();
		}

		/**
		 * Returns the raw value bound to the {@link MethodParameter}.
		 * 
		 * @return
		 */
		public Object getValue() {
			return value;
		}

		/**
		 * Returns the bound value converted into a {@link String} based on default conversion service setup.
		 * 
		 * @return
		 */
		public String asString() {

			return value == null //
					? null //
					: (String) CONVERSION_SERVICE.convert(value, parameterTypeDescriptor, STRING_DESCRIPTOR);
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
