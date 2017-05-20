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

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.hateoas.hal.forms.HalFormsDeserializers.MediaTypesDeserializer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Value object for a HAL-FORMS template. Describes the available state transition details.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @see http://mamund.site44.com/misc/hal-forms/
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonPropertyOrder({ "title", "method", "contentType", "properties" })
@JsonIgnoreProperties({ "key" })
public class Template {

	public static final String DEFAULT_KEY = "default";

	private String key;
	private List<Property> properties = new ArrayList<Property>();

	private String title;
	private HttpMethod httpMethod;
	private List<MediaType> contentType;

	/**
	 * Configure a HAL-Forms template with a key value.
	 * @param key
	 */
	public Template(String key) {
		this.key = key;//(key != null) ? key : Template.DEFAULT_KEY;
	}

	/**
	 * A HAL-Forms template with no name is dubbed the <a href="https://rwcbook.github.io/hal-forms/#_the_code__templates_code_element">"default" template</a>.
	 *
	 * TODO: Assess spec's statement that 'For this release, the only valid value for key is "default"'
	 */
	public Template() {
		this(Template.DEFAULT_KEY);
	}

	public Property getProperty(String propertyName) {

		for (Property property : properties) {
			if (property.getName().equals(propertyName)) {
				return property;
			}
		}

		return null;
	}

	@JsonDeserialize(using = MediaTypesDeserializer.class)
	public String getContentType() {
		return StringUtils.collectionToCommaDelimitedString(contentType);
	}

	public String getMethod() {
		return this.httpMethod == null ? null : this.httpMethod.toString().toLowerCase();
	}

	public void setMethod(String method) {
		this.httpMethod = HttpMethod.valueOf(method.toUpperCase());
	}

	@JsonIgnore
	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod method) {
		this.httpMethod = method;
	}
}
