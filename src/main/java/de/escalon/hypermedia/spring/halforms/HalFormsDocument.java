package de.escalon.hypermedia.spring.halforms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.EmbeddedWrapper;
import org.springframework.hateoas.core.EmbeddedWrappers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.escalon.hypermedia.spring.halforms.ValueSuggest.ValueSuggestType;

@JsonPropertyOrder({ "embeddeds", "links", "templates" })
@JsonDeserialize(using = HalFormsDocumentDeserializer.class)
public class HalFormsDocument extends ResourceSupport implements TemplatesSupport {

	private final List<Template> templates = new ArrayList<Template>();

	private final EmbeddedWrappers wrappers = new EmbeddedWrappers(true);
	private final List<EmbeddedWrapper> embeddedWrappers = new ArrayList<EmbeddedWrapper>();
	List<Object> embedded = new ArrayList<Object>();

	public HalFormsDocument() {}

	public HalFormsDocument(Iterable<Link> links, List<Template> templates) {

		super.add(links);

		for (Template template : templates) {
			add(template);
		}

	}

	@Override
	@JsonProperty("_templates")
	@JsonInclude(Include.NON_EMPTY)
	@JsonSerialize(using = Jackson2HalFormsModule.HalTemplateListSerializer.class)
	public List<Template> getTemplates() {
		return templates;
	}

	@JsonProperty("_embedded")
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY,
			using = Jackson2HalFormsModule.HalEmbeddedResourcesSerializer.class)
	public Collection<EmbeddedWrapper> getEmbeddeds() {
		return embeddedWrappers;
	}

	@Override
	@JsonProperty("_links")
	@JsonSerialize(using = Jackson2HalFormsModule.HalFormsLinkLinkSerializer.class)
	public List<Link> getLinks() {
		return super.getLinks();
	}

	public void add(Template template) {
		templates.add(template);

		if (template.getProperties() == null) {
			return;
		}

		for (Property prop : template.getProperties()) {
			Suggest suggest = prop.getSuggest();
			if (suggest instanceof ValueSuggest<?>) {

				ValueSuggest<?> valueSuggest = (ValueSuggest<?>) suggest;
				if (valueSuggest.getType() == ValueSuggestType.EMBEDDED) {
					addIfNotDuplicated(valueSuggest.getValues());
				}
			}
		}
	}

	private void addIfNotDuplicated(Iterable<?> values) {
		/**
		 * Remove embedded duplicates
		 */
		List<Object> temp = new ArrayList<Object>();
		for (Object object : values) {
			if (!embedded.contains(object)) {
				temp.add(object);
				embedded.add(object);
			}
		}
		if (!temp.isEmpty()) {
			embeddedWrappers.add(wrappers.wrap(temp));
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

	@JsonIgnore
	public Template getTemplate() {
		return getTemplate(Template.DEFAULT_KEY);
	}

	@JsonIgnore
	public Template getTemplate(String key) {
		for (Template template : templates) {
			if (template.getKey().equals(key)) {
				return template;
			}
		}
		return null;
	}

}
