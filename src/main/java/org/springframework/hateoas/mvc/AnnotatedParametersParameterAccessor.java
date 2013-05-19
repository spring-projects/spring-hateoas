/*
 * Copyright 2012-2013 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriTemplate;

/**
 * Value object to allow accessing {@link MethodInvocation} parameters with the configured {@link AnnotationAttribute}.
 * 
 * @author Oliver Gierke
 */
class AnnotatedParametersParameterAccessor {

	private final AnnotationAttribute attribute;

	/**
	 * Creates a new {@link AnnotatedParametersParameterAccessor} using the given {@link AnnotationAttribute}.
	 * 
	 * @param attribute must not be {@literal null}.
	 */
	public AnnotatedParametersParameterAccessor(AnnotationAttribute attribute) {

		Assert.notNull(attribute);
		this.attribute = attribute;
	}

	/**
	 * Returns {@link BoundMethodParameter}s contained in the given {@link MethodInvocation}.
	 * 
	 * @param invocation must not be {@literal null}.
	 * @return
	 */
	public List<BoundMethodParameter> getBoundParameters(MethodInvocation invocation) {

		Assert.notNull(invocation, "MethodInvocation must not be null!");

		MethodParameters parameters = new MethodParameters(invocation.getMethod());
		Object[] arguments = invocation.getArguments();
		List<BoundMethodParameter> result = new ArrayList<BoundMethodParameter>();

		for (MethodParameter parameter : parameters.getParametersWith(attribute.getAnnotationType())) {
			result.add(new BoundMethodParameter(parameter, arguments[parameter.getParameterIndex()], attribute));
		}

		return result;
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
		 * @param parameter
		 * @param value
		 * @param attribute
		 */
		public BoundMethodParameter(MethodParameter parameter, Object value, AnnotationAttribute attribute) {

			Assert.notNull(parameter, "MethodParameter must not be null!");

			this.parameter = parameter;
			this.value = value;
			this.attribute = attribute;
			this.parameterTypeDescriptor = TypeDescriptor.nested(parameter, 0);
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

			if (value == null) {
				return null;
			}

			return (String) CONVERSION_SERVICE.convert(value, parameterTypeDescriptor, STRING_DESCRIPTOR);
		}
	}

	public Map<String, MethodParameterValue> getBoundMethodParameterValues(MethodInvocation invocation) {

		List<BoundMethodParameter> boundParameters = getBoundParameters(invocation);
		Map<String, MethodParameterValue> result = new HashMap<String, MethodParameterValue>();
		for (BoundMethodParameter boundMethodParameter : boundParameters) {
			String key = boundMethodParameter.getVariableName();
			MethodParameter parameter = boundMethodParameter.parameter;
			Object value = boundMethodParameter.getValue();
			String formatted = boundMethodParameter.asString();
			result.put(key , new MethodParameterValue(parameter , value, formatted));
		}
		return result;
	}
}
