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
package de.escalon.hypermedia.spring.halforms;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.hal.Jackson2HalModule.EmbeddedMapper;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import de.escalon.hypermedia.affordance.SuggestType;

class ValueSuggestSerializer extends JsonSerializer<ValueSuggest<?>> implements ContextualSerializer {

	private final RelProvider relProvider;

	private final EmbeddedMapper mapper;

	private final ValueSuggestDirectSerializer directSerializer;

	public ValueSuggestSerializer(final EmbeddedMapper mapper, final RelProvider relProvider,
			final ValueSuggestDirectSerializer directSerializer) {
		this.relProvider = relProvider;
		this.mapper = mapper;
		this.directSerializer = directSerializer;
	}

	@Override
	public void serialize(final ValueSuggest<?> value, final JsonGenerator gen, final SerializerProvider provider)
			throws IOException, JsonProcessingException {

		Iterator<?> iterator = value.getValues().iterator();
		if (!iterator.hasNext()) {
			return;
		}

		if (value.getType() == SuggestType.INTERNAL) {
			directSerializer.serialize(value, gen, provider);
		} else {
			gen.writeStartObject();

			Map<String, Object> curiedMap = mapper.map(value.getValues());

			if (value.getType() == SuggestType.EXTERNAL) {
				String embeddedRel;
				if (!curiedMap.isEmpty()) {
					embeddedRel = curiedMap.keySet().iterator().next();
				} else {
					embeddedRel = relProvider.getCollectionResourceRelFor(iterator.next().getClass());
				}
				gen.writeStringField("embedded", embeddedRel);
			} else {
				gen.writeStringField("href", (String) value.getValues().iterator().next());
			}

			if (value.getTextField() != null) {
				gen.writeStringField("prompt-field", value.getTextField());
			}
			if (value.getValueField() != null) {
				gen.writeStringField("value-field", value.getValueField());
			}
			gen.writeEndObject();
		}

	}

	@Override
	public JsonSerializer<?> createContextual(final SerializerProvider prov, final BeanProperty property)
			throws JsonMappingException {

		return new ValueSuggestSerializer(mapper, relProvider, new ValueSuggestDirectSerializer());
	}

	public static class ValueSuggestDirectSerializer extends ContainerSerializer<Object> implements ContextualSerializer {

		private static final long serialVersionUID = 1L;

		private final TextValueSerializer textValueSerializer;

		private final EnumValueSerializer enumValueSerializer;

		protected ValueSuggestDirectSerializer() {

			super(List.class, false);

			textValueSerializer = new TextValueSerializer();

			enumValueSerializer = new EnumValueSerializer();
		}

		@Override
		public void serialize(final Object value, final JsonGenerator jgen, final SerializerProvider provider)
				throws IOException, JsonGenerationException {
			ValueSuggest<?> suggest = (ValueSuggest<?>) value;

			Iterable<?> iterable = suggest.getValues();

			if (!iterable.iterator().hasNext()) {
				return;
			}
			jgen.writeStartArray();
			serializeContents(suggest, jgen, provider);
			jgen.writeEndArray();

		}

		private void serializeContents(final ValueSuggest<?> suggest, final JsonGenerator jgen,
				final SerializerProvider provider) throws IOException, JsonGenerationException {

			textValueSerializer.setTextField(suggest.getTextField());
			textValueSerializer.setValueField(suggest.getValueField());

			for (Object elem : suggest.getValues()) {
				if (elem == null) {
					provider.defaultSerializeNull(jgen);
				} else {
					if (elem.getClass().isEnum()) {
						enumValueSerializer.serialize(elem, jgen, provider);
					} else {
						textValueSerializer.serialize(elem, jgen, provider);
					}
				}
			}
		}

		@Override
		public JsonSerializer<?> createContextual(final SerializerProvider prov, final BeanProperty property)
				throws JsonMappingException {
			return new ValueSuggestDirectSerializer();
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
		public boolean hasSingleElement(final Object value) {
			return false;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(final TypeSerializer vts) {
			return null;
		}
	}

	private static class TextValueSerializer extends JsonSerializer<Object> {

		private static final String VALUE_FIELD_NAME = "value";

		private static final String PROMPT_FIELD_NAME = "prompt";

		private String valueField;

		private String textField;

		@Override
		public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider serializers)
				throws IOException, JsonProcessingException {

			BeanWrapper beanWrapper = new BeanWrapperImpl(value);

			gen.writeStartObject();
			if (valueField != null) {
				gen.writeObjectField(VALUE_FIELD_NAME, beanWrapper.getPropertyValue(valueField));
			}
			if (textField != null) {
				gen.writeObjectField(PROMPT_FIELD_NAME, beanWrapper.getPropertyValue(textField));
			}
			gen.writeEndObject();
		}

		public void setValueField(final String valueField) {
			this.valueField = valueField;
		}

		public void setTextField(final String textField) {
			this.textField = textField;
		}

	}

	private static class EnumValueSerializer extends JsonSerializer<Object> {

		private static final String VALUE_FIELD_NAME = "value";

		private static final String PROMPT_FIELD_NAME = "prompt";

		@Override
		public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider serializers)
				throws IOException, JsonProcessingException {

			gen.writeStartObject();
			gen.writeObjectField(VALUE_FIELD_NAME, (value));
			gen.writeObjectField(PROMPT_FIELD_NAME, ((Enum<?>) value).name());
			gen.writeEndObject();
		}

	}

}
