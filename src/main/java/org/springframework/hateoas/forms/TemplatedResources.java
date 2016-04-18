package org.springframework.hateoas.forms;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TemplatedResources<T> extends Resources<T> {

	@JsonProperty("_templates")
	@JsonInclude(Include.NON_EMPTY)
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
		super(content, links);
	}

	/**
	 * Creates a {@link TemplatedResources} instance with the given content and {@link Link}s.
	 * 
	 * @param content must not be {@literal null}.
	 * @param links the links to be added to the {@link Resources}.
	 */
	public TemplatedResources(Iterable<T> content, Iterable<Link> links) {

		super(content, links);
	}

	public void add(Template template) {
		// TODO Create a getter in template to obtain Link instance.
		String href = template.getHref();
		String rel = template.getRel();
		Link link = new Link(href, rel);
		
		//TODO Don't add default link to links. Override self? 

		super.add(link);

		this.templates.add(template);
	}
}
