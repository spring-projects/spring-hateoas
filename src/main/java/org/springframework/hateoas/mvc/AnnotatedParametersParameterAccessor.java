/*
 * Copyright 2012 the original author or authors.
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
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.MethodParameter;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Oliver Gierke
 */
public class AnnotatedParametersParameterAccessor {

	private final AnnotationAttribute attribute;

	public AnnotatedParametersParameterAccessor(AnnotationAttribute attribute) {

		Assert.notNull(attribute);
		this.attribute = attribute;
	}

	public Map<String, Object> getBoundParameters(MethodInvocation invocation) {

		MethodParameters parameters = new MethodParameters(invocation.getMethod());
		Object[] arguments = invocation.getArguments();
		Map<String, Object> result = new HashMap<String, Object>();

		for (MethodParameter parameter : parameters.getParametersWith(attribute.getAnnotationType())) {

			Annotation annotation = parameter.getParameterAnnotation(attribute.getAnnotationType());
			String annotationAttributeValue = attribute.getValueFrom(annotation);
			String key = StringUtils.hasText(annotationAttributeValue) ? annotationAttributeValue : parameter
					.getParameterName();
			result.put(key, arguments[parameter.getParameterIndex()]);
		}

		return result;
	}
}
