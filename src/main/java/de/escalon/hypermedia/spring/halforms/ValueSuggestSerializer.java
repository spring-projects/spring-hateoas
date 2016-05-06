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
import java.util.HashMap;
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
import com.fasterxml.jackson.databind.ser.std.EnumSerializer;

import de.escalon.hypermedia.spring.halforms.ValueSuggest.ValueSuggestType;

class ValueSuggestSerializer extends JsonSerializer<ValueSuggest<?>> implements ContextualSerializer {

	private final RelProvider relProvider;

	private final EmbeddedMapper mapper;

	private final ValueSuggestDirectSerializer directSerializer;

	public ValueSuggestSerializer(EmbeddedMapper mapper, RelProvider relProvider,
			ValueSuggestDirectSerializer directSerializer) {
		this.relProvider = relProvider;
		this.mapper = mapper;
		this.directSerializer = directSerializer;
	}

	@Override
	public void serialize(ValueSuggest<?> value, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		Iterator<?> iterator = value.getValues().iterator();
		if (!iterator.hasNext()) {
			return;
		}

		if (value.getType() == ValueSuggestType.DIRECT) {
			directSerializer.serialize(value, gen, provider);
		} else {
			gen.writeStartObject();

			Map<String, Object> curiedMap = mapper.map(value.getValues());

			if (value.getType() == ValueSuggestType.EMBEDDED) {
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

			// FIXME: debería delegar en el serializador por defecto??
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
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
			throws JsonMappingException {

		return new ValueSuggestSerializer(mapper, relProvider, new ValueSuggestDirectSerializer(property));
	}

	public static class ValueSuggestDirectSerializer extends ContainerSerializer<Object> implements ContextualSerializer {

		private static final long serialVersionUID = 1L;

		private final BeanProperty property;

		private final Map<Class<?>, JsonSerializer<Object>> serializers;

		private final TextValueSerializer textValueSerializer;

		public ValueSuggestDirectSerializer() {
			this(null);
		}

		protected ValueSuggestDirectSerializer(BeanProperty property) {

			super(List.class, false);
			this.property = property;
			serializers = new HashMap<Class<?>, JsonSerializer<Object>>();

			textValueSerializer = new TextValueSerializer();
		}

		@Override
		public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider)
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

		private void serializeContents(ValueSuggest<?> suggest, JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonGenerationException {

			textValueSerializer.setTextField(suggest.getTextField());
			textValueSerializer.setValueField(suggest.getValueField());

			for (Object elem : suggest.getValues()) {
				if (elem == null) {
					provider.defaultSerializeNull(jgen);
				} else {
					JsonSerializer<Object> serializer = getOrLookupSerializerFor(elem.getClass(), provider);
					if (EnumSerializer.class.isAssignableFrom(serializer.getClass())) {
						serializer.serialize(elem, jgen, provider);
					} else {
						textValueSerializer.serialize(elem, jgen, provider);
					}
				}
			}
		}

		private JsonSerializer<Object> getOrLookupSerializerFor(Class<?> type, SerializerProvider provider)
				throws JsonMappingException {

			JsonSerializer<Object> serializer = serializers.get(type);

			if (serializer == null) {
				serializer = provider.findValueSerializer(type, property);
				serializers.put(type, serializer);
			}

			return serializer;
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new ValueSuggestDirectSerializer(property);
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
		public boolean hasSingleElement(Object value) {
			return false;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	public static class TextValueSerializer extends JsonSerializer<Object> {

		private static final String VALUE_FIELD_NAME = "value";

		private static final String PROMPT_FIELD_NAME = "prompt";

		private String valueField;

		private String textField;

		@Override
		public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
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

		public void setValueField(String valueField) {
			this.valueField = valueField;
		}

		public void setTextField(String textField) {
			this.textField = textField;
		}

	}

}
