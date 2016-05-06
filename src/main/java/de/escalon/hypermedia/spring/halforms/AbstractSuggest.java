/*
 * Copyright 2016 the original author or authors.
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
package de.escalon.hypermedia.spring.halforms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @see Suggest
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties({ "type" })
class AbstractSuggest implements Suggest {

	private final String textField;

	private final String valueField;

	public AbstractSuggest(String textField, String valueField) {
		this.textField = textField;
		this.valueField = valueField;
	}

	@Override
	@JsonProperty("value-field")
	public String getValueField() {
		return valueField;
	}

	@Override
	@JsonProperty("prompt-field")
	public String getTextField() {
		return textField;
	}

}
