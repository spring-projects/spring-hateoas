package de.escalon.hypermedia.spring.halforms;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrapper;
import org.springframework.hateoas.core.EmbeddedWrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.escalon.hypermedia.spring.halforms.ValueSuggest.ValueSuggestType;

@JsonPropertyOrder({ "links", "templates" })
public class HalFormsDocument extends ResourceSupport implements TemplatesSupport {

	private List<Template> templates = new ArrayList<Template>();

	private EmbeddedWrappers wrappers;
	private List<EmbeddedWrapper> embeddedWrappers;

	public HalFormsDocument(Iterable<Link> links) {

		wrappers = new EmbeddedWrappers(true);
		embeddedWrappers = new ArrayList<EmbeddedWrapper>();

		for (Link link : links) {
			if (link instanceof Template) {
				add((Template) link);
			} else {
				super.add(link);
			}
		}
	}

	@JsonProperty("_templates")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalFormsModule.HalTemplateListSerializer.class)
	public List<Template> getTemplates() {
		return templates;
	}

	@JsonUnwrapped
	public Resources<EmbeddedWrapper> getEmbeddeds() {
		return new Resources<EmbeddedWrapper>(embeddedWrappers);
	}

	public void add(Template template) {
		// TODO Create a getter in template to obtain Link instance.
		String href = template.getHref();
		String rel = template.getRel();
		Link link = new Link(href, Template.DEFAULT_KEY.equals(rel) ? Link.REL_SELF : rel);

		if (!alreadyExists(link)) {
			add(link);
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
				} else {
					throw new IllegalStateException("Already exists an state with same 'rel' and different 'href' :" + prev);
				}
			}
		}
		return false;
	}

}
