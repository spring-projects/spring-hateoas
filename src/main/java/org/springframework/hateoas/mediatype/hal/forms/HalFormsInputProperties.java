/*
 * Copyright 2026 the original author or authors.
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.util.Assert;

/**
 * The {@link PropertyMetadata} of the properties of a HAL-FORMS template input.
 *
 * @author Réda Housni Alaoui
 * @see HalFormsValuesCreator
 */
public final class HalFormsInputProperties {

	private final Map<String, PropertyMetadata> propertyByName;

	HalFormsInputProperties(InputPayloadMetadata payload) {

		Assert.notNull(payload, "Payload metadata must not be null!");

		Map<String, PropertyMetadata> propertyByName = new LinkedHashMap<>();
		payload.stream().forEach(it -> propertyByName.put(it.getName(), it));

		this.propertyByName = Collections.unmodifiableMap(propertyByName);
	}

	/**
	 * @param name must not be {@literal null}.
	 * @return the metadata of the property with the given name, if the input has one.
	 */
	public Optional<PropertyMetadata> getProperty(String name) {

		Assert.notNull(name, "Name must not be null!");

		return Optional.ofNullable(propertyByName.get(name));
	}

	/**
	 * @return the metadata of every property of the input, in the order of the input.
	 */
	public Stream<PropertyMetadata> stream() {
		return propertyByName.values().stream();
	}
}
