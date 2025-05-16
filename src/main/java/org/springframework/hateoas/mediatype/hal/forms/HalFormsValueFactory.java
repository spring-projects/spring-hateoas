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
import java.util.function.Function;

import org.springframework.hateoas.AffordanceModel;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 *
 * Factory implementation to register creator functions to eventually create value from
 * {@link AffordanceModel.PropertyMetadata} to decouple the registration (via {@link HalFormsConfiguration}) from the consumption during
 * rendering.
 *
 * @author Réda Housni Alaoui
 */
class HalFormsValueFactory {

	private final Map<Class<?>, Map<String, Function<AffordanceModel.PropertyMetadata, String>>> values;

	/**
	 * Creates a new, empty {@link HalFormsValueFactory}.
	 */
	public HalFormsValueFactory() {
		this.values = new HashMap<>();
	}

	/**
	 * Copy-constructor to keep {@link HalFormsValueFactory} immutable during registrations.
	 *
	 * @param values must not be {@literal null}.
	 */
	private HalFormsValueFactory(Map<Class<?>, Map<String, Function<AffordanceModel.PropertyMetadata, String>>> values) {
		this.values = values;
	}

	/**
	 * Registers a {@link Function} to create a {@link String} instance from the given {@link AffordanceModel.PropertyMetadata}
	 * to supply value for the given property of the given type.
	 *
	 * @param type must not be {@literal null}.
	 */
	HalFormsValueFactory withValues(Class<?> type, String property,
									   Function<AffordanceModel.PropertyMetadata, String> creator) {

		Assert.notNull(type, "Type must not be null!");
		Assert.hasText(property, "Property must not be null or empty!");
		Assert.notNull(creator, "Creator function must not be null!");

		Map<Class<?>, Map<String, Function<AffordanceModel.PropertyMetadata, String>>> values = new HashMap<>(this.values);

		values.compute(type, (it, map) -> {

			if (map == null) {
				map = new HashMap<>();
			}

			map.put(property, creator);

			return map;
		});

		return new HalFormsValueFactory(values);
	}

	/**
	 * Returns the value to be used for the property with the given {@link AffordanceModel.PayloadMetadata} and
	 * {@link AffordanceModel.PropertyMetadata}.
	 *
	 * @param payload must not be {@literal null}.
	 * @param property must not be {@literal null}.
	 */
	@Nullable
	String getValue(AffordanceModel.PayloadMetadata payload, AffordanceModel.PropertyMetadata property) {

		Assert.notNull(payload, "Payload metadata must not be null!");
		Assert.notNull(property, "Property metadata must not be null!");

		Class<?> type = payload.getType();
		String name = property.getName();

		Map<String, Function<AffordanceModel.PropertyMetadata, String>> map = values.get(type);

		if (map == null) {
			return null;
		}

		Function<AffordanceModel.PropertyMetadata, String> function = map.get(name);

		return function == null ? null : function.apply(property);
	}

}
