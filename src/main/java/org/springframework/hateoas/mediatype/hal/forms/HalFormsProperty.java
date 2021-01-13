/*
 * Copyright 2016-2021 the original author or authors.
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

import java.util.Objects;

import org.springframework.hateoas.AffordanceModel.Named;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.AffordanceModel.PropertyMetadataConfigured;
import org.springframework.util.Assert;

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
final class HalFormsProperty implements PropertyMetadataConfigured<HalFormsProperty>, Named {

	private final String name;
	private @JsonInclude(Include.NON_DEFAULT) final boolean readOnly;
	private final String value;
	private @JsonInclude(Include.NON_EMPTY) final String prompt;
	private final String regex;
	private final boolean templated;
	private @JsonInclude(Include.NON_DEFAULT) final boolean required;
	private final boolean multi;

	HalFormsProperty() {

		this.name = null;
		this.readOnly = false;
		this.value = null;
		this.prompt = null;
		this.regex = null;
		this.templated = false;
		this.required = false;
		this.multi = false;
	}

	private HalFormsProperty(String name, boolean readOnly, String value, String prompt, String regex, boolean templated,
			boolean required, boolean multi) {

		Assert.notNull(name, "name must not be null!");

		this.name = name;
		this.readOnly = readOnly;
		this.value = value;
		this.prompt = prompt;
		this.regex = regex;
		this.templated = templated;
		this.required = required;
		this.multi = multi;
	}

	/**
	 * Creates a new {@link HalFormsProperty} with the given name.
	 *
	 * @param name must not be {@literal null}.
	 * @return
	 */
	static HalFormsProperty named(String name) {
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

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing the {@literal name}.
	 *
	 * @param name
	 * @return
	 */
	HalFormsProperty withName(String name) {

		Assert.notNull(name, "name must not be null!");

		return this.name == name ? this
				: new HalFormsProperty(name, this.readOnly, this.value, this.prompt, this.regex, this.templated, this.required,
						this.multi);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing the {@literal readOnly}.
	 *
	 * @param readOnly
	 * @return
	 */
	HalFormsProperty withReadOnly(boolean readOnly) {

		return this.readOnly == readOnly ? this
				: new HalFormsProperty(this.name, readOnly, this.value, this.prompt, this.regex, this.templated, this.required,
						this.multi);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing the {@literal value}.
	 *
	 * @param value
	 * @return
	 */
	HalFormsProperty withValue(String value) {

		return this.value == value ? this
				: new HalFormsProperty(this.name, this.readOnly, value, this.prompt, this.regex, this.templated, this.required,
						this.multi);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing the {@literal prompt}.
	 *
	 * @param prompt
	 * @return
	 */
	HalFormsProperty withPrompt(String prompt) {

		return this.prompt == prompt ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, prompt, this.regex, this.templated, this.required,
						this.multi);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing the {@literal regex}.
	 *
	 * @param regex
	 * @return
	 */
	HalFormsProperty withRegex(String regex) {

		return this.regex == regex ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, regex, this.templated, this.required,
						this.multi);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing {@literal templated}.
	 *
	 * @param templated
	 * @return
	 */
	HalFormsProperty withTemplated(boolean templated) {

		return this.templated == templated ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, this.regex, templated, this.required,
						this.multi);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing {@literal required}.
	 *
	 * @param required
	 * @return
	 */
	HalFormsProperty withRequired(boolean required) {

		return this.required == required ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated, required,
						this.multi);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing {@literal multi}.
	 *
	 * @param multi
	 * @return
	 */
	HalFormsProperty withMulti(boolean multi) {

		return this.multi == multi ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated,
						this.required, multi);
	}

	@JsonProperty
	public String getName() {
		return this.name;
	}

	@JsonProperty
	boolean isReadOnly() {
		return this.readOnly;
	}

	@JsonProperty
	String getValue() {
		return this.value;
	}

	@JsonProperty
	String getPrompt() {
		return this.prompt;
	}

	@JsonProperty
	String getRegex() {
		return this.regex;
	}

	@JsonProperty
	boolean isTemplated() {
		return this.templated;
	}

	@JsonProperty
	boolean isRequired() {
		return this.required;
	}

	@JsonProperty
	boolean isMulti() {
		return this.multi;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (!(o instanceof HalFormsProperty))
			return false;
		HalFormsProperty that = (HalFormsProperty) o;
		return this.readOnly == that.readOnly && this.templated == that.templated && this.required == that.required
				&& this.multi == that.multi && Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value)
				&& Objects.equals(this.prompt, that.prompt) && Objects.equals(this.regex, that.regex);
	}

	@Override
	public int hashCode() {

		return Objects.hash(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated, this.required,
				this.multi);
	}

	public String toString() {

		return "HalFormsProperty(name=" + this.name + ", readOnly=" + this.readOnly + ", value=" + this.value + ", prompt="
				+ this.prompt + ", regex=" + this.regex + ", templated=" + this.templated + ", required=" + this.required
				+ ", multi=" + this.multi + ")";
	}
}
