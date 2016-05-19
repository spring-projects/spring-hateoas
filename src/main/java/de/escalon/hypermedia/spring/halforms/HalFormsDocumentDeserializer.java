package de.escalon.hypermedia.spring.halforms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.Link;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;

import de.escalon.hypermedia.affordance.SuggestType;
import de.escalon.hypermedia.spring.halforms.Jackson2HalFormsModule.HalTemplateListDeserializer;

public class HalFormsDocumentDeserializer extends JsonDeserializer<HalFormsDocument> {
	private HalEmbeddedResourcesDeserializer resourcesDeser = new HalEmbeddedResourcesDeserializer();
	private final HalTemplateListDeserializer templateDeser = new HalTemplateListDeserializer();
	private final HalLinkListDeserializer linkDeser = new HalLinkListDeserializer();

	@Override
	public HalFormsDocument deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {

		Map<String, List<Object>> embeddeds = null;
		List<Link> links = new ArrayList<Link>();
		List<Template> templates = new ArrayList<Template>();
		while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {
			if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
				throw new JsonParseException("Expected property ", jp.getCurrentLocation());
			}

			jp.nextToken();

			// FIXME: process _embedded first
			if ("_embedded".equals(jp.getCurrentName())) {
				embeddeds = resourcesDeser.deserialize(jp, ctxt);
			} else if ("_links".equals(jp.getCurrentName())) {
				links.addAll(linkDeser.deserialize(jp, ctxt));
			} else if ("_templates".equals(jp.getCurrentName())) {

				// FIXME: change SuggestDeserializer!
				templates.addAll(templateDeser.deserialize(jp, ctxt));
			}
		}

		return new HalFormsDocument(links, assignEmbeddeds(templates, embeddeds));
	}

	private List<Template> assignEmbeddeds(List<Template> templates, Map<String, List<Object>> embeddeds) {
		for (Template template : templates) {
			for (Property property : template.getProperties()) {
				if (property.getSuggest() == null) {
					continue;
				}

				SuggestMapper suggest = (SuggestMapper) property.getSuggest();
				if (suggest.getType() == SuggestType.EXTERNAL) {
					suggest.setValues(embeddeds.get(suggest.getEmbeddedRel()));
				}
			}
		}
		return templates;
	}

	private static class HalEmbeddedResourcesDeserializer extends ContainerDeserializerBase<Map<String, List<Object>>> {

		protected HalEmbeddedResourcesDeserializer() {
			super(Map.class);
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		@Override
		public Map<String, List<Object>> deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {

			Map<String, List<Object>> result = new HashMap<String, List<Object>>();

			Object object;
			String relation;

			JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(ctxt.constructType(Object.class));

			// links is an object, so we parse till we find its end.
			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {
				if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
					throw new JsonParseException("Expected relation name", jp.getCurrentLocation());
				}

				relation = jp.getText();
				if (!result.containsKey(relation)) {
					result.put(relation, new ArrayList<Object>());
				}
				List<Object> embeddeds = result.get(relation);

				if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
					while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
						object = deser.deserialize(jp, ctxt);
						embeddeds.add(object);
					}
				} else {
					object = deser.deserialize(jp, ctxt);
					embeddeds.add(object);
				}
			}

			return result;
		}

	}

	private static class HalLinkListDeserializer extends ContainerDeserializerBase<List<Link>> {

		protected HalLinkListDeserializer() {
			super(List.class);
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		@Override
		public List<Link> deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			List<Link> result = new ArrayList<Link>();
			String relation;
			Link link;

			// links is an object, so we parse till we find its end.
			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {
				if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
					throw new JsonParseException("Expected relation name", jp.getCurrentLocation());
				}

				// save the relation in case the link does not contain it
				relation = jp.getText();

				if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
					while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
						link = readValue(jp, relation);
						if (link != null) {
							result.add(new Link(link.getHref(), relation));
						}
					}
				} else {
					link = readValue(jp, relation);
					if (link != null) {
						result.add(new Link(link.getHref(), relation));
					}
				}
			}

			return result;
		}

		private Link readValue(JsonParser jp, String relation) throws IOException {
			Link link = null;
			// TODO: curies es un array!

			if ("curies".equals(relation)) {
				link = jp.readValueAs(ExtendedLink.class);
			} else {
				link = jp.readValueAs(ExtendedLink.class);
			}
			return link;
		}

	}

	public static class ExtendedLink extends Link {
		private String name;
		private boolean templated;

		public void setTemplated(boolean templated) {
			this.templated = templated;
		}

		public boolean isTemplated() {
			return templated;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}
