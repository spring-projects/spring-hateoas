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
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.hal.forms.HalFormsDeserializers.MediaTypesDeserializer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Value object for a HAL-FORMS template. Describes the available state transition details.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @see https://rwcbook.github.io/hal-forms/#_the_code__templates_code_element
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonPropertyOrder({ "title", "method", "contentType", "properties" })
@JsonIgnoreProperties({ "key" })
public class HalFormsTemplate {

	public static final String DEFAULT_KEY = "default";

	private @JsonIgnore String key;
	private List<HalFormsProperty> properties = new ArrayList<HalFormsProperty>();

	private String title;
	private @JsonIgnore HttpMethod httpMethod;
	private List<MediaType> contentType;

	/**
	 * Configure a HAL-FORMS template with a key value.
	 * @param key
	 */
	public HalFormsTemplate(String key) {
		this.key = key;
	}

	/**
	 * A HAL-FORMS template with no name is dubbed the <a href="https://rwcbook.github.io/hal-forms/#_the_code__templates_code_element">"default" template</a>.
	 */
	public HalFormsTemplate() {
		this(HalFormsTemplate.DEFAULT_KEY);
	}

	public String getContentType() {
		return StringUtils.collectionToCommaDelimitedString(contentType);
	}

	@JsonDeserialize(using = MediaTypesDeserializer.class)
	public void setContentType(List<MediaType> contentType) {
		this.contentType = contentType;
	}

	public String getMethod() {
		return this.httpMethod == null ? null : this.httpMethod.toString().toLowerCase();
	}

	public void setMethod(String method) {
		this.httpMethod = HttpMethod.valueOf(method.toUpperCase());
	}
}
