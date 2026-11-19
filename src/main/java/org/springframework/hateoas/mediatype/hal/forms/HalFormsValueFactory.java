/*
 * Copyright 2023 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.AffordanceModel;
import org.springframework.util.Assert;

/**
 *
 * Factory implementation to register {@link HalFormsValuesCreator}s to decouple the registration (via
 * {@link HalFormsConfiguration}) from the consumption during rendering.
 *
 * @author Réda Housni Alaoui
 */
class HalFormsValueFactory {

	private final Map<Class<?>, HalFormsValuesCreator> creators;

	/**
	 * Creates a new, empty {@link HalFormsValueFactory}.
	 */
	public HalFormsValueFactory() {
		this.creators = new HashMap<>();
	}

	/**
	 * Copy-constructor to keep {@link HalFormsValueFactory} immutable during registrations.
	 *
	 * @param creators must not be {@literal null}.
	 */
	private HalFormsValueFactory(Map<Class<?>, HalFormsValuesCreator> creators) {
		this.creators = creators;
	}

	/**
	 * Registers the {@link HalFormsValuesCreator} of the given type, replacing any creator previously registered for it.
	 *
	 * @param type must not be {@literal null}.
	 * @param creator must not be {@literal null}.
	 */
	HalFormsValueFactory withValues(Class<?> type, HalFormsValuesCreator creator) {

		Assert.notNull(type, "Type must not be null!");
		Assert.notNull(creator, "Creator must not be null!");

		Map<Class<?>, HalFormsValuesCreator> creators = new HashMap<>(this.creators);
		creators.put(type, creator);

		return new HalFormsValueFactory(creators);
	}

	/**
	 * Returns the values of the properties of the given {@link AffordanceModel.InputPayloadMetadata}.
	 *
	 * @param payload must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	HalFormsValues getValues(AffordanceModel.InputPayloadMetadata payload) {

		Assert.notNull(payload, "Payload metadata must not be null!");

		HalFormsValuesCreator creator = creators.get(payload.getType());

		if (creator == null) {
			return HalFormsValues.none();
		}

		HalFormsValues values = creator.create(new HalFormsInputProperties(payload));

		Assert.notNull(values, "HalFormsValues must not be null!");

		return values;
	}
}
