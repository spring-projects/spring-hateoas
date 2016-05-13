package de.escalon.hypermedia.spring.halforms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.EmbeddedWrapper;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.escalon.hypermedia.spring.halforms.ValueSuggest.ValueSuggestType;

@JsonPropertyOrder({ "embeddeds", "links", "templates" })
public class HalFormsDocument extends ResourceSupport implements TemplatesSupport {

	private final List<Template> templates = new ArrayList<Template>();

	private final EmbeddedWrappers wrappers = new EmbeddedWrappers(true);
	private final List<EmbeddedWrapper> embeddedWrappers = new ArrayList<EmbeddedWrapper>();;

	public HalFormsDocument() {}

	public HalFormsDocument(Iterable<Link> links) {

		for (Link link : links) {
			if (link instanceof Template) {
				add((Template) link);
			} else {
				super.add(link);
			}
		}
	}

	@JsonCreator
	public HalFormsDocument(@JsonProperty("templates") List<Template> templates, @JsonProperty("links") List<Link> links,
			@JsonProperty("embeddeds") List<EmbeddedWrapper> wrappers) {
		this.templates.addAll(templates);
		this.embeddedWrappers.addAll(wrappers);
		add(links);
	}

	@Override
	@JsonProperty("_templates")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalFormsModule.HalTemplateListSerializer.class)
	@JsonDeserialize(using = Jackson2HalFormsModule.HalTemplateListDeserializer.class)
	public List<Template> getTemplates() {
		return templates;
	}

	@JsonProperty("_embedded")
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY,
			using = Jackson2HalFormsModule.HalEmbeddedResourcesSerializer.class)
	@JsonDeserialize(using = Jackson2HalModule.HalResourcesDeserializer.class)
	public Collection<EmbeddedWrapper> getEmbeddeds() {
		return embeddedWrappers;
	}

	@Override
	@JsonProperty("_links")
	@JsonSerialize(using = Jackson2HalFormsModule.HalFormsLinkLinkSerializer.class)
	@JsonDeserialize(using = Jackson2HalModule.HalLinkListDeserializer.class)
	public List<Link> getLinks() {
		return super.getLinks();
	}

	public void add(Template template) {
		// TODO Create a getter in template to obtain Link instance.
		String href = template.getHref();
		String rel = template.getRel();
		Link link = new Link(href, Template.DEFAULT_KEY.equals(rel) ? Link.REL_SELF : rel);

		// FIXME: only support one rel with multiple templates
		if (!alreadyExists(link)) {
			add(link);
		}

		templates.add(template);

		if (template.getProperties() == null) {
			return;
		}

		for (Property prop : template.getProperties()) {
			Suggest suggest = prop.getSuggest();
			if (suggest instanceof ValueSuggest<?>) {

				ValueSuggest<?> valueSuggest = (ValueSuggest<?>) suggest;
				if (valueSuggest.getType().equals(ValueSuggestType.EMBEDDED)) {
					embeddedWrappers.add(wrappers.wrap(valueSuggest.getValues()));
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
