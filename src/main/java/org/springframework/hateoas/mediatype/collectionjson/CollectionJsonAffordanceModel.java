/*
 * Copyright 2018-2021 the original author or authors.
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.mediatype.ConfiguredAffordance;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

/**
 * {@link AffordanceModel} for Collection+JSON.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
class CollectionJsonAffordanceModel extends AffordanceModel {

	private static final Set<HttpMethod> ENTITY_ALTERING_METHODS = EnumSet.of(HttpMethod.POST, HttpMethod.PUT,
			HttpMethod.PATCH);

	private final List<CollectionJsonData> inputProperties;
	private final List<CollectionJsonData> queryProperties;

	CollectionJsonAffordanceModel(ConfiguredAffordance configured) {

		super(configured.getNameOrDefault(), configured.getTarget(),
				configured.getMethod(), configured.getInputMetadata(), configured.getQueryParameters(),
				configured.getOutputMetadata());

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

	public List<CollectionJsonData> getInputProperties() {
		return this.inputProperties;
	}

	public List<CollectionJsonData> getQueryProperties() {
		return this.queryProperties;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.AffordanceModel#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof CollectionJsonAffordanceModel)) {
			return false;
		}

		if (!super.equals(o)) {
			return false;
		}

		CollectionJsonAffordanceModel that = (CollectionJsonAffordanceModel) o;

		return Objects.equals(this.inputProperties, that.inputProperties)
				&& Objects.equals(this.queryProperties, that.queryProperties);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.AffordanceModel#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.inputProperties, this.queryProperties);
	}
}
