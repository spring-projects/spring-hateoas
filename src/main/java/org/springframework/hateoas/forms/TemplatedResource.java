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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrapper;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.hateoas.forms.ValueSuggest.ValueSuggestType;
import org.springframework.hateoas.hal.Jackson2HalFormsModule;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class TemplatedResource<T> extends Resource<T> implements TemplatesSupport {

	private final List<Template> templates = new ArrayList<Template>();

	private EmbeddedWrappers wrappers;

	private List<EmbeddedWrapper> embeddedWrappers;

	@JsonUnwrapped
	public Resources<EmbeddedWrapper> getEmbeddeds() {
		return new Resources<EmbeddedWrapper>(embeddedWrappers);
	}

	/**
	 * Creates a new {@link Resource} with the given content and {@link Link}s (optional).
	 * 
	 * @param content must not be {@literal null}.
	 * @param links the links to add to the {@link Resource}.
	 */
	public TemplatedResource(T content, Link... links) {
		this(content, Arrays.asList(links));
	}

	/**
	 * Creates a new {@link Resource} with the given content and {@link Link}s.
	 * 
	 * @param content must not be {@literal null}.
	 * @param links the links to add to the {@link Resource}.
	 */
	public TemplatedResource(T content, Iterable<Link> links) {

		super(content, new Link[0]);
		Assert.notNull(content, "Content must not be null!");
		Assert.isTrue(!(content instanceof Collection), "Content must not be a collection! Use Resources instead!");

		for (Link link : links) {
			if (link instanceof Template) {
				add((Template) link);
			}
			else {
				add(link);
			}
		}
	}

	@Override
	@JsonProperty("_templates")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalFormsModule.HalTemplateListSerializer.class)
	public List<Template> getTemplates() {
		return templates;
	}

	public void add(Template template) {
		// TODO Create a getter in template to obtain Link instance.
		String href = template.getHref();
		String rel = template.getRel();
		Link link = new Link(href, Template.DEFAULT_KEY.equals(rel) ? Link.REL_SELF : rel);

		if (!alreadyExists(link)) {
			super.add(link);
		}

		this.templates.add(template);

		if (template.getProperties() == null) {
			return;
		}

		for (Property prop : template.getProperties()) {
			Suggest suggest = prop.getSuggest();
			if (suggest instanceof ValueSuggest<?>) {

				ValueSuggest<?> valueSuggest = (ValueSuggest<?>) suggest;
				if (valueSuggest.getType().equals(ValueSuggestType.EMBEDDED)) {
					if (wrappers == null) {
						wrappers = new EmbeddedWrappers(true);
						embeddedWrappers = new ArrayList<EmbeddedWrapper>();
					}
					for (Object value : valueSuggest.getValues()) {
						embeddedWrappers.add(wrappers.wrap(value));
					}
				}
			}
		}
	}

	private boolean alreadyExists(Link link) {

		String rel = link.getRel();
		String href = link.getHref();

		Link self = getLink(Link.REL_SELF);
		if (self != null && self.getHref().equals(href)) {
			return true;
		}

		for (Link prev : getLinks()) {
			if (rel.equals(prev.getRel())) {
				if (href.equals(prev.getHref())) {
					return true;
				}
				else {
					throw new IllegalStateException(
							"Already exists an state with same 'rel' and different 'href' :" + prev);
				}
			}
		}
		return false;
	}

}
