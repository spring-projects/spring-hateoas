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

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Value object for a HAL-FORMS template. Describes the available state transition details.
 * 
 * @see http://mamund.site44.com/misc/hal-forms/
 */
@JsonInclude(Include.NON_DEFAULT)
@JsonPropertyOrder({ "title", "method", "contentType", "properties" })
@JsonIgnoreProperties({ "href", "rel", "templated" })
public class Template extends Link {

	private static final long serialVersionUID = 2593020248152501268L;

	public static final String DEFAULT_KEY = "default";

	private final List<Property> properties = new ArrayList<Property>();

	private RequestMethod[] method;

	private String contentType;

	private String title;

	public Template() {}

	public Template(String href) {
		this(href, Template.DEFAULT_KEY);
	}

	public Template(String href, String key) {
		super(href, key);
	}

	public void setMethod(RequestMethod[] method) {
		this.method = method;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public String getTitle() {
		return title;
	}

	public Property getProperty(String propertyName) {
		for (Property property : properties) {
			if (property.getName().equals(propertyName)) {
				return property;
			}
		}
		return null;
	}

	public List<Property> getProperties() {
		return properties;
	}

	@JsonIgnore
	public RequestMethod[] getMethod() {
		return method;
	}

	@JsonProperty("method")
	public String getMethodStr() {
		if (method == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (RequestMethod rm : method) {
			sb.append(rm.toString()).append(',');
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

}
