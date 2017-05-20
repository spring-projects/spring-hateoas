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

import static org.springframework.hateoas.affordance.Suggestions.*;
import static org.springframework.hateoas.hal.Jackson2HalModule.*;
import static org.springframework.hateoas.hal.forms.HalFormsDocument.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.affordance.Suggestions;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Greg Turnquist
 */
public class HalFormsDeserializers {

	/**
	 * Deserialize an entire <a href="https://rwcbook.github.io/hal-forms/">HAL-Forms</a> document.
	 */
	static class HalFormsDocumentDeserializer extends JsonDeserializer<HalFormsDocument> {

		private final HalLinkListDeserializer linkDeser = new HalLinkListDeserializer();
		private final HalFormsTemplateListDeserializer templateDeser = new HalFormsTemplateListDeserializer();

		@Override
		public HalFormsDocument deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException {

			HalFormsDocument.HalFormsDocumentBuilder halFormsDocumentBuilder = halFormsDocument();

			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

				if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
					throw new JsonParseException(jp, "Expected property ", jp.getCurrentLocation());
				}

				jp.nextToken();

				if ("_links".equals(jp.getCurrentName())) {
					halFormsDocumentBuilder.links(this.linkDeser.deserialize(jp, ctxt));
				} else if ("_templates".equals(jp.getCurrentName())) {
					halFormsDocumentBuilder.templates(this.templateDeser.deserialize(jp, ctxt));
				}
			}

			return halFormsDocumentBuilder.build();
		}
	}

	/**
	 * Deserialize an object of HAL-Forms {@link Template}s into a {@link List} of {@link Template}s.
	 */
	static class HalFormsTemplateListDeserializer extends ContainerDeserializerBase<List<Template>> {

		public HalFormsTemplateListDeserializer() {
			super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Template.class));
		}

		/**
		 * Accessor for declared type of contained value elements; either exact
		 * type, or one of its supertypes.
		 */
		@Override
		public JavaType getContentType() {
			return null;
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		/**
		 * Method that can be called to ask implementation to deserialize
		 * JSON content into the value type this serializer handles.
		 * Returned instance is to be constructed by method itself.
		 * <p>
		 * Pre-condition for this method is that the parser points to the
		 * first event that is part of value to deserializer (and which
		 * is never JSON 'null' literal, more on this below): for simple
		 * types it may be the only value; and for structured types the
		 * Object start marker or a FIELD_NAME.
		 * </p>
		 * The two possible input conditions for structured types result
		 * from polymorphism via fields. In the ordinary case, Jackson
		 * calls this method when it has encountered an OBJECT_START,
		 * and the method implementation must advance to the next token to
		 * see the first field name. If the application configures
		 * polymorphism via a field, then the object looks like the following.
		 * <pre>
		 *      {
		 *          "@class": "class name",
		 *          ...
		 *      }
		 *  </pre>
		 * Jackson consumes the two tokens (the <tt>@class</tt> field name
		 * and its value) in order to learn the class and select the deserializer.
		 * Thus, the stream is pointing to the FIELD_NAME for the first field
		 * after the @class. Thus, if you want your method to work correctly
		 * both with and without polymorphism, you must begin your method with:
		 * <pre>
		 *       if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
		 *         jp.nextToken();
		 *       }
		 *  </pre>
		 * This results in the stream pointing to the field name, so that
		 * the two conditions align.
		 * Post-condition is that the parser will point to the last
		 * event that is part of deserialized value (or in case deserialization
		 * fails, event that was not recognized or usable, which may be
		 * the same event as the one it pointed to upon call).
		 * Note that this method is never called for JSON null literal,
		 * and thus deserializers need (and should) not check for it.
		 *
		 * @param jp Parsed used for reading JSON content
		 * @param ctxt Context that can be used to access information about
		 * this deserialization activity.
		 * @return Deserialized value
		 */
		@Override
		public List<Template> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			List<Template> result = new ArrayList<Template>();
			String relation;
			Template template;

			// links is an object, so we parse till we find its end.
			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

				if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
					throw new JsonParseException(jp, "Expected relation name", jp.getCurrentLocation());
				}

				// save the relation in case the link does not contain it
				relation = jp.getText();

				if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
					while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
						template = jp.readValueAs(Template.class);
						template.setKey(relation);
						result.add(template);
					}
				} else {
					template = jp.readValueAs(Template.class);
					template.setKey(relation);
					result.add(template);
				}
			}

			return result;
		}
	}

	/**
	 * Deserialize all of the {@link Suggestions} properties.
	 */
	static class SuggestDeserializer extends JsonDeserializer<Suggestions> {

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		public Suggestions deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(ctxt.constructType(Object.class));

			List<Object> list = new ArrayList<Object>();

			String textField = null;
			String valueField = null;
			String embeddedRel = null;
			String href = null;

			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

				if ("values".equals(jp.getCurrentName())) {

					jp.nextToken();

					while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
						list.add(deser.deserialize(jp, ctxt));
					}

				} else if ("embedded".equals(jp.getCurrentName())) {
					embeddedRel = jp.getText();
				} else if ("href".equals(jp.getCurrentName())) {
					href = jp.getText();
				} else if ("prompt-field".equals(jp.getCurrentName())) {
					textField = jp.getText();
				} else if ("value-field".equals(jp.getCurrentName())) {
					valueField = jp.getText();
				}
			}

			if (valueField != null) {
				return values(list).withPromptField(textField).withValueField(valueField);
			} else if (href != null) {
				return remote(href).withPromptField(textField).withValueField(valueField);
			} else if (embeddedRel != null) {
				return external(embeddedRel).withPromptField(textField).withValueField(valueField);
			}

			return NONE;
		}
	}

	/**
	 * Deserialize a {@link MediaType} embedded inside a HAL-Forms document.
	 */
	static class MediaTypesDeserializer extends ContainerDeserializerBase<List<MediaType>> {

		private static final long serialVersionUID = -7218376603548438390L;

		public MediaTypesDeserializer() {
			super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, MediaType.class));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentType()
		 */
		@Override
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentDeserializer()
		 */
		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		public List<MediaType> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return MediaType.parseMediaTypes(p.getText());
		}
	}
}
