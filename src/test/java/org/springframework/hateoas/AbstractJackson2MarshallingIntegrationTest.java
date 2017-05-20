package org.springframework.hateoas;

import java.io.StringWriter;
import java.io.Writer;

import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class to test objects against the Jackson 2.0 {@link ObjectMapper}.
 * 
 * @author Oliver Gierke
 * @author Jon Brisbin
 */
public abstract class AbstractJackson2MarshallingIntegrationTest {

	protected ObjectMapper mapper;

	@Before
	public void setUp() {
		this.mapper = new ObjectMapper();
	}

	protected String write(Object object) throws Exception {
		
		Writer writer = new StringWriter();
		this.mapper.writeValue(writer, object);
		return writer.toString();
	}

	protected <T> T read(String source, Class<T> targetType) throws Exception {
		return this.mapper.readValue(source, targetType);
	}
}
