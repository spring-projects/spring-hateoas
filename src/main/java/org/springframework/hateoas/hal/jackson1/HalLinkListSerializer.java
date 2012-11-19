package org.springframework.hateoas.hal.jackson1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.ser.std.ContainerSerializerBase;
import org.codehaus.jackson.map.ser.std.MapSerializer;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.springframework.hateoas.Link;

public class HalLinkListSerializer extends ContainerSerializerBase<List<Link>> {

	public HalLinkListSerializer() {
		super(List.class, false);
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

		MapSerializer serializer = MapSerializer.construct(new String[] {}, mapType, true, null, null,
				provider.findKeySerializer(keyType, null), new OptionalListSerializer(Link.class, false));

		serializer.serialize(sortedLinks, jgen, provider);

	}

	@Override
	public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
		// TODO Auto-generated method stub
		return null;
	}

}
