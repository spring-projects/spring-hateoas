/*
 * Copyright 2018-2019 the original author or authors.
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
package org.springframework.hateoas.mediatype.uber;

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.QueryParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

/**
 * {@link AffordanceModel} for {@literal UBER+JSON}.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
class UberAffordanceModel extends AffordanceModel {

	private static final Set<HttpMethod> ENTITY_ALTERING_METHODS = EnumSet.of(HttpMethod.POST, HttpMethod.PUT,
			HttpMethod.PATCH);

	private final @Getter Collection<MediaType> mediaTypes = Collections.singleton(MediaTypes.UBER_JSON);

	private final @Getter List<UberData> inputProperties;
	private final @Getter List<UberData> queryProperties;

	UberAffordanceModel(String name, Link link, HttpMethod httpMethod, InputPayloadMetadata inputType,
			List<QueryParameter> queryMethodParameters, PayloadMetadata outputType) {
		super(name, link, httpMethod, inputType, queryMethodParameters, outputType);

		this.inputProperties = determineAffordanceInputs();
		this.queryProperties = determineQueryProperties();
	}

	private List<UberData> determineAffordanceInputs() {

		if (!ENTITY_ALTERING_METHODS.contains(getHttpMethod())) {
			return Collections.emptyList();
		}

		return getInput().stream()//
				.map(PropertyMetadata::getName) //
				.map(propertyName -> new UberData() //
						.withName(propertyName) //
						.withValue("")) //
				.collect(Collectors.toList());
	}

	/**
	 * Transform GET-based query parameters (e.g. {@literal &query}) into a list of {@link UberData} objects.
	 */
	private List<UberData> determineQueryProperties() {

		if (!getHttpMethod().equals(HttpMethod.GET)) {
			return Collections.emptyList();
		}

		if (getHttpMethod().equals(HttpMethod.GET)) {
			return getQueryMethodParameters().stream()
					.map(queryParameter -> new UberData().withName(queryParameter.getName()).withValue(""))
					.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	@Nullable
	UberAction getAction() {
		return UberAction.forRequestMethod(getHttpMethod());
	}
}
