/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Wither;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.hateoas.hal.forms.HalFormsDeserializers.MediaTypeDeserializer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Value object for a HAL-FORMS template. Describes the available state transition details.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @see https://rwcbook.github.io/hal-forms/#_the_code__templates_code_element
 */
@Data
@Setter(AccessLevel.NONE)
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@JsonAutoDetect(getterVisibility = Visibility.NON_PRIVATE)
@JsonIgnoreProperties({ "httpMethod" })
@JsonPropertyOrder({ "title", "method", "contentType", "properties" })
class HalFormsTemplate {

	private String title;
	private @Wither(AccessLevel.PRIVATE) HttpMethod httpMethod;
	private List<HalFormsProperty> properties;
	private MediaType contentType;

	private HalFormsTemplate() {
		this(null, null, Collections.emptyList(), null);
	}

	static HalFormsTemplate forMethod(HttpMethod httpMethod) {
		return new HalFormsTemplate().withHttpMethod(httpMethod);
	}

	@JsonDeserialize(using = MediaTypeDeserializer.class)
	void setContentType(MediaType mediaType) {
		this.contentType = mediaType;
	}

	String getMethod() {
		return this.httpMethod == null ? null : this.httpMethod.toString().toLowerCase();
	}

	void setMethod(String method) {
		this.httpMethod = HttpMethod.valueOf(method.toUpperCase());
	}
}
