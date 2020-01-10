/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import java.io.IOException;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Simple Jackson serializers and deserializers.
 *
 * @author Oliver Gierke
 */
public class JacksonSerializers {

	/**
	 * Custom {@link JsonDeserializer} for Spring's {@link MediaType} using the {@link MediaType#parseMediaType(String)}
	 * method.
	 *
	 * @author Oliver Gierke
	 */
	public static class MediaTypeDeserializer extends StdDeserializer<MediaType> {

		private static final long serialVersionUID = 391537719262033410L;

		public MediaTypeDeserializer() {
			super(MediaType.class);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public MediaType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return MediaType.parseMediaType(p.getText());
		}
	}
}
