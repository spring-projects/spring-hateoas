package org.springframework.hateoas.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.Jackson2HalFormsModule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class TemplatedResources<T> extends Resources<T> {

	@JsonProperty("_templates")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalFormsModule.HalTemplateListSerializer.class)
	private List<Template> templates = new ArrayList<Template>();

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
		super(content);
		for (Link link : links) {
			if (link instanceof Template) {
				add((Template) link);
			}
			else {
				add(link);
			}
		}
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
