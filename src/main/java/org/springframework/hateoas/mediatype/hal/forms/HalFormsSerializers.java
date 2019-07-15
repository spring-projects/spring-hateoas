/*
 * Copyright 2017-2019 the original author or authors.
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
import java.util.Map;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

/**
 * Collection of components needed to serialize a HAL-FORMS document.
 *
 * @author Greg Turnquist
 */
class HalFormsSerializers {

	static class HalFormsRepresentationModelSerializer extends ContainerSerializer<RepresentationModel<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = -4583146321934407153L;

		private final HalFormsTemplateBuilder builder;
		private final BeanProperty property;

		HalFormsRepresentationModelSerializer(HalFormsTemplateBuilder builder, @Nullable BeanProperty property) {

			super(RepresentationModel.class, false);

			this.builder = builder;
			this.property = property;
		}

		HalFormsRepresentationModelSerializer(HalFormsTemplateBuilder customizations) {
			this(customizations, null);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(RepresentationModel<?> value, JsonGenerator gen, SerializerProvider provider)
				throws IOException {

			HalFormsDocument<?> doc = HalFormsDocument.forRepresentationModel(value) //
					.withLinks(value.getLinks()) //
					.withTemplates(builder.findTemplates(value));

			provider.findValueSerializer(HalFormsDocument.class, property).serialize(doc, gen, provider);
		}

		@Override
		@Nullable
		@SuppressWarnings("null")
		public JavaType getContentType() {
			return null;
		}

		@Override
		@Nullable
		@SuppressWarnings("null")
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(RepresentationModel<?> resource) {
			return false;
		}

		@Override
		@Nullable
		@SuppressWarnings("null")
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
			return null;
		}

		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
			return new HalFormsRepresentationModelSerializer(builder, property);
		}
	}

	/**
	 * Serializer for {@link CollectionModel}.
	 */
	static class HalFormsEntityModelSerializer extends ContainerSerializer<EntityModel<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = -7912243216469101379L;

		private final HalFormsTemplateBuilder builder;
		private final BeanProperty property;

		HalFormsEntityModelSerializer(HalFormsTemplateBuilder builder, @Nullable BeanProperty property) {

			super(EntityModel.class, false);

			this.builder = builder;
			this.property = property;
		}

		HalFormsEntityModelSerializer(HalFormsTemplateBuilder builder) {
			this(builder, null);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(EntityModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			HalFormsDocument<?> doc = HalFormsDocument.forEntity(value.getContent()) //
					.withLinks(value.getLinks()) //
					.withTemplates(builder.findTemplates(value));

			provider.findValueSerializer(HalFormsDocument.class, property).serialize(doc, gen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentSerializer()
		 */
		@Override
		@Nullable
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(EntityModel<?> resource) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new HalFormsEntityModelSerializer(builder, property);
		}
	}

	/**
	 * Serializer for {@link CollectionModel}
	 */
	static class HalFormsCollectionModelSerializer extends ContainerSerializer<CollectionModel<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = -3601146866067500734L;

		private final BeanProperty property;
		private final Jackson2HalModule.EmbeddedMapper embeddedMapper;
		private final HalFormsTemplateBuilder customizations;

		HalFormsCollectionModelSerializer(HalFormsTemplateBuilder customizations, @Nullable BeanProperty property,
				Jackson2HalModule.EmbeddedMapper embeddedMapper) {

			super(CollectionModel.class, false);

			this.property = property;
			this.embeddedMapper = embeddedMapper;
			this.customizations = customizations;
		}

		HalFormsCollectionModelSerializer(HalFormsTemplateBuilder customizations,
				Jackson2HalModule.EmbeddedMapper embeddedMapper) {
			this(customizations, null, embeddedMapper);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(CollectionModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			Map<HalLinkRelation, Object> embeddeds = embeddedMapper.map(value);

			HalFormsDocument<?> doc;

			if (value instanceof PagedModel) {

				doc = HalFormsDocument.empty() //
						.withEmbedded(embeddeds) //
						.withPageMetadata(((PagedModel<?>) value).getMetadata()) //
						.withLinks(value.getLinks()) //
						.withTemplates(customizations.findTemplates(value));

			} else {

				doc = HalFormsDocument.empty() //
						.withEmbedded(embeddeds) //
						.withLinks(value.getLinks()) //
						.withTemplates(customizations.findTemplates(value));
			}

			provider.findValueSerializer(HalFormsDocument.class, property).serialize(doc, gen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentSerializer()
		 */
		@Override
		@Nullable
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(CollectionModel<?> resources) {
			return resources.getContent().size() == 1;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new HalFormsCollectionModelSerializer(customizations, property, embeddedMapper);
		}
	}
}
