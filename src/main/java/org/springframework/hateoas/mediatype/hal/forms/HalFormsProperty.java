/*
 * Copyright 2016-2020 the original author or authors.
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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.Wither;

import org.springframework.hateoas.AffordanceModel.Named;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.AffordanceModel.PropertyMetadataConfigured;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describe a parameter for the associated state transition in a HAL-FORMS document. A {@link HalFormsTemplate} may
 * contain a list of {@link HalFormsProperty}s
 *
 * @see https://mamund.site44.com/misc/hal-forms/
 */
@JsonInclude(Include.NON_DEFAULT)
@Value
@Wither
@Getter(onMethod = @__(@JsonProperty))
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@ToString
public class HalFormsProperty implements PropertyMetadataConfigured<HalFormsProperty>, Named {

	private @NonNull String name;
	private @JsonInclude(Include.NON_DEFAULT) boolean readOnly;
	private String value;
	private @JsonInclude(Include.NON_EMPTY) String prompt;
	private String regex;
	private boolean templated;
	private @JsonInclude(Include.NON_DEFAULT) boolean required;
	private boolean multi;

	/**
	 * Creates a new {@link HalFormsProperty} with the given name.
	 *
	 * @param name must not be {@literal null}.
	 * @return
	 */
	public static HalFormsProperty named(String name) {
		return new HalFormsProperty().withName(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.mediatype.PropertyMetadataAware#apply(org.springframework.hateoas.mediatype.PropertyUtils.PropertyMetadata)
	 */
	public HalFormsProperty apply(PropertyMetadata metadata) {

		HalFormsProperty customized = withRequired(metadata.isRequired()) //
				.withReadOnly(metadata.isReadOnly());

		return metadata.getPattern() //
				.map(customized::withRegex) //
				.orElse(customized);
	}
}
