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
package org.springframework.hateoas.mediatype.hal.forms;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Wither;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.hateoas.mediatype.hal.forms.HalFormsDeserializers.MediaTypesDeserializer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
@JsonIgnoreProperties({ "httpMethod", "contentTypes" })
@JsonPropertyOrder({ "title", "method", "contentType", "properties" })
public class HalFormsTemplate {

	public static final String DEFAULT_KEY = "default";

	private @Getter(onMethod = @__(@JsonInclude(Include.NON_EMPTY))) String title;
	private @Wither(AccessLevel.PRIVATE) HttpMethod httpMethod;
	private List<HalFormsProperty> properties;
	private List<MediaType> contentTypes;

	@SuppressWarnings("null")
	private HalFormsTemplate() {
		this(null, null, Collections.emptyList(), Collections.emptyList());
	}

	public static HalFormsTemplate forMethod(HttpMethod httpMethod) {
		return new HalFormsTemplate().withHttpMethod(httpMethod);
	}

	/**
	 * Returns a new {@link HalFormsTemplate} with the given {@link HalFormsProperty} added.
	 *
	 * @param property must not be {@literal null}.
	 * @return
	 */
	public HalFormsTemplate andProperty(HalFormsProperty property) {

		Assert.notNull(property, "Property must not be null!");

		List<HalFormsProperty> properties = new ArrayList<>(this.properties);
		properties.add(property);

		return new HalFormsTemplate(title, httpMethod, properties, contentTypes);
	}

	/**
	 * Returns a new {@link HalFormsTemplate} with the given {@link MediaType} added as content type.
	 *
	 * @param mediaType must not be {@literal null}.
	 * @return
	 */
	public HalFormsTemplate andContentType(MediaType mediaType) {

		Assert.notNull(mediaType, "Media type must not be null!");

		List<MediaType> contentTypes = new ArrayList<>(this.contentTypes);
		contentTypes.add(mediaType);

		return new HalFormsTemplate(title, httpMethod, properties, contentTypes);
	}

	// Jackson helper methods to create the right representation format

	@JsonInclude(Include.NON_EMPTY)
	String getContentType() {
		return StringUtils.collectionToDelimitedString(contentTypes, ", ");
	}

	@JsonDeserialize(using = MediaTypesDeserializer.class)
	void setContentType(List<MediaType> mediaTypes) {
		this.contentTypes = mediaTypes;
	}

	@Nullable
	String getMethod() {
		return this.httpMethod == null ? null : this.httpMethod.toString().toLowerCase();
	}

	void setMethod(String method) {
		this.httpMethod = HttpMethod.valueOf(method.toUpperCase());
	}

	Optional<HalFormsProperty> getPropertyByName(String name) {
		return properties.stream().filter(it -> it.getName().equals(name)).findFirst();
	}
}
