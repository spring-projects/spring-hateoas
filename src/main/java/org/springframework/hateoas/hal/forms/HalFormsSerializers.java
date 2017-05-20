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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.affordance.Suggestions;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Greg Turnquist
 */
public class HalFormsSerializers {

	/**
	 * Serialize {@link List} of {@link Template}s into HAL-Forms format.
	 */
	static class HalFormsTemplateListSerializer extends ContainerSerializer<List<Template>> implements ContextualSerializer {

		private static final long serialVersionUID = 1L;

		private static final String RELATION_MESSAGE_TEMPLATE = "_templates.%s.title";

		private final BeanProperty property;
		private final MessageSourceAccessor messageSource;

		public HalFormsTemplateListSerializer(BeanProperty property, MessageSourceAccessor messageSource) {

			super(TypeFactory.defaultInstance().constructType(List.class));
			this.property = property;
			this.messageSource = messageSource;
		}

		public HalFormsTemplateListSerializer(MessageSourceAccessor messageSource) {
			this(null, messageSource);
		}

		public HalFormsTemplateListSerializer() {
			this(null, null);
		}

		@Override
		public void serialize(List<Template> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			// sort templates according to their relation
			Map<String, List<Object>> sortedTemplates = new LinkedHashMap<String, List<Object>>();

			for (Template template : value) {
				if (sortedTemplates.get(template.getKey()) == null) {
					sortedTemplates.put(template.getKey(), new ArrayList<Object>());
				}
				sortedTemplates.get(template.getKey()).add(toHalFormsTemplate(template));
			}

			TypeFactory typeFactory = provider.getConfig().getTypeFactory();
			JavaType keyType = typeFactory.constructSimpleType(String.class, new JavaType[0]);
			JavaType valueType = typeFactory.constructCollectionType(ArrayList.class, Object.class);
			JavaType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);

			MapSerializer serializer = MapSerializer.construct(Collections.<String> emptySet(), mapType, true, null,
				provider.findKeySerializer(keyType, null), new Jackson2HalModule.OptionalListJackson2Serializer(this.property), null);

			if (!sortedTemplates.isEmpty()) {
				serializer.serialize(sortedTemplates, gen, provider);
			}
		}

		/**
		 * Wraps the given link into a HAL specific extension.
		 *
		 * @param template must not be {@literal null}.
		 * @return
		 */
		private HalFormsTemplate toHalFormsTemplate(Template template) {

			String key = template.getKey();
			String title = getTitle(key);

			if (title == null) {
				title = getTitle(key.contains(":") ? key.substring(key.indexOf(":") + 1) : key);
			}

			return new HalFormsTemplate(template, title);
		}

		/**
		 * Returns the title for the given local link relation resolved through the configured {@link MessageSourceAccessor}
		 *
		 * @param localRel must not be {@literal null} or empty.
		 * @return
		 */
		private String getTitle(String localRel) {

			Assert.hasText(localRel, "Local relation must not be null or empty!");

			try {
				return this.messageSource == null ? null
					: this.messageSource.getMessage(String.format(RELATION_MESSAGE_TEMPLATE, localRel));
			} catch (NoSuchMessageException o_O) {
				return null;
			}
		}

		/**
		 * Accessor for finding declared (static) element type for
		 * type this serializer is used for.
		 */
		@Override
		public JavaType getContentType() {
			return null;
		}

		/**
		 * Accessor for serializer used for serializing contents
		 * (List and array elements, Map values etc) of the
		 * container for which this serializer is used, if it is
		 * known statically.
		 * Note that for dynamic types this may return null; if so,
		 * caller has to instead use {@link #getContentType()} and
		 * {@link SerializerProvider#findValueSerializer}.
		 */
		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		/**
		 * Method called to determine if the given value (of type handled by
		 * this serializer) contains exactly one element.
		 * Note: although it might seem sensible to instead define something
		 * like "getElementCount()" method, this would not work well for
		 * containers that do not keep track of size (like linked lists may
		 * not).
		 *
		 * @param value
		 */
		@Override
		public boolean hasSingleElement(List<Template> value) {
			return value.size() == 1;
		}

