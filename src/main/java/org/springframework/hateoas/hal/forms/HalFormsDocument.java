/*
 * Copyright 2017 the original author or authors.
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
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListDeserializer;
import org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListSerializer;
import org.springframework.util.Assert;

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

	private HalFormsDocument() {
		this(null, null, Collections.emptyMap(), null, Collections.emptyList(), Collections.emptyMap());
	}

	/**
	 * Creates a new {@link HalFormsDocument} for the given resource.
	 * 
	 * @param resource can be {@literal null}.
	 * @return
	 */
	public static <T> HalFormsDocument<T> forResource(T resource) {
		return new HalFormsDocument<T>().withResource(resource);
	}

	/**
	 * returns a new {@link HalFormsDocument} for the given resources.
	 * 
	 * @param resources must not be {@literal null}.
	 * @return
	 */
	public static <T> HalFormsDocument<T> forResources(Collection<T> resources) {

		Assert.notNull(resources, "Resources must not be null!");

		return new HalFormsDocument<T>().withResources(resources);
	}

	/**
	 * Creates a new empty {@link HalFormsDocument}.
	 * 
	 * @return
	 */
	public static HalFormsDocument<?> empty() {
		return new HalFormsDocument<>();
	}

	/**
	 * Returns the default template of the document.
	 * 
	 * @return
	 */
	@JsonIgnore
	public HalFormsTemplate getDefaultTemplate() {
		return getTemplate(HalFormsTemplate.DEFAULT_KEY);
	}

	/**
	 * Returns the template with the given name.
	 * 
	 * @param key must not be {@literal null}.
	 * @return
	 */
	@JsonIgnore
	public HalFormsTemplate getTemplate(String key) {

		Assert.notNull(key, "Template key must not be null!");

		return this.templates.get(key);
	}

	/**
	 * Adds the given {@link Link} to the current document.
	 * 
	 * @param link must not be {@literal null}.
	 * @return
	 */
	public HalFormsDocument<T> andLink(Link link) {

		Assert.notNull(link, "Link must not be null!");

		List<Link> links = new ArrayList<>(this.links);
		links.add(link);

		return new HalFormsDocument<T>(resource, resources, embedded, pageMetadata, links, templates);
	}

	/**
	 * Adds the given {@link HalFormsTemplate} to the current document.
	 * 
	 * @param name must not be {@literal null} or empty.
	 * @param template must not be {@literal null}.
	 * @return
	 */
	public HalFormsDocument<T> andTemplate(String name, HalFormsTemplate template) {

		Assert.hasText(name, "Template name must not be null or empty!");
		Assert.notNull(template, "Template must not be null!");

		Map<String, HalFormsTemplate> templates = new HashMap<>(this.templates);
		templates.put(name, template);

		return new HalFormsDocument<T>(resource, resources, embedded, pageMetadata, links, templates);
	}

	/**
	 * Adds the given value as embedded one.
	 * 
	 * @param key must not be {@literal null} or empty.
	 * @param value must not be {@literal null}.
	 * @return
	 */
	public HalFormsDocument<T> andEmbedded(String key, Object value) {

		Assert.notNull(key, "Embedded key must not be null!");
		Assert.notNull(value, "Embedded value must not be null!");

		Map<String, Object> embedded = new HashMap<String, Object>(this.embedded);
		embedded.put(key, value);

		return new HalFormsDocument<T>(resource, resources, embedded, pageMetadata, links, templates);
	}
}
