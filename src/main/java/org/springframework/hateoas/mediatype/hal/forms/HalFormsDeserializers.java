/*
 * Copyright 2017-2026 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.ContainerDeserializerBase;
import tools.jackson.databind.type.TypeFactory;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;

/**
 * Collection of components needed to deserialize a HAL-FORMS document.
 *
 * @author Greg Turnquist
 */
class HalFormsDeserializers {

	private static final TypeFactory TYPE_FACTORY = TypeFactory.createDefaultInstance();

	static class HalFormsCollectionModelDeserializer extends ContainerDeserializerBase<List<Object>> {

		private JavaType contentType;

		HalFormsCollectionModelDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		HalFormsCollectionModelDeserializer() {
			this(TYPE_FACTORY.constructCollectionLikeType(List.class, Object.class));
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public List<Object> deserialize(JsonParser jp, DeserializationContext ctxt) {

			List<Object> result = new ArrayList<>();
			ValueDeserializer<Object> deser = ctxt.findRootValueDeserializer(contentType);
			Object object;

			// links is an object, so we parse till we find its end.
			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

				if (!JsonToken.PROPERTY_NAME.equals(jp.currentToken())) {
					throw new StreamReadException(jp, "Expected relation name");
				}

				if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
					while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
						object = deser.deserialize(jp, ctxt);
						result.add(object);
					}
				} else {
					object = deser.deserialize(jp, ctxt);
					result.add(object);
				}
			}

			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.deser.std.ContainerDeserializerBase#getContentType()
		 */
		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.deser.std.ContainerDeserializerBase#getContentDeserializer()
		 */
		@Override
		@Nullable
		public ValueDeserializer<Object> getContentDeserializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#createContextual(tools.jackson.databind.DeserializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {

			return new HalFormsCollectionModelDeserializer(
					property == null ? ctxt.getContextualType() : property.getType().getContentType());
		}
	}

	/**
	 * Deserialize a {@link MediaType} embedded inside a HAL-FORMS document.
	 */
	static class MediaTypesDeserializer extends ContainerDeserializerBase<List<MediaType>> {

		public MediaTypesDeserializer() {
			super(TYPE_FACTORY.constructCollectionLikeType(List.class, MediaType.class));
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.deser.std.ContainerDeserializerBase#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.deser.std.ContainerDeserializerBase#getContentDeserializer()
		 */
		@Override
		@Nullable
		public ValueDeserializer<Object> getContentDeserializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public List<MediaType> deserialize(JsonParser p, DeserializationContext ctxt) {
			return MediaType.parseMediaTypes(p.getString());
		}
	}
}
