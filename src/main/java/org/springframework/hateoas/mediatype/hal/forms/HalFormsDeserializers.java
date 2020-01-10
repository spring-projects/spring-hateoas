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
package org.springframework.hateoas.mediatype.hal.forms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Collection of components needed to deserialize a HAL-FORMS document.
 *
 * @author Greg Turnquist
 */
class HalFormsDeserializers {

	static class HalFormsCollectionModelDeserializer extends ContainerDeserializerBase<List<Object>>
			implements ContextualDeserializer {

		private static final long serialVersionUID = -7325599536381465624L;

		private JavaType contentType;

		HalFormsCollectionModelDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		HalFormsCollectionModelDeserializer() {
			this(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Object.class));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public List<Object> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			List<Object> result = new ArrayList<>();
			JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(contentType);
			Object object;

			// links is an object, so we parse till we find its end.
			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

				if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
					throw new JsonParseException(jp, "Expected relation name");
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
		 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentType()
		 */
		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentDeserializer()
		 */
		@Override
		@Nullable
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.ContextualDeserializer#createContextual(com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {

			return new HalFormsCollectionModelDeserializer(
					property == null ? ctxt.getContextualType() : property.getType().getContentType());
		}
	}

	/**
	 * Deserialize a {@link MediaType} embedded inside a HAL-FORMS document.
	 */
	static class MediaTypesDeserializer extends ContainerDeserializerBase<List<MediaType>> {

		private static final long serialVersionUID = -7218376603548438390L;

		public MediaTypesDeserializer() {
			super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, MediaType.class));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentDeserializer()
		 */
		@Override
		@Nullable
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public List<MediaType> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return MediaType.parseMediaTypes(p.getText());
		}
	}
}
