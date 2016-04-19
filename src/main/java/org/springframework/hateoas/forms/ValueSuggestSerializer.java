package org.springframework.hateoas.forms;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.forms.ValueSuggest.ValueSuggestType;

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

public class ValueSuggestSerializer extends JsonSerializer<ValueSuggest<?>> implements ContextualSerializer {

	private RelProvider relProvider;

	private ValueSuggestDirectSerializer directSerializer;

	public ValueSuggestSerializer(RelProvider relProvider, ValueSuggestDirectSerializer directSerializer) {
		this.relProvider = relProvider;
		this.directSerializer = directSerializer;
	}

	@Override
	public void serialize(ValueSuggest<?> value, JsonGenerator gen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		if (!value.getValues().iterator().hasNext()) {
			return;
		}

		if (value.getType().equals(ValueSuggestType.DIRECT)) {
			directSerializer.serialize(value, gen, provider);
		}
		else {
			gen.writeStartObject();

			gen.writeStringField("embedded",
					relProvider.getCollectionResourceRelFor(value.getValues().iterator().next().getClass()));

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

		return new ValueSuggestSerializer(relProvider, new ValueSuggestDirectSerializer(property));
	}

	public static class ValueSuggestDirectSerializer extends ContainerSerializer<Object>
			implements ContextualSerializer {

		private final BeanProperty property;

		private Map<Class<?>, JsonSerializer<Object>> serializers;

		private TextValueSerializer textValueSerializer;

		public ValueSuggestDirectSerializer() {
			this(null);
		}

		protected ValueSuggestDirectSerializer(BeanProperty property) {

			super(List.class, false);
			this.property = property;
			this.serializers = new HashMap<Class<?>, JsonSerializer<Object>>();

			this.textValueSerializer = new TextValueSerializer();
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

			Iterator<?> values = suggest.getValues().iterator();
			while (values.hasNext()) {
				Object elem = values.next();
				if (elem == null) {
					provider.defaultSerializeNull(jgen);
				}
				else {
					JsonSerializer<Object> serializer = getOrLookupSerializerFor(elem.getClass(), provider);
					if (EnumSerializer.class.isAssignableFrom(serializer.getClass())) {
						serializer.serialize(elem, jgen, provider);
					}
					else {
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

		private String valueField;

		private String textField;

		@Override
		public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException, JsonProcessingException {

		}

		public void setValueField(String valueField) {
			this.valueField = valueField;
		}

		public void setTextField(String textField) {
			this.textField = textField;
		}

	}

}
