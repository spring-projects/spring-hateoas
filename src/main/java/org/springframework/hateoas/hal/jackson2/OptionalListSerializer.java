package org.springframework.hateoas.hal.jackson2;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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

public class OptionalListSerializer extends ContainerSerializer<Object> implements ContextualSerializer {

	protected BeanProperty _property;
	private JsonSerializer<Object> _linkSer;

	public OptionalListSerializer() {
		super(List.class, false);
	}

	public OptionalListSerializer(BeanProperty property) {
		this();
		_property = property;
	}

	@Override
	public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonGenerationException {

		List list = (List) value;

		if (list.size() == 1) {
			serializeContents(list.iterator(), jgen, provider);
			return;
		}
		jgen.writeStartArray();
		serializeContents(list.iterator(), jgen, provider);
		jgen.writeEndArray();
	}

	public void serializeContents(Iterator<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonGenerationException {
		if (value.hasNext()) {
			final TypeSerializer typeSer = null;
			do {
				Object elem = value.next();
				if (elem == null) {
					provider.defaultSerializeNull(jgen);
				} else {
					if (_linkSer == null) {
						_linkSer = provider.findValueSerializer(elem.getClass(), _property);
					}
					_linkSer.serialize(elem, jgen, provider);
				}
			} while (value.hasNext());
		}
	}

	@Override
	public JsonSerializer<?> getContentSerializer() {
		return _linkSer;
	}

	@Override
	public JavaType getContentType() {
		return null;
	}

	@Override
	public boolean hasSingleElement(Object arg0) {
		return false;
	}

	@Override
	public boolean isEmpty(Object arg0) {
		return false;
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
			throws JsonMappingException {
		return new OptionalListSerializer(property);
	}
}
