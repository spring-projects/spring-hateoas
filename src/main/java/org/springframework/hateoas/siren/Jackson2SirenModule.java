package org.springframework.hateoas.siren;

import java.io.IOException;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

public class Jackson2SirenModule extends SimpleModule {

	public class SirenLinkListSerializer extends ContainerSerializer<List<Link>> implements ContextualSerializer {


		private final BeanProperty property;

		public SirenLinkListSerializer() {
			this(null);
		}

		public SirenLinkListSerializer(BeanProperty property) {

			super(List.class, false);
			this.property = property;
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public JavaType getContentType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public JsonSerializer<?> getContentSerializer() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isEmpty(List<Link> value) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean hasSingleElement(List<Link> value) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void serialize(List<Link> value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
				JsonGenerationException {
			// TODO Auto-generated method stub
			
		}
	}

	private static final long serialVersionUID = 8273710584662573042L;

	public Jackson2SirenModule() {
		super("json-hal-module", new Version(1, 0, 0, null));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(ResourceSupport.class, ResourceSupportMixin.class);
	}
	
	

}
