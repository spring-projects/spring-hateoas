/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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

import org.junit.jupiter.api.BeforeEach;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.server.core.AnnotationLinkRelationProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class to test objects against the Jackson 2.0 {@link ObjectMapper}.
 *
 * @author Oliver Gierke
 * @author Jon Brisbin
 * @author Greg Turnquist
 */
@Deprecated
public abstract class AbstractJackson2MarshallingIntegrationTest {

	protected ObjectMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = MappingTestUtils.defaultObjectMapper();
	}

	protected ObjectMapper with(HalConfiguration configuration) {

		ObjectMapper copy = mapper.copy();

		copy.setHandlerInstantiator(new HalHandlerInstantiator(new AnnotationLinkRelationProvider(), CurieProvider.NONE,
				MessageResolver.DEFAULTS_ONLY, configuration));

		return copy;
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
