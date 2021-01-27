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
import java.util.Optional;

import org.springframework.hateoas.AffordanceModel.Named;
import org.springframework.hateoas.mediatype.html.HtmlInputType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
final class HalFormsProperty implements Named {

	private final String name, value, prompt, regex, placeholder;
	private final boolean templated, multi;
	private final @JsonInclude(Include.NON_DEFAULT) boolean readOnly, required;
	private final @Nullable Long min, max, minLength, maxLength;
	private final @Nullable HtmlInputType type;
	private final @Nullable HalFormsOptions options;

	HalFormsProperty() {

		this.name = null;
		this.readOnly = false;
		this.value = null;
		this.prompt = null;
		this.regex = null;
		this.templated = false;
		this.required = false;
		this.multi = false;
		this.placeholder = null;
		this.min = null;
		this.max = null;
		this.minLength = null;
		this.maxLength = null;
		this.type = null;
		this.options = null;
	}

	private HalFormsProperty(String name, boolean readOnly, String value, String prompt, String regex, boolean templated,
			boolean required, boolean multi, String placeholder, @Nullable Long min, @Nullable Long max,
			@Nullable Long minLength, @Nullable Long maxLength, @Nullable HtmlInputType type,
			@Nullable HalFormsOptions options) {

		Assert.notNull(name, "name must not be null!");

		this.name = name;
		this.readOnly = readOnly;
		this.value = value;
		this.prompt = StringUtils.hasText(prompt) ? prompt : null;
		this.regex = regex;
		this.templated = templated;
		this.required = required;
		this.multi = multi;
		this.placeholder = StringUtils.hasText(placeholder) ? placeholder : null;
		this.min = min;
		this.max = max;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.type = type;
		this.options = options;
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
						this.multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, this.type, this.options);
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
						this.multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, this.type, this.options);
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
						this.multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, this.type, this.options);
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
						this.multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, this.type, this.options);
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
						this.multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, this.type, this.options);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing the {@literal regex}.
	 *
	 * @param regex
	 * @return
	 */
	HalFormsProperty withRegex(Optional<String> regex) {
		return regex.map(it -> withRegex(it)).orElse(this);
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
						this.multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, this.type, this.options);
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
						this.multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, this.type, this.options);
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
						this.required, multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, this.type,
						this.options);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing {@literal placeholder}.
	 *
	 * @param placeholder
	 * @return
	 */
	HalFormsProperty withPlaceholder(String placeholder) {

		return this.placeholder == placeholder ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated,
						this.required, this.multi, placeholder, this.min, this.max, this.minLength, this.maxLength, this.type,
						this.options);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing {@literal min}.
	 *
	 * @param min can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	HalFormsProperty withMin(@Nullable Long min) {

		return Objects.equals(this.min, min) ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated,
						this.required, this.multi, this.placeholder, min, this.max, this.minLength, this.maxLength, this.type,
						this.options);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing {@literal max}.
	 *
	 * @param max can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	HalFormsProperty withMax(@Nullable Long max) {

		return Objects.equals(this.max, max) ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated,
						this.required, this.multi, this.placeholder, this.min, max, this.minLength, this.maxLength, this.type,
						this.options);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing {@literal minLength}.
	 *
	 * @param minLength can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	HalFormsProperty withMinLength(@Nullable Long minLength) {

		return Objects.equals(this.minLength, minLength) ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated,
						this.required, this.multi, this.placeholder, this.min, this.max, minLength, this.maxLength, this.type,
						this.options);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing {@literal maxLength}.
	 *
	 * @param maxLength can be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	HalFormsProperty withMaxLength(@Nullable Long maxLength) {

		return Objects.equals(this.maxLength, maxLength) ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated,
						this.required, this.multi, this.placeholder, this.min, this.max, this.minLength, maxLength, this.type,
						this.options);
	}

	/**
	 * Create a new {@link HalFormsProperty} by copying attributes and replacing {@literal type}.
	 *
	 * @param type can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	HalFormsProperty withType(@Nullable HtmlInputType type) {

		return Objects.equals(this.type, type) ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated,
						this.required, this.multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, type,
						this.options);
	}

	/**
	 * Creates a new {@link HalFormsProperty} by copying attributes and replacing {@literal options}.
	 *
	 * @param options can be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.3
	 */
	HalFormsProperty withOptions(@Nullable HalFormsOptions options) {

		return Objects.equals(this.options, options) ? this
				: new HalFormsProperty(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated,
						this.required, this.multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, this.type,
						options);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.AffordanceModel.Named#getName()
	 */
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

	@JsonIgnore
	boolean hasRegex() {
		return StringUtils.hasText(regex);
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

	@JsonProperty
	public String getPlaceholder() {
		return this.placeholder;
	}

	/**
	 * @return the min
	 */
	@Nullable
	@JsonProperty
	public Long getMin() {
		return min;
	}

	/**
	 * @return the max
	 */
	@Nullable
	@JsonProperty
	public Long getMax() {
		return max;
	}

	/**
	 * @return the minLength
	 */
	@Nullable
	@JsonProperty
	public Long getMinLength() {
		return minLength;
	}

	/**
	 * @return the maxLength
	 */
	@Nullable
	@JsonProperty
	Long getMaxLength() {
		return maxLength;
	}

	/**
	 * @return the type
	 */
	@Nullable
	@JsonProperty
	@JsonInclude(Include.NON_NULL)
	HtmlInputType getType() {
		return type;
	}

	/**
	 * @return the suggest
	 */
	@Nullable
	@JsonProperty
	HalFormsOptions getOptions() {
		return options;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object o) {

		if (this == o) {
			return true;
		}
		if (!(o instanceof HalFormsProperty)) {
			return false;
		}
		HalFormsProperty that = (HalFormsProperty) o;

		return this.readOnly == that.readOnly //
				&& this.templated == that.templated //
				&& this.required == that.required //
				&& this.multi == that.multi //
				&& Objects.equals(this.name, that.name) //
				&& Objects.equals(this.value, that.value) //
				&& Objects.equals(this.prompt, that.prompt) //
				&& Objects.equals(this.regex, that.regex) //
				&& Objects.equals(this.placeholder, that.placeholder) //
				&& Objects.equals(this.type, that.type) //
				&& Objects.equals(this.options, that.options);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return Objects.hash(this.name, this.readOnly, this.value, this.prompt, this.regex, this.templated, this.required,
				this.multi, this.placeholder, this.min, this.max, this.minLength, this.maxLength, this.type, this.options);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "HalFormsProperty(name=" + this.name //
				+ ", readOnly=" + this.readOnly //
				+ ", value=" + this.value //
				+ ", prompt=" + this.prompt //
				+ ", regex=" + this.regex //
				+ ", templated=" + this.templated //
				+ ", required=" + this.required //
				+ ", multi=" + this.multi //
				+ ", placeholder=" + this.placeholder //
				+ ", min=" + this.min //
				+ ", max=" + this.max //
				+ ", minLength=" + this.minLength //
				+ ", maxLength=" + this.maxLength //
				+ ", type=" + this.type //
				+ ", options=" + this.options //
				+ ")";
	}
}