		/**
		 * Method called to check whether given serializable value is
		 * considered "empty" value (for purposes of suppressing serialization
		 * of empty values).
		 * Default implementation will consider only null values to be empty.
		 * NOTE: replaces {@link #isEmpty(Object)}, which was deprecated in 2.5
		 *
		 * @param provider
		 * @param value
		 * @since 2.5
		 */
		@Override
		public boolean isEmpty(SerializerProvider provider, List<Template> value) {
			return value.isEmpty();
		}

		/**
		 * Method that needs to be implemented to allow construction of a new
		 * serializer object with given {@link TypeSerializer}, used when
		 * addition type information is to be embedded.
		 *
		 * @param vts
		 */
		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		/**
		 * Method called to see if a different (or differently configured) serializer
		 * is needed to serialize values of specified property.
		 * Note that instance that this method is called on is typically shared one and
		 * as a result method should <b>NOT</b> modify this instance but rather construct
		 * and return a new instance. This instance should only be returned as-is, in case
		 * it is already suitable for use.
		 *
		 * @param prov Serializer provider to use for accessing config, other serializers
		 * @param property Method or field that represents the property
		 * (and is used to access value to serialize).
		 * Should be available; but there may be cases where caller can not provide it and
		 * null is passed instead (in which case impls usually pass 'this' serializer as is)
		 * @return Serializer to use for serializing values of specified property;
		 * may be this instance or a new instance.
		 * @throws JsonMappingException
		 */
		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
			return new HalFormsTemplateListSerializer(property, null);
		}
	}

	/**
	 * Serialize {@link Suggestions.RemoteSuggestions} properties into HAL-Forms format.
	 */
	static class RemoteSuggestionsSerializer extends StdSerializer<Suggestions.RemoteSuggestions> {

		public RemoteSuggestionsSerializer() {
			super(TypeFactory.defaultInstance().constructType(Suggestions.RemoteSuggestions.class));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(Suggestions.RemoteSuggestions value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException {

			jgen.writeStartObject();
			jgen.writeStringField("href", value.getTemplate().toString());

			if (!value.getTemplate().getVariables().isEmpty()) {
				jgen.writeObjectField("templated", true);
			}

			writePromptAndValueFields(value, jgen);

			jgen.writeEndObject();
		}
	}

	/**
	 * Serialize {@link Suggestions.ExternalSuggestions} properties into HAL-Forms format.
	 */
	static class ExternalSuggestionSerializer extends StdSerializer<Suggestions.ExternalSuggestions> {

		public ExternalSuggestionSerializer() {
			super(TypeFactory.defaultInstance().constructType(Suggestions.ExternalSuggestions.class));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(Suggestions.ExternalSuggestions value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException {

			jgen.writeStartObject();
			jgen.writeStringField("embedded", value.getReference());

			writePromptAndValueFields(value, jgen);

			jgen.writeEndObject();
		}
	}

	static class HalEmbeddedResourcesSerializer extends ContainerSerializer<Collection<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = 1L;
		private final BeanProperty property;
		private final Jackson2HalModule.EmbeddedMapper embeddedMapper;

		public HalEmbeddedResourcesSerializer(Jackson2HalModule.EmbeddedMapper embeddedMapper) {
			this(null, embeddedMapper);
		}

		public HalEmbeddedResourcesSerializer(BeanProperty property, Jackson2HalModule.EmbeddedMapper embeddedMapper) {
			super(Collection.class, false);
			this.embeddedMapper = embeddedMapper;
			this.property = property;
		}

		@Override
		public void serialize(Collection<?> value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException {
			Map<String, Object> embeddeds = embeddedMapper.map(value);

			provider.findValueSerializer(Map.class, property).serialize(embeddeds, jgen, provider);
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new HalEmbeddedResourcesSerializer(property, embeddedMapper);
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		@Override
		public boolean hasSingleElement(Collection<?> value) {
			return value.size() == 1;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		@Override
		public boolean isEmpty(SerializerProvider provider, Collection<?> value) {
			return value.isEmpty();
		}
	}


	/**
	 * Write {@link Suggestions} into a property entry.
	 *
	 * @param suggestions
	 * @param generator
	 * @throws IOException
	 */
	private static void writePromptAndValueFields(Suggestions suggestions, JsonGenerator generator) throws IOException {

		if (suggestions.getPromptField() != null) {
			generator.writeObjectField("prompt-field", suggestions.getPromptField());
		}

		if (suggestions.getValueField() != null) {
			generator.writeObjectField("value-field", suggestions.getValueField());
		}
	}

}
