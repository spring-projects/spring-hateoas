package org.springframework.hateoas.hal.jackson2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.Link;

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
import com.fasterxml.jackson.databind.type.TypeFactory;

public class HalLinkListSerializer extends ContainerSerializer<List<Link>> implements ContextualSerializer {

	private BeanProperty _property;

	public HalLinkListSerializer() {
		super(List.class, false);
	}

	public HalLinkListSerializer(BeanProperty property) {
		super(List.class, false);
		this._property = property;
	}

	@Override
	public void serialize(List<Link> value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonGenerationException {

		// sort links according to their relation
		Map<String, List<Link>> sortedLinks = new HashMap<String, List<Link>>();
		for (Link link : value) {
			if (sortedLinks.get(link.getRel()) == null) {
				sortedLinks.put(link.getRel(), new ArrayList<Link>());
			}
			sortedLinks.get(link.getRel()).add(link);
		}

		TypeFactory typeFactory = provider.getConfig().getTypeFactory();
		JavaType keyType = typeFactory.uncheckedSimpleType(String.class);
		JavaType valueType = typeFactory.constructCollectionType(ArrayList.class, Link.class);
		JavaType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);

		MapSerializer serializer = MapSerializer.construct(new String[] {}, mapType, true, null,
				provider.findKeySerializer(keyType, null), new OptionalListSerializer(_property));

		serializer.serialize(sortedLinks, jgen, provider);
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
			throws JsonMappingException {
		return new HalLinkListSerializer(property);
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
	public boolean isEmpty(List<Link> value) {
		return false;
	}

	@Override
	public boolean hasSingleElement(List<Link> value) {
		return false;
	}

	@Override
	protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
		// TODO Auto-generated method stub
		return null;
	}

}
