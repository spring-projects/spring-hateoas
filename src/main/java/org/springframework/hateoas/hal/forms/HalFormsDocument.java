/*
 * Copyright 2017 the original author or authors.
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
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListDeserializer;
import org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Representation of a HAL-FORMS document.
 * 
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
@Value
@Wither
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@JsonPropertyOrder({ "resource", "resources", "embedded", "links", "templates", "metadata" })
public class HalFormsDocument<T> {

	@JsonUnwrapped //
	@JsonInclude(Include.NON_NULL) //
	@Wither(AccessLevel.PRIVATE) //
	private T resource;

	@JsonInclude(Include.NON_EMPTY) @JsonIgnore //
	@Wither(AccessLevel.PRIVATE) //
	private Collection<T> resources;

	@JsonProperty("_embedded") //
	@JsonInclude(Include.NON_EMPTY) //
	private Map<String, Object> embedded;

	@JsonProperty("page") //
	@JsonInclude(Include.NON_NULL) //
	private PagedResources.PageMetadata pageMetadata;

	@Singular //
	@JsonProperty("_links") //
	@JsonInclude(Include.NON_EMPTY) //
	@JsonSerialize(using = HalLinkListSerializer.class) //
	@JsonDeserialize(using = HalLinkListDeserializer.class) //
	private List<Link> links;

	@Singular //
	@JsonProperty("_templates") //
	@JsonInclude(Include.NON_EMPTY) //
	private Map<String, HalFormsTemplate> templates;

	HalFormsDocument() {
		this(null, null, Collections.emptyMap(), null, Collections.emptyList(), Collections.emptyMap());
	}

	/**
	 * Creates a new {@link HalFormsDocument} for the given resource.
	 * 
	 * @param resource can be {@literal null}.
	 * @return
	 */
	static <T> HalFormsDocument<T> forResource(T resource) {
		return new HalFormsDocument<T>().withResource(resource);
	}
}
