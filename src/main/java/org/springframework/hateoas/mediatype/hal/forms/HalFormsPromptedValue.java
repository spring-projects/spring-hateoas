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

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.hateoas.mediatype.MessageSourceResolvableSerializer;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A value object to describe prompted values for HAL-FORMS {@code options}' {@code inline} attribute or responses of
 * resources pointed to by the {@code link} object.
 *
 * @author Oliver Drotbohm
 * @see https://rwcbook.github.io/hal-forms/#options-element
 * @since 1.3
 */
public class HalFormsPromptedValue {

	private final Object prompt;
	private final Object value;

	/**
	 * Creates a new {@link HalFormsPromptedValue} for the given prompt and value.
	 *
	 * @param prompt must not be {@literal null}.
	 * @param value must not be {@literal null}.
	 */
	private HalFormsPromptedValue(Object prompt, Object value) {

		Assert.notNull(prompt, "Prompt must not be null!");
		Assert.notNull(value, "Value must not be null!");

		this.prompt = prompt;
		this.value = value;
	}

	/**
	 * Creates a new {@link HalFormsPromptedValue} with the given plain prompt and value.
	 *
	 * @param prompt must not be {@literal null} or empty.
	 * @param value
	 * @return
	 */
	public static HalFormsPromptedValue of(String prompt, Object value) {

		Assert.hasText(prompt, "Prompt must not be null or empty!");
		Assert.notNull(value, "Value must not be null!");

		return new HalFormsPromptedValue(prompt, value);
	}

	/**
	 * Creates a new {@link HalFormsPromptedValue} with the given prompt key to be used for i18nization and value.
	 *
	 * @param promptKey must not be {@literal null} or empty.
	 * @param value
	 * @return
	 */
	public static HalFormsPromptedValue ofI18ned(String promptKey, Object value) {

		Assert.hasText(promptKey, "Prompt key must not be null or empty!");
		Assert.notNull(value, "Value must not be null!");

		return new HalFormsPromptedValue(new I18nizedPrompt(promptKey, value), value);
	}

	/**
	 * Returns the prompt to be used. Can be a pre-resolved {@link String} or a value to be resolved into a String during
	 * serialization.
	 *
	 * @return will never be {@literal null}.
	 */
	@JsonProperty
	public Object getPrompt() {
		return prompt;
	}

	/**
	 * Returns the value.
	 *
	 * @return will never be {@literal null}.
	 */
	@JsonProperty
	public Object getValue() {
		return value;
	}

	/**
	 * Wrapper for a prompt to be i18ned via a {@link MessageSourceResolvableSerializer} during serialization.
	 *
	 * @author Oliver Drotbohm
	 */
	@JsonSerialize(using = MessageSourceResolvableSerializer.class)
	private static class I18nizedPrompt extends DefaultMessageSourceResolvable {

		private static final long serialVersionUID = 7262804826421266153L;

		I18nizedPrompt(String promptKey, Object value) {
			super(new String[] { promptKey }, new Object[] { value }, promptKey);
		}
	}
}
