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
package org.springframework.hateoas.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;

/**
 * @author Oliver Gierke
 */
public class MethodParameters {

	private final List<MethodParameter> parameters;

	/**
	 * Creates a new {@link MethodParameter} from the given {@link Method}.
	 * 
	 * @param method must not be {@literal null}.
	 */
	public MethodParameters(Method method) {

		Assert.notNull(method);
		this.parameters = new ArrayList<MethodParameter>();

		for (int i = 0; i < method.getParameterTypes().length; i++) {
			parameters.add(new MethodParameter(method, i));
		}
	}

	/**
	 * Returns all {@link MethodParameter}s.
	 * 
	 * @return
	 */
	public List<MethodParameter> getParameters() {
		return parameters;
	}

	/**
	 * Returns all {@link MethodParameter}s annotated with the given annotation type.
	 * 
	 * @param annotation must not be {@literal null}.
	 * @return
	 */
	public List<MethodParameter> getParametersWith(Class<? extends Annotation> annotation) {

		Assert.notNull(annotation);
		List<MethodParameter> result = new ArrayList<MethodParameter>();

		for (MethodParameter parameter : getParameters()) {
			if (parameter.hasParameterAnnotation(annotation)) {
				result.add(parameter);
			}
		}

		return result;
	}
}
