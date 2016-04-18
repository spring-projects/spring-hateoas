package org.springframework.hateoas.forms;

import java.io.IOException;

import org.springframework.hateoas.Link;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LinkSuggestSerializer extends JsonSerializer<Link> {
	@Override
	public void serialize(Link value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeString(value.getHref());
	}

}
