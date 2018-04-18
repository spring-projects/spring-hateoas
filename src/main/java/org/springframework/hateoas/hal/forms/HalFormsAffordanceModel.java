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

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.GenericAffordanceModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.support.PropertyUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * {@link GenericAffordanceModel} for a HAL-FORMS {@link MediaType}.
 * 
 * @author Greg Turnquist
 */
public class HalFormsAffordanceModel extends GenericAffordanceModel {

	private final @Getter List<HalFormsProperty> inputProperties;

	public HalFormsAffordanceModel(String name, Link link, HttpMethod httpMethod, ResolvableType inputType, List<QueryParameter> queryMethodParameters, ResolvableType outputType) {

		super(name, link, httpMethod, inputType, queryMethodParameters, outputType);
		
		this.inputProperties = determineInputs();
	}

	/**
	 * Look at the input's domain type to extract the {@link Affordance}'s properties.
	 * Then transform them into a list of {@link HalFormsProperty} objects.
	 */
	private List<HalFormsProperty> determineInputs() {

		if (Arrays.asList(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH).contains(getHttpMethod())) {
			
			return PropertyUtils.findPropertyNames(getInputType()).stream()
				.map(propertyName -> new HalFormsProperty()
					.withName(propertyName)
					.withRequired(Arrays.asList(HttpMethod.POST, HttpMethod.PUT).contains(getHttpMethod())))
				.collect(Collectors.toList());
			
		} else {
			return Collections.emptyList();
		}
	}

	public String getURI() {
		return getLink().expand().getHref();
	}
}
