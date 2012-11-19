package org.springframework.hateoas.hal.jackson1;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.ser.std.ContainerSerializerBase;

public class OptionalListSerializer extends ContainerSerializerBase<Object> {

	protected JsonSerializer<Object> _elementSerializer;
	protected BeanProperty _property;
	private JsonSerializer<Object> linkSer;

	public OptionalListSerializer() {
		super(List.class, false);
	}

	public OptionalListSerializer(Class<?> t, boolean dummy) {
		super(t, dummy);
	}

	@Override
	public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
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
					if (linkSer == null) {
						linkSer = provider.findValueSerializer(elem.getClass(), _property);
					}
					linkSer.serialize(elem, jgen, provider);
				}
			} while (value.hasNext());
		}
	}
}
