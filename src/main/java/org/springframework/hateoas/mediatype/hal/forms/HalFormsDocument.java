/*
 * Copyright 2017-2020 the original author or authors.
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.PropertyUtils;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.HalLinkListSerializer;
import org.springframework.hateoas.mediatype.hal.forms.Jackson2HalFormsModule.HalFormsLinksDeserializer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
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
@JsonPropertyOrder({ "attributes", "entity", "entities", "embedded", "links", "templates", "metadata" })
final class HalFormsDocument<T> {

	@Nullable //
	@JsonInclude(Include.NON_EMPTY) //
	private final Map<String, Object> attributes;

	@Nullable //
	@JsonUnwrapped //
	@JsonInclude(Include.NON_NULL) //
	private final T entity;

	@Nullable //
	@JsonInclude(Include.NON_EMPTY) //
	@JsonIgnore //
	private final Collection<T> entities;

	@JsonProperty("_embedded") //
	@JsonInclude(Include.NON_EMPTY) //
	private final Map<HalLinkRelation, Object> embedded;

	@Nullable //
	@JsonProperty("page") //
	@JsonInclude(Include.NON_NULL) //
	private final PagedModel.PageMetadata pageMetadata;

	@JsonProperty("_links") //
	@JsonInclude(Include.NON_EMPTY) //
	@JsonSerialize(using = HalLinkListSerializer.class) //
	@JsonDeserialize(using = HalFormsLinksDeserializer.class) //
	private final Links links;

	@JsonProperty("_templates") //
	@JsonInclude(Include.NON_EMPTY) //
	private final Map<String, HalFormsTemplate> templates;

	HalFormsDocument(Map<String, Object> attributes, T entity, Collection<T> entities,
			Map<HalLinkRelation, Object> embedded, PageMetadata pageMetadata, Links links,
			Map<String, HalFormsTemplate> templates) {

		this.attributes = attributes;
		this.entity = entity;
		this.entities = entities;
		this.embedded = embedded;
		this.pageMetadata = pageMetadata;
		this.links = links;
		this.templates = templates;
	}

	private HalFormsDocument() {
		this(null, null, null, Collections.emptyMap(), null, Links.NONE, Collections.emptyMap());
	}

	/**
	 * Creates a new {@link HalFormsDocument} for the given resource support.
	 *
	 * @param model can be {@literal null}
	 * @return
	 */
	static HalFormsDocument<?> forRepresentationModel(RepresentationModel<?> model) {

		Map<String, Object> attributes = PropertyUtils.extractPropertyValues(model);
		attributes.remove("links");

		return new HalFormsDocument<>().withAttributes(attributes);
	}

	/**
	 * Creates a new {@link HalFormsDocument} for the given resource.
	 *
	 * @param resource can be {@literal null}.
	 * @return
	 */
	static <T> HalFormsDocument<T> forEntity(@Nullable T resource) {
		return new HalFormsDocument<T>().withEntity(resource);
	}

	/**
	 * returns a new {@link HalFormsDocument} for the given resources.
	 *
	 * @param entities must not be {@literal null}.
	 * @return
	 */
	static <T> HalFormsDocument<T> forEntities(Collection<T> entities) {

		Assert.notNull(entities, "Resources must not be null!");

		return new HalFormsDocument<T>().withEntities(entities);
	}

	/**
	 * Creates a new empty {@link HalFormsDocument}.
	 *
	 * @return
	 */
	static HalFormsDocument<?> empty() {
		return new HalFormsDocument<>();
	}

	/**
	 * Create a new {@link HalFormsDocument} by copying attributes and replacing the {@literal attributes}.
	 *
	 * @param attributes
	 * @return
	 */
	private HalFormsDocument<T> withAttributes(@Nullable Map<String, Object> attributes) {

		return this.attributes == attributes ? this
				: new HalFormsDocument<T>(attributes, this.entity, this.entities, this.embedded, this.pageMetadata, this.links,
						this.templates);
	}

	/**
	 * Create a new {@link HalFormsDocument} by copying attributes and replacing {@literal entity}.
	 * 
	 * @param entity
	 * @return
	 */
	private HalFormsDocument<T> withEntity(@Nullable T entity) {

		return this.entity == entity ? this
				: new HalFormsDocument<T>(this.attributes, entity, this.entities, this.embedded, this.pageMetadata, this.links,
						this.templates);
	}

	/**
	 * Create a new {@link HalFormsDocument} by copying attributes and replacing the {@literal entities}.
	 *
	 * @param entities
	 * @return
	 */
	private HalFormsDocument<T> withEntities(@Nullable Collection<T> entities) {

		return this.entities == entities ? this
				: new HalFormsDocument<T>(this.attributes, this.entity, entities, this.embedded, this.pageMetadata, this.links,
						this.templates);
	}

	/**
	 * Create a new {@link HalFormsDocument} by copying the attributes and adding a new embedded value.
	 *
	 * @param key must not be {@literal null} or empty.
	 * @param value must not be {@literal null}.
	 * @return
	 */
	HalFormsDocument<T> andEmbedded(HalLinkRelation key, Object value) {

		Assert.notNull(key, "Embedded key must not be null!");
		Assert.notNull(value, "Embedded value must not be null!");

		Map<HalLinkRelation, Object> embedded = new HashMap<>(this.embedded);
		embedded.put(key, value);

		return new HalFormsDocument<>(this.attributes, this.entity, this.entities, embedded, this.pageMetadata, this.links,
				this.templates);
	}

	/**
	 * Create a new {@link HalFormsDocument} by copying attributes and replacing all {@literal embedded}s.
	 *
	 * @param embedded
	 * @return
	 */
	HalFormsDocument<T> withEmbedded(Map<HalLinkRelation, Object> embedded) {

		return this.embedded == embedded ? this
				: new HalFormsDocument<T>(this.attributes, this.entity, this.entities, embedded, this.pageMetadata, this.links,
						this.templates);
	}

	/**
	 * Create a new {@link HalFormsDocument} by copying attributes and replacing the {@literal pageMetadata}.
	 *
	 * @param pageMetadata
	 * @return
	 */
	HalFormsDocument<T> withPageMetadata(@Nullable PageMetadata pageMetadata) {

		return this.pageMetadata == pageMetadata ? this
				: new HalFormsDocument<T>(this.attributes, this.entity, this.entities, this.embedded, pageMetadata, this.links,
						this.templates);
	}

	/**
	 * Create a new {@link HalFormsDocument} by copying the attributes and adding a new {@link Link}.
	 *
	 * @param link must not be {@literal null}.
	 * @return
	 */
	HalFormsDocument<T> andLink(Link link) {

		Assert.notNull(link, "Link must not be null!");

		return new HalFormsDocument<>(this.attributes, this.entity, this.entities, this.embedded, this.pageMetadata,
				this.links.and(link), this.templates);
	}

	/**
	 * Create a new {@link HalFormsDocument} by copying attributes and replacing the {@literal links}.
	 *
	 * @param links
	 * @return
	 */
	HalFormsDocument<T> withLinks(Links links) {

		return this.links == links ? this
				: new HalFormsDocument<T>(this.attributes, this.entity, this.entities, this.embedded, this.pageMetadata, links,
						this.templates);
	}

	/**
	 * Create a new {@link HalFormsDocument} by copying the attributes and adding a new {@link HalFormsTemplate}.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @param template must not be {@literal null}.
	 * @return
	 */
	HalFormsDocument<T> andTemplate(String name, HalFormsTemplate template) {

		Assert.hasText(name, "Template name must not be null or empty!");
		Assert.notNull(template, "Template must not be null!");

		Map<String, HalFormsTemplate> templates = new HashMap<>(this.templates);
		templates.put(name, template);

		return new HalFormsDocument<>(this.attributes, this.entity, this.entities, this.embedded, this.pageMetadata,
				this.links, templates);
	}

	/**
	 * Create a new {@link HalFormsDocument} by copying attributes and replacing the {@literal templates}.
	 *
	 * @param templates
	 * @return
	 */
	HalFormsDocument<T> withTemplates(Map<String, HalFormsTemplate> templates) {

		return this.templates == templates ? this
				: new HalFormsDocument<T>(this.attributes, this.entity, this.entities, this.embedded, this.pageMetadata,
						this.links, templates);
	}

	@Nullable
	@JsonAnyGetter
	Map<String, Object> getAttributes() {
		return this.attributes;
	}

	@Nullable
	T getEntity() {
		return this.entity;
	}

	@Nullable
	Collection<T> getEntities() {
		return this.entities;
	}

	Map<HalLinkRelation, Object> getEmbedded() {
		return this.embedded;
	}

	@Nullable
	PageMetadata getPageMetadata() {
		return this.pageMetadata;
	}

	Links getLinks() {
		return this.links;
	}

	Map<String, HalFormsTemplate> getTemplates() {
		return this.templates;
	}

	/**
	 * Returns the template with the given name.
	 *
	 * @param key must not be {@literal null}.
	 * @return
	 */
	@JsonIgnore
	HalFormsTemplate getTemplate(String key) {

		Assert.notNull(key, "Template key must not be null!");

		return this.templates.get(key);
	}

	/**
	 * Returns the default template of the document.
	 *
	 * @return
	 */
	@JsonIgnore
	HalFormsTemplate getDefaultTemplate() {
		return getTemplate(HalFormsTemplate.DEFAULT_KEY);
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (!(o instanceof HalFormsDocument))
			return false;
		HalFormsDocument<?> that = (HalFormsDocument<?>) o;
		return Objects.equals(this.attributes, that.attributes) && Objects.equals(this.entity, that.entity)
				&& Objects.equals(this.entities, that.entities) && Objects.equals(this.embedded, that.embedded)
				&& Objects.equals(this.pageMetadata, that.pageMetadata) && Objects.equals(this.links, that.links)
				&& Objects.equals(this.templates, that.templates);
	}

	@Override
	public int hashCode() {

		return Objects.hash(this.attributes, this.entity, this.entities, this.embedded, this.pageMetadata, this.links,
				this.templates);
	}

	public String toString() {

		return "HalFormsDocument(attributes=" + this.attributes + ", entity=" + this.entity + ", entities=" + this.entities
				+ ", embedded=" + this.embedded + ", pageMetadata=" + this.pageMetadata + ", links=" + this.links
				+ ", templates=" + this.templates + ")";
	}
}
