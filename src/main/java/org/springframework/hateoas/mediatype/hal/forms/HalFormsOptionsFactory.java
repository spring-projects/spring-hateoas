/*
 * Copyright 2021 the original author or authors.
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

import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Factory implementation to register creator functions to eventually create {@link HalFormsOptions} from
 * {@link PropertyMetadata} to decouple the registration (via {@link HalFormsConfiguration}) from the consumption during
 * rendering.
 *
 * @author Oliver Drotbohm
 * @since 1.3
 */
class HalFormsOptionsFactory {

	private final Map<Class<?>, Map<String, Function<PropertyMetadata, HalFormsOptions>>> options;

	/**
	 * Creates a new, empty {@link HalFormsOptionsFactory}.
	 */
	public HalFormsOptionsFactory() {
		this.options = new HashMap<>();
	}

	/**
	 * Copy-constructor to keep {@link HalFormsConfiguration} immutable during registrations.
	 *
	 * @param options must not be {@literal null}.
	 */
	private HalFormsOptionsFactory(Map<Class<?>, Map<String, Function<PropertyMetadata, HalFormsOptions>>> options) {
		this.options = options;
	}

	/**
	 * Registers a {@link Function} to create a {@link HalFormsOptions} instance from the given {@link PropertyMetadata}
	 * to supply options for the given property of the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @param property
	 * @param creator
	 * @return
	 * @see HalFormsOptions#inline(Object...)
	 * @see HalFormsOptions#remote(org.springframework.hateoas.Link)
	 */
	HalFormsOptionsFactory withOptions(Class<?> type, String property,
			Function<PropertyMetadata, HalFormsOptions> creator) {

		Assert.notNull(type, "Type must not be null!");
		Assert.hasText(property, "Property must not be null or empty!");
		Assert.notNull(creator, "Creator function must not be null!");

		Map<Class<?>, Map<String, Function<PropertyMetadata, HalFormsOptions>>> options = new HashMap<>(this.options);

		options.compute(type, (it, map) -> {

			if (map == null) {
				map = new HashMap<>();
			}

			map.put(property, creator);

			return map;
		});

		return new HalFormsOptionsFactory(options);
	}

	/**
	 * Returns the {@link HalFormsOptions} to be used for the property with the given {@link PayloadMetadata} and
	 * {@link PropertyMetadata}.
	 *
	 * @param payload must not be {@literal null}.
	 * @param property must not be {@literal null}.
	 * @return
	 */
	@Nullable
	HalFormsOptions getOptions(PayloadMetadata payload, PropertyMetadata property) {

		Assert.notNull(payload, "Payload metadata must not be null!");
		Assert.notNull(property, "Property metadata must not be null!");

		Class<?> type = payload.getType();
		String name = property.getName();

		Map<String, Function<PropertyMetadata, HalFormsOptions>> map = options.get(type);

		if (map == null) {
			return null;
		}

		Function<PropertyMetadata, HalFormsOptions> function = map.get(name);

		return function == null ? null : function.apply(property);
	}
}
