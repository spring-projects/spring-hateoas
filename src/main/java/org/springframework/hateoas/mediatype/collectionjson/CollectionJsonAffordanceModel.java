/*
 * Copyright 2018-2020 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

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

/**
 * {@link AffordanceModel} for Collection+JSON.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@EqualsAndHashCode(callSuper = true)
class CollectionJsonAffordanceModel extends AffordanceModel {

	private static final Set<HttpMethod> ENTITY_ALTERING_METHODS = EnumSet.of(HttpMethod.POST, HttpMethod.PUT,
			HttpMethod.PATCH);

	private final @Getter List<CollectionJsonData> inputProperties;
	private final @Getter List<CollectionJsonData> queryProperties;

	public CollectionJsonAffordanceModel(String name, Link link, HttpMethod httpMethod, InputPayloadMetadata inputType,
			List<QueryParameter> queryMethodParameters, PayloadMetadata outputType) {

		super(name, link, httpMethod, inputType, queryMethodParameters, outputType);

		this.inputProperties = determineInputs();
		this.queryProperties = determineQueryProperties();
	}

	/**
	 * Look at the input's domain type to extract the {@link Affordance}'s properties. Then transform them into a list of
	 * {@link CollectionJsonData} objects.
	 */
	private List<CollectionJsonData> determineInputs() {

		if (!ENTITY_ALTERING_METHODS.contains(getHttpMethod())) {
			return Collections.emptyList();
		}

		return getInput().stream().map(PropertyMetadata::getName) //
				.map(propertyName -> new CollectionJsonData() //
						.withName(propertyName) //
						.withValue("")) //
				.collect(Collectors.toList());
	}

	/**
	 * Transform a list of general {@link QueryParameter}s into a list of {@link CollectionJsonData} objects.
	 *
	 * @return
	 */
	private List<CollectionJsonData> determineQueryProperties() {

		if (!getHttpMethod().equals(HttpMethod.GET)) {
			return Collections.emptyList();
		}

		return getQueryMethodParameters().stream() //
				.map(queryProperty -> new CollectionJsonData() //
						.withName(queryProperty.getName()) //
						.withValue("")) //
				.collect(Collectors.toList());
	}
}
