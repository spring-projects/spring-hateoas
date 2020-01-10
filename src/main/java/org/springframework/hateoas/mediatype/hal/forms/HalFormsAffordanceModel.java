/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import static org.springframework.http.HttpMethod.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * {@link AffordanceModel} for a HAL-FORMS {@link MediaType}.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
@EqualsAndHashCode(callSuper = true)
class HalFormsAffordanceModel extends AffordanceModel {

	private static final Set<HttpMethod> ENTITY_ALTERING_METHODS = EnumSet.of(POST, PUT, PATCH);

	private final @Getter List<HalFormsProperty> inputProperties;

	public HalFormsAffordanceModel(String name, Link link, HttpMethod httpMethod, InputPayloadMetadata inputType,
			List<QueryParameter> queryMethodParameters, PayloadMetadata outputType) {

		super(name, link, httpMethod, inputType, queryMethodParameters, outputType);

		this.inputProperties = determineInputs();
	}

	/**
	 * Look at the input's domain type to extract the {@link Affordance}'s properties. Then transform them into a list of
	 * {@link HalFormsProperty} objects.
	 */
	private List<HalFormsProperty> determineInputs() {

		if (!ENTITY_ALTERING_METHODS.contains(getHttpMethod())) {
			return Collections.emptyList();
		}

		return getInput().stream() //
				.map(PropertyMetadata::getName) //
				.map(it -> new HalFormsProperty() //
						.withName(it)) //
				.collect(Collectors.toList());
	}
}
