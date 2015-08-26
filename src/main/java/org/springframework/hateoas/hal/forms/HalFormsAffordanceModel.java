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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.hateoas.support.PropertyUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

/**
 * {@link AffordanceModel} for a HAL-FORMS {@link org.springframework.http.MediaType}.
 * 
 * @author Greg Turnquist
 */
class HalFormsAffordanceModel implements AffordanceModel {

	private static final List<HttpMethod> METHODS_FOR_INPUT_DETECTTION = Arrays.asList(HttpMethod.POST, HttpMethod.PUT,
			HttpMethod.PATCH);

	private final Affordance affordance;
	private final UriComponents components;
	private final boolean required;
	private final List<String> properties;

	public HalFormsAffordanceModel(Affordance affordance, UriComponents components) {

		this.affordance = affordance;
		this.components = components;
		this.required = determineRequired(affordance.getHttpMethod());
		this.properties = METHODS_FOR_INPUT_DETECTTION.contains(affordance.getHttpMethod()) //
				? determineAffordanceInputs() //
				: Collections.emptyList();
	}

	/**
	 * Transform the details of the REST method's {@link MethodParameters} into
	 * {@link HalFormsProperty}s.
	 * 
	 * @return
	 */
	public List<HalFormsProperty> getProperties() {

		return properties.stream() //
			.map(name -> HalFormsProperty.named(name).withRequired(required)) //
			.collect(Collectors.toList());
	}

	public String getURI() {
		return components.toUriString();
	}

	/**
	 * Returns whether the affordance is pointing to the same path as the given one.
	 * 
	 * @param path must not be {@literal null}.
	 * @return
	 */
	public boolean hasPath(String path) {

		Assert.notNull(path, "Path must not be null!");

		return getURI().equals(path);
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
	 */
	private List<String> determineAffordanceInputs() {

		return this.affordance.getInputMethodParameters().stream()
			.findFirst()
			.map(methodParameter -> {
				ResolvableType resolvableType = ResolvableType.forMethodParameter(methodParameter);
				return PropertyUtils.findProperties(resolvableType);
			})
			.orElse(Collections.emptyList());
	}
}
