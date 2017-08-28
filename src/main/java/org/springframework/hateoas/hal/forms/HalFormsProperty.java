/*
 * Copyright 2016-2017 the original author or authors.
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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Describe a parameter for the associated state transition in a HAL-FORMS document. A {@link HalFormsTemplate} may
 * contain a list of {@link HalFormsProperty}s
 * 
 * @see http://mamund.site44.com/misc/hal-forms/
 */
@JsonInclude(Include.NON_DEFAULT)
@Value
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class HalFormsProperty {

	private @Wither(AccessLevel.PRIVATE) @NonNull String name;
	private Boolean readOnly;
	private String value;
	private String prompt;
	private String regex;
	private boolean templated;
	private @JsonInclude(Include.ALWAYS) boolean required;
	private boolean multi;

	/**
	 * Creates a new {@link HalFormsProperty} with the given name.
	 * 
	 * @param name must not be {@literal null} or empty.
	 * @return
	 */
	public static HalFormsProperty named(String name) {

		Assert.hasText(name, "Property name must not be null or empty!");

		return new HalFormsProperty().withName(name);
	}
}
