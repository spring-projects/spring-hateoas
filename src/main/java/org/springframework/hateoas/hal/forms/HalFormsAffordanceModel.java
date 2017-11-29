/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponents;

/**
 * {@link AffordanceModel} for a HAL-FORMS {@link org.springframework.http.MediaType}.
 * 
 * @author Greg Turnquist
 */
@Slf4j
class HalFormsAffordanceModel implements AffordanceModel {

	private static final List<HttpMethod> METHODS_FOR_INPUT_DETECTTION = Arrays.asList(HttpMethod.POST, HttpMethod.PUT,
			HttpMethod.PATCH);

	private final UriComponents components;
	private final boolean required;
	private final Map<String, Class<?>> properties;

	public HalFormsAffordanceModel(Affordance affordance, MethodInvocation invocationValue, UriComponents components) {

		this.components = components;
		this.required = determineRequired(affordance.getHttpMethod());
		this.properties = METHODS_FOR_INPUT_DETECTTION.contains(affordance.getHttpMethod()) //
				? determineAffordanceInputs(invocationValue.getMethod()) //
				: Collections.<String, Class<?>> emptyMap();
	}

	/**
	 * Transform the details of the Spring MVC method's {@link RequestBody} into a collection of
	 * {@link HalFormsProperty}s.
	 * 
	 * @return
	 */
	public List<HalFormsProperty> getProperties() {

		return properties.entrySet().stream() //
				.map(entry -> entry.getKey()) //
				.map(key -> HalFormsProperty.named(key).withRequired(required)).collect(Collectors.toList());
	}

	public String getPath() {
		return components.getPath();
	}

	/**
	 * Returns whether the affordance is pointing to the same path as the given one.
	 * 
	 * @param path must not be {@literal null}.
	 * @return
	 */
	public boolean hasPath(String path) {

		Assert.notNull(path, "Path must not be null!");

		return getPath().equals(path);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.AffordanceModel#getMediaType()
	 */
	@Override
	public Collection<MediaType> getMediaTypes() {
		return Collections.singleton(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * Based on the Spring MVC controller's HTTP method, decided whether or not input attributes are required or not.
	 *
	 * @param httpMethod - string representation of an HTTP method, e.g. GET, POST, etc.
	 * @return
	 */
	private boolean determineRequired(HttpMethod httpMethod) {
		return Arrays.asList(HttpMethod.POST, HttpMethod.PUT).contains(httpMethod);
	}

	/**
	 * Look at the inputs for a Spring MVC controller method to decide the {@link Affordance}'s properties.
	 *
	 * @param method - {@link Method} of the Spring MVC controller tied to this affordance
	 */
	private Map<String, Class<?>> determineAffordanceInputs(Method method) {

		if (method == null) {
			return Collections.emptyMap();
		}

		LOG.debug("Gathering details about " + method.getDeclaringClass().getCanonicalName() + "." + method.getName());

		Map<String, Class<?>> properties = new TreeMap<>();
		MethodParameters parameters = new MethodParameters(method);

		for (MethodParameter parameter : parameters.getParametersWith(RequestBody.class)) {

			Class<?> parameterType = parameter.getParameterType();

			LOG.debug("\tRequest body: " + parameterType.getCanonicalName() + "(");

			for (PropertyDescriptor descriptor : BeanUtils.getPropertyDescriptors(parameterType)) {

				if (!descriptor.getName().equals("class")) {

					LOG.debug("\t\t" + descriptor.getPropertyType().getCanonicalName() + " " + descriptor.getName());
					properties.put(descriptor.getName(), descriptor.getPropertyType());
				}
			}

			LOG.debug(")");
		}

		LOG.debug("Assembled " + this.toString());
		return properties;
	}
}
