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

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

/**
 * The values of the properties of a HAL-FORMS template, keyed by property name.
 *
 * @author Réda Housni Alaoui
 * @see https://rwcbook.github.io/hal-forms/#property-element
 * @see HalFormsValuesCreator
 */
public final class HalFormsValues {

	private static final HalFormsValues NONE = new HalFormsValues(Map.of());

	private final Map<String, Object> valueByProperty;

	private HalFormsValues(Map<String, Object> valueByProperty) {
		this.valueByProperty = valueByProperty;
	}

	/**
	 * @return values holding no property value.
	 */
	public static HalFormsValues none() {
		return NONE;
	}

	/**
	 * Returns new values that also hold the given value of the given property, replacing any value it already held. A
	 * {@literal null} value leaves the property without a value.
	 *
	 * @param property must not be {@literal null} or empty.
	 * @param value can be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalFormsValues and(String property, @Nullable Object value) {

		Assert.hasText(property, "Property must not be null or empty!");

		Map<String, Object> valueByProperty = new HashMap<>(this.valueByProperty);

		if (value == null) {
			valueByProperty.remove(property);
		} else {
			valueByProperty.put(property, value);
		}

		return new HalFormsValues(Map.copyOf(valueByProperty));
	}

	@Nullable
	Object getValue(String property) {
		return valueByProperty.get(property);
	}

	@Override
	public String toString() {
		return "HalFormsValues" + valueByProperty;
	}
}
