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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
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

		private final BeanProperty property;

		HalFormsRepresentationModelSerializer(@Nullable BeanProperty property) {

			super(RepresentationModel.class, false);
			this.property = property;
		}

		HalFormsRepresentationModelSerializer() {
			this(null);
		}

		@Override
		public void serialize(RepresentationModel<?> value, JsonGenerator gen, SerializerProvider provider)
				throws IOException {

			HalFormsDocument<?> doc = HalFormsDocument.forResourceSupport(value) //
					.withLinks(value.getLinks()) //
					.withTemplates(findTemplates(value));

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
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
			return new HalFormsRepresentationModelSerializer(property);
		}
	}

	/**
	 * Serializer for {@link CollectionModel}.
	 */
	static class HalFormsEntityModelSerializer extends ContainerSerializer<EntityModel<?>> implements ContextualSerializer {

		private static final long serialVersionUID = -7912243216469101379L;

		private final BeanProperty property;

		HalFormsEntityModelSerializer(@Nullable BeanProperty property) {

			super(EntityModel.class, false);
			this.property = property;
		}

		HalFormsEntityModelSerializer() {
			this(null);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(EntityModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			HalFormsDocument<?> doc = HalFormsDocument.forResource(value.getContent()) //
					.withLinks(value.getLinks()) //
					.withTemplates(findTemplates(value));

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
			return new HalFormsEntityModelSerializer(property);
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

		HalFormsCollectionModelSerializer(@Nullable BeanProperty property, Jackson2HalModule.EmbeddedMapper embeddedMapper) {

			super(CollectionModel.class, false);

			this.property = property;
			this.embeddedMapper = embeddedMapper;
		}

		HalFormsCollectionModelSerializer(Jackson2HalModule.EmbeddedMapper embeddedMapper) {
			this(null, embeddedMapper);
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
						.withTemplates(findTemplates(value));

			} else {

				doc = HalFormsDocument.empty() //
						.withEmbedded(embeddeds) //
						.withLinks(value.getLinks()) //
						.withTemplates(findTemplates(value));
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
			return new HalFormsCollectionModelSerializer(property, embeddedMapper);
		}
	}

	/**
	 * Extract template details from a {@link RepresentationModel}'s {@link Affordance}s.
	 *
	 * @param resource
	 * @return
	 */
	private static Map<String, HalFormsTemplate> findTemplates(RepresentationModel<?> resource) {

		if (!resource.hasLink(IanaLinkRelations.SELF)) {
			return Collections.emptyMap();
		}

		Map<String, HalFormsTemplate> templates = new HashMap<>();
		List<Affordance> affordances = resource.getLink(IanaLinkRelations.SELF).map(Link::getAffordances)
				.orElse(Collections.emptyList());

		affordances.stream() //
				.map(it -> it.getAffordanceModel(MediaTypes.HAL_FORMS_JSON)) //
				.map(HalFormsAffordanceModel.class::cast) //
				.filter(it -> !it.hasHttpMethod(HttpMethod.GET)) //
				.peek(it -> validate(resource, it)) //
				.forEach(it -> {

					HalFormsTemplate template = HalFormsTemplate.forMethod(it.getHttpMethod()) //
							.withProperties(it.getInputProperties());

					/*
					 * First template in HAL-FORMS is "default".
					 */
					templates.put(templates.isEmpty() ? "default" : it.getName(), template);
				});

		return templates;
	}

	/**
	 * Verify that the resource's self link and the affordance's URI have the same relative path.
	 *
	 * @param resource
	 * @param model
	 */
	private static void validate(RepresentationModel<?> resource, HalFormsAffordanceModel model) {

		String affordanceUri = model.getURI();
		String selfLinkUri = resource.getRequiredLink(IanaLinkRelations.SELF.value()).expand().getHref();

		if (!affordanceUri.equals(selfLinkUri)) {
			throw new IllegalStateException("Affordance's URI " + affordanceUri + " doesn't match self link " + selfLinkUri
					+ " as expected in HAL-FORMS");
		}
	}
}
