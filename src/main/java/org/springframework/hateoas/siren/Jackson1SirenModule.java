package org.springframework.hateoas.siren;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.ContextualSerializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ser.std.ContainerSerializerBase;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.hal.Jackson1HalModule;
import org.springframework.hateoas.siren.ResourceSupportMixin;

public class Jackson1SirenModule extends SimpleModule {
	
	/**
	 * Creates a new {@link Jackson1HalModule}.
	 */
	public Jackson1SirenModule() {

		super("json-siren-module", new Version(1, 0, 0, null));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(ResourceSupport.class, ResourceSupportMixin.class);
	}

	public class SirenLinkListSerializer extends ContainerSerializerBase<List<Link>> implements
	ContextualSerializer<List<Link>> {

		private final BeanProperty property;

		/**
		 * Creates a new {@link HalLinkListSerializer}.
		 */
		public SirenLinkListSerializer() {
			this(null);
		}

		public SirenLinkListSerializer(BeanProperty property) {
			super(List.class, false);
			this.property = property;
		}

		@Override
		public JsonSerializer<List<Link>> createContextual(SerializationConfig config, BeanProperty property)
				throws JsonMappingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void serialize(List<Link> value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
				JsonGenerationException {
			// TODO Auto-generated method stub
			
		}
	}

}
