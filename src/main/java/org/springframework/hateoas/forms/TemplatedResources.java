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
import java.util.Iterator;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.forms.ValueSuggest.ValueSuggestType;
import org.springframework.hateoas.hal.Jackson2HalFormsModule;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class TemplatedResources<T> extends Resources<T> {

	private List<Template> templates = new ArrayList<Template>();

	private List<Iterable<?>> embeddedContent = new ArrayList<Iterable<?>>();

	/**
	 * Creates an empty {@link TemplatedResources} instance.
	 */
	protected TemplatedResources() {
		this(new ArrayList<T>());
	}

	/**
	 * Creates a {@link TemplatedResources} instance with the given content and {@link Link}s (optional).
	 * 
	 * @param content must not be {@literal null}.
	 * @param links the links to be added to the {@link Resources}.
	 */
	public TemplatedResources(Iterable<T> content, Link... links) {
		this(content, Arrays.asList(links));
	}

	/**
	 * Creates a {@link TemplatedResources} instance with the given content and {@link Link}s.
	 * 
	 * @param content must not be {@literal null}.
	 * @param links the links to be added to the {@link Resources}.
	 */
	public TemplatedResources(Iterable<T> content, Iterable<Link> links) {

		this.embeddedContent.add(content);

		for (Link link : links) {
			if (link instanceof Template) {
				add((Template) link);
			}
			else {
				add(link);
			}
		}
	}

	@JsonProperty("_templates")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalFormsModule.HalTemplateListSerializer.class)
	public List<Template> getTemplates() {
		return templates;
	}

	@JsonProperty("_embedded")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalModule.HalResourcesSerializer.class)
	public List<Iterable<?>> getEmbeddedContent() {
		return embeddedContent;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Iterator iterator() {
		return embeddedContent.iterator();
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
			if (suggest != null && suggest instanceof ValueSuggest<?>) {

				ValueSuggest<?> valueSuggest = (ValueSuggest<?>) suggest;
				if (valueSuggest.getType().equals(ValueSuggestType.EMBEDDED)) {
					embeddedContent.add(valueSuggest.getValues());
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
