/*
 * Copyright 2017-2024 the original author or authors.
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

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import org.springframework.http.MediaType;

/**
 * Simple Jackson serializers and deserializers.
 *
 * @author Oliver Gierke
 */
public class JacksonSerializers {

	/**
	 * Custom {@link ValueDeserializer} for Spring's {@link MediaType} using the {@link MediaType#parseMediaType(String)}
	 * method.
	 *
	 * @author Oliver Gierke
	 */
	public static class MediaTypeDeserializer extends StdDeserializer<MediaType> {

		public MediaTypeDeserializer() {
			super(MediaType.class);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
		public MediaType deserialize(JsonParser p, DeserializationContext ctxt) {
			return MediaType.parseMediaType(p.getString());
		}
	}
}
