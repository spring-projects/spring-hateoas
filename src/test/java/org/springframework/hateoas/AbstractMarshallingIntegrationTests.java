/*
 * Copyright 2012 the original author or authors.
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
package org.springframework.hateoas;

import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;

/**
 * Base class to eas integration tests for {@link ObjectMapper} marshalling.
 * 
 * @author Oliver Gierke
 */
public abstract class AbstractMarshallingIntegrationTests {

	ObjectMapper mapper;

	@Before
	public void setUp() {
		mapper = new ObjectMapper();
	}

	protected String write(Object object) throws Exception {

		Writer writer = new StringWriter();
		mapper.writeValue(writer, object);
		return writer.toString();
	}

	protected <T> T read(String source, Class<T> targetType) throws Exception {
		return mapper.readValue(source, targetType);
	}
}
