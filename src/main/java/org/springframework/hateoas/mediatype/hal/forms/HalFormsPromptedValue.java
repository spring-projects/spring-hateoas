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

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.hateoas.mediatype.MessageSourceResolvableSerializer;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A value object to describe prompted values for HAL-FORMS {@code options}' {@code inline} attribute or responses of
 * resources pointed to by the {@code link} object.
 *
 * @author Oliver Drotbohm
 * @see https://rwcbook.github.io/hal-forms/#options-element
 */
public class HalFormsPromptedValue {

	private final Object prompt;
	private final Object value;

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
	 * @param promptKey
	 * @param value
	 * @return
	 */
	public static HalFormsPromptedValue ofI18ned(String promptKey, Object value) {
		return new HalFormsPromptedValue(new I18nizedPrompt(promptKey, value), value);
	}

	@JsonProperty
	public Object getPrompt() {
		return prompt;
	}

	@JsonIgnore
	@SuppressWarnings("null")
	public Object getPromptAsString() {

		return MessageSourceResolvable.class.isInstance(prompt)
				? MessageSourceResolvable.class.cast(prompt).getCodes()[0]
				: prompt.toString();
	}

	@JsonProperty
	public Object getValue() {
		return value;
	}

	@JsonSerialize(using = MessageSourceResolvableSerializer.class)
	private static class I18nizedPrompt extends DefaultMessageSourceResolvable {

		private static final long serialVersionUID = 7262804826421266153L;

		public I18nizedPrompt(String promptKey, Object value) {
			super(new String[] { promptKey }, new Object[] { value }, promptKey);
		}
	}
}
