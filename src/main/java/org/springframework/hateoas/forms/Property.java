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
package org.springframework.hateoas.forms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Describe a parameter for the associated state transition in a HAL-FORMS document. A {@link Template} may contain a
 * list of {@link Property}
 * 
 * @see http://mamund.site44.com/misc/hal-forms/
 * 
 */
@JsonInclude(Include.NON_DEFAULT)
public class Property {

	private String name;

	private Boolean readOnly;

	private String value;

	private String prompt;

	private String regex;

	private boolean templated;

	private Suggest suggest;

	public Property() {
	}

	public Property(String name, Boolean readOnly, boolean templated, String value, String prompt, String regex,
			Suggest suggest) {
		this.name = name;
		this.readOnly = readOnly;
		this.templated = templated;
		this.value = value;
		this.prompt = prompt;
		this.regex = regex;
		this.suggest = suggest;
	}

	public String getName() {
		return name;
	}

	public Boolean isReadOnly() {
		return readOnly;
	}

	public String getValue() {
		return value;
	}

	public String getPrompt() {
		return prompt;
	}

	public String getRegex() {
		return regex;
	}

	public boolean isTemplated() {
		return templated;
	}

	public Suggest getSuggest() {
		return suggest;
	}
}
