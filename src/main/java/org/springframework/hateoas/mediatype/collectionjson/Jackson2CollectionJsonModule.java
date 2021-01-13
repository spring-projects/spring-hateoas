/*
stand bisher hauptsächlich darin, dass  * Copyright 2015-2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Links.MergeMode;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.JacksonHelper;
import org.springframework.hateoas.mediatype.PropertyUtils;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Jackson 2 module implementation to render {@link CollectionModel}, {@link EntityModel}, and
 * {@link RepresentationModel} instances in Collection+JSON compatible JSON.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class Jackson2CollectionJsonModule extends SimpleModule {

	private static final long serialVersionUID = -6540574644565592709L;

	public Jackson2CollectionJsonModule() {

		super("collection-json-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

		setMixInAnnotation(RepresentationModel.class, RepresentationModelMixin.class);
		setMixInAnnotation(EntityModel.class, EntityRepresentationModelMixin.class);
		setMixInAnnotation(CollectionModel.class, CollectionRepresentationModelMixin.class);
		setMixInAnnotation(PagedModel.class, PagedResourcesMixin.class);

		addSerializer(new CollectionJsonPagedResourcesSerializer());
		addSerializer(new CollectionJsonResourcesSerializer());
		addSerializer(new CollectionJsonResourceSerializer());
		addSerializer(new CollectionJsonResourceSupportSerializer());
		addSerializer(new CollectionJsonLinksSerializer());
		addDeserializer(Links.class, new CollectionJsonLinksDeserializer());
	}

	/**
	 * Custom {@link JsonSerializer} to render Link instances in JSON Collection compatible JSON.
	 *
	 * @author Alexander Baetz
	 * @author Oliver Gierke
	 */
	static class CollectionJsonLinksSerializer extends ContainerSerializer<Links> {

		private static final long serialVersionUID = 5959299073301391055L;

		CollectionJsonLinksSerializer() {
			super(Links.class);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(Links links, JsonGenerator jgen, SerializerProvider provider) throws IOException {

			JavaType type = provider.getTypeFactory().constructCollectionType(List.class, Link.class);

			provider.findValueSerializer(type) //
					.serialize(links.toList(), jgen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(com.fasterxml.jackson.databind.SerializerProvider, java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean isEmpty(SerializerProvider provider, Links value) {
			return value.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentType()
		 */
		@Override
		public JavaType getContentType() {
			return TypeFactory.defaultInstance().constructType(Link.class);
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
		public boolean hasSingleElement(Links value) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonResourceSupportSerializer extends ContainerSerializer<RepresentationModel<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = 6127711241993352699L;

		private final BeanProperty property;

		CollectionJsonResourceSupportSerializer() {
			this(null);
		}

		CollectionJsonResourceSupportSerializer(@Nullable BeanProperty property) {

			super(RepresentationModel.class, false);
			this.property = property;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(RepresentationModel<?> value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException {

			String href = value.getRequiredLink(IanaLinkRelations.SELF.value()).getHref();

			CollectionJson<Object> collectionJson = new CollectionJson<>() //
					.withVersion("1.0") //
					.withHref(href) //
					.withLinks(value.getLinks().without(IanaLinkRelations.SELF)) //
					.withQueries(findQueries(value)) //
					.withTemplate(findTemplate(value));

			CollectionJsonItem<Object> item = new CollectionJsonItem<>() //
					.withHref(href) //
					.withLinks(value.getLinks().without(IanaLinkRelations.SELF)) //
					.withRawData(value);

			if (!item.getData().isEmpty()) {
				collectionJson = collectionJson.withItems(item);
			}

			CollectionJsonDocument<?> doc = new CollectionJsonDocument<>(collectionJson);

			provider.findValueSerializer(CollectionJsonDocument.class, property).serialize(doc, jgen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new CollectionJsonResourceSupportSerializer(property);
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
		public boolean hasSingleElement(RepresentationModel<?> value) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonResourceSerializer extends ContainerSerializer<EntityModel<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = 2212535956767860364L;

		private final BeanProperty property;

		CollectionJsonResourceSerializer() {
			this(null);
		}

		CollectionJsonResourceSerializer(@Nullable BeanProperty property) {

			super(EntityModel.class, false);
			this.property = property;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(EntityModel<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

			String href = value.getRequiredLink(IanaLinkRelations.SELF).getHref();
			Links withoutSelfLink = value.getLinks().without(IanaLinkRelations.SELF);

			CollectionJson<Object> collectionJson = new CollectionJson<>() //
					.withVersion("1.0") //
					.withHref(href) //
					.withLinks(withoutSelfLink) //
					.withItems(new CollectionJsonItem<>() //
							.withHref(href) //
							.withLinks(withoutSelfLink) //
							.withRawData(value.getContent()))
					.withQueries(findQueries(value)) //
					.withTemplate(findTemplate(value));

			CollectionJsonDocument<?> doc = new CollectionJsonDocument<>(collectionJson);

			provider.findValueSerializer(CollectionJsonDocument.class, property) //
					.serialize(doc, jgen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new CollectionJsonResourceSerializer(property);
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
		public boolean hasSingleElement(EntityModel<?> value) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonResourcesSerializer extends ContainerSerializer<CollectionModel<?>> {

		private static final long serialVersionUID = -278986431091914402L;

		CollectionJsonResourcesSerializer() {
			super(CollectionModel.class, false);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(CollectionModel<?> value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException {

			CollectionJson<Object> collectionJson = new CollectionJson<>() //
					.withVersion("1.0") //
					.withHref(value.getRequiredLink(IanaLinkRelations.SELF).getHref()) //
					.withLinks(value.getLinks().without(IanaLinkRelations.SELF)) //
					.withItems(resourcesToCollectionJsonItems(value)) //
					.withQueries(findQueries(value)) //
					.withTemplate(findTemplate(value));

			CollectionJsonDocument<?> doc = new CollectionJsonDocument<>(collectionJson);

			provider.findValueSerializer(CollectionJsonDocument.class) //
					.serialize(doc, jgen, provider);
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
		 * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(com.fasterxml.jackson.databind.SerializerProvider, java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean isEmpty(SerializerProvider provider, CollectionModel<?> value) {
			return value.getContent().isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(CollectionModel<?> value) {
			return value.getContent().size() == 1;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonPagedResourcesSerializer extends ContainerSerializer<PagedModel<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = -6703190072925382402L;

		private final BeanProperty property;

		CollectionJsonPagedResourcesSerializer() {
			this(null);
		}

		CollectionJsonPagedResourcesSerializer(@Nullable BeanProperty property) {

			super(CollectionModel.class, false);
			this.property = property;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(PagedModel<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

			CollectionJson<?> collectionJson = new CollectionJson<>() //
					.withVersion("1.0") //
					.withHref(value.getRequiredLink(IanaLinkRelations.SELF).getHref()) //
					.withLinks(value.getLinks().without(IanaLinkRelations.SELF)) //
					.withItems(resourcesToCollectionJsonItems(value)) //
					.withQueries(findQueries(value)) //
					.withTemplate(findTemplate(value));

			CollectionJsonDocument<?> doc = new CollectionJsonDocument<>(collectionJson);

			provider.findValueSerializer(CollectionJsonDocument.class, property).serialize(doc, jgen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new CollectionJsonPagedResourcesSerializer(property);
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
		 * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean isEmpty(PagedModel<?> value) {
			return value.getContent().size() == 0;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(PagedModel<?> value) {
			return value.getContent().size() == 1;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonLinksDeserializer extends ContainerDeserializerBase<Links> {

		private static final long serialVersionUID = 4260899521055619665L;

		CollectionJsonLinksDeserializer() {
			super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Link.class));
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
		public Links deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {

			JavaType type = ctx.getTypeFactory().constructCollectionLikeType(List.class, Link.class);

			return Links.of(jp.getCodec().<List<Link>> readValue(jp, type));
		}
	}

	static class CollectionJsonResourceSupportDeserializer extends ContainerDeserializerBase<RepresentationModel<?>>
			implements ContextualDeserializer {

		private static final long serialVersionUID = 502737712634617739L;

		private final JavaType contentType;

		CollectionJsonResourceSupportDeserializer() {
			this(TypeFactory.defaultInstance().constructType(RepresentationModel.class));
		}

		CollectionJsonResourceSupportDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
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
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		public RepresentationModel<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			TypeFactory typeFactory = ctxt.getTypeFactory();

			JavaType rootType = typeFactory.constructSimpleType(Object.class, new JavaType[] {});
			JavaType wrappedType = typeFactory.constructParametricType(CollectionJsonDocument.class, rootType);

			CollectionJsonDocument<?> document = jp.getCodec().readValue(jp, wrappedType);
			CollectionJson<?> collection = document.getCollection();

			List<? extends CollectionJsonItem<?>> items = collection.getItems();
			Links links = collection.getLinks();

			CollectionJson<?> withOwnSelfLink = collection.withOwnSelfLink();

			if (!items.isEmpty()) {

				Links merged = items.stream() //
						.map(CollectionJsonItem::getLinks) //
						.reduce(links, //
								(left, right) -> left.merge(right), //
								(left, right) -> right);

				CollectionJsonItem<?> firstItem = items.get(0).withOwnSelfLink();
				RepresentationModel<?> resource = (RepresentationModel<?>) firstItem.toRawData(this.contentType);

				if (resource != null) {
					resource.add(firstItem.getLinks().merge(merged));
				}

				return resource;
			}

			CollectionJsonTemplate template = withOwnSelfLink.getTemplate();

			if (template != null) {

				Map<String, Object> properties = template.getData().stream()
						.collect(Collectors.toMap(CollectionJsonData::getName, CollectionJsonData::getValue));

				RepresentationModel<?> resourceSupport = (RepresentationModel<?>) PropertyUtils
						.createObjectFromProperties(this.contentType.getRawClass(), properties);

				return resourceSupport.add(withOwnSelfLink.getLinks());

			} else {
				return new RepresentationModel<>().add(withOwnSelfLink.getLinks());
			}
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.ContextualDeserializer#createContextual(com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonDeserializer<?> createContextual(DeserializationContext context, @Nullable BeanProperty property)
				throws JsonMappingException {

			JavaType type = property == null //
					? context.getContextualType() //
					: property.getType().getContentType();

			return new CollectionJsonResourceSupportDeserializer(type);
		}
	}

	static class CollectionJsonResourceDeserializer extends ContainerDeserializerBase<EntityModel<?>>
			implements ContextualDeserializer {

		private static final long serialVersionUID = -5911687423054932523L;

		private final JavaType contentType;

		CollectionJsonResourceDeserializer() {
			this(TypeFactory.defaultInstance().constructType(CollectionJson.class));
		}

		CollectionJsonResourceDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
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
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public EntityModel<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			JavaType rootType = JacksonHelper.findRootType(this.contentType);
			JavaType wrappedType = ctxt.getTypeFactory().constructParametricType(CollectionJsonDocument.class, rootType);

			CollectionJsonDocument<?> document = jp.getCodec().readValue(jp, wrappedType);

			CollectionJson<?> collection = document.getCollection();
			List<? extends CollectionJsonItem<?>> items = collection.getItems();
			Links links = collection.withOwnSelfLink().getLinks();
			CollectionJsonTemplate template = collection.getTemplate();

			if (items.isEmpty() && template != null) {

				Map<String, Object> properties = template.getData().stream()
						.collect(Collectors.toMap(CollectionJsonData::getName, CollectionJsonData::getValue));

				Object obj = PropertyUtils.createObjectFromProperties(rootType.getRawClass(), properties);

				return EntityModel.of(obj, links);

			} else {

				Links merged = items.stream() //
						.map(CollectionJsonItem::getLinks) //
						.reduce(links, //
								(left, right) -> left.merge(MergeMode.REPLACE_BY_REL, right), //
								(left, right) -> right);

				CollectionJsonItem<?> firstItem = items.get(0).withOwnSelfLink();

				return EntityModel.of(firstItem.toRawData(rootType),
						merged.merge(MergeMode.REPLACE_BY_REL, firstItem.getLinks()));
			}
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.ContextualDeserializer#createContextual(com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {

			return new CollectionJsonResourceDeserializer(
					property == null ? ctxt.getContextualType() : property.getType().getContentType());
		}
	}

	static abstract class CollectionJsonDeserializerBase<T extends CollectionModel<?>>
			extends ContainerDeserializerBase<T> implements ContextualDeserializer {

		private static final long serialVersionUID = 1007769482339850545L;

		private final JavaType contentType;
		private final BiFunction<List<Object>, Links, T> finalizer;
		private final Function<JavaType, CollectionJsonDeserializerBase<T>> creator;

		CollectionJsonDeserializerBase(BiFunction<List<Object>, Links, T> finalizer,
				Function<JavaType, CollectionJsonDeserializerBase<T>> creator) {
			this(TypeFactory.defaultInstance().constructType(CollectionJson.class), finalizer, creator);
		}

		private CollectionJsonDeserializerBase(JavaType contentType, BiFunction<List<Object>, Links, T> finalizer,
				Function<JavaType, CollectionJsonDeserializerBase<T>> creator) {

			super(contentType);

			this.contentType = contentType;
			this.finalizer = finalizer;
			this.creator = creator;
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
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property)
				throws JsonMappingException {

			JavaType contextualType = property == null //
					? ctxt.getContextualType() //
					: property.getType().getContentType();

			return creator.apply(contextualType);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public T deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {

			JavaType rootType = JacksonHelper.findRootType(contentType);
			JavaType wrappedType = ctxt.getTypeFactory().constructParametricType(CollectionJsonDocument.class, rootType);

			CollectionJsonDocument<?> document = parser.getCodec().readValue(parser, wrappedType);
			CollectionJson<?> collection = document.getCollection().withOwnSelfLink();

			Links links = collection.getLinks();

			if (!collection.hasItems() || !contentType.hasGenericTypes()) {
				return finalizer.apply(Collections.emptyList(), links);
			}

			boolean isResource = contentType.hasGenericTypes() && contentType.containedType(0).hasRawClass(EntityModel.class);

			return collection.getItems().stream() //
					.map(CollectionJsonItem::withOwnSelfLink) //
					.map(it -> isResource //
							? RepresentationModel.of(it.toRawData(rootType), it.getLinks()) //
							: it.toRawData(rootType)) //
					.collect(Collectors.collectingAndThen(Collectors.toList(), it -> finalizer.apply(it, links)));
		}
	}

	static class CollectionJsonResourcesDeserializer extends CollectionJsonDeserializerBase<CollectionModel<?>> {

		private static final long serialVersionUID = 6406522912020578141L;
		private static final BiFunction<List<Object>, Links, CollectionModel<?>> FINISHER = CollectionModel::of;
		private static final Function<JavaType, CollectionJsonDeserializerBase<CollectionModel<?>>> CONTEXTUAL_CREATOR = CollectionJsonResourcesDeserializer::new;

		CollectionJsonResourcesDeserializer() {
			super(FINISHER, CONTEXTUAL_CREATOR);
		}

		private CollectionJsonResourcesDeserializer(JavaType contentType) {
			super(contentType, FINISHER, CONTEXTUAL_CREATOR);
		}
	}

	static class CollectionJsonPagedResourcesDeserializer extends CollectionJsonDeserializerBase<PagedModel<?>> {

		private static final long serialVersionUID = -7465448422501330790L;
		private static final BiFunction<List<Object>, Links, PagedModel<?>> FINISHER = (content, links) -> PagedModel
				.of(content, null, links);
		private static final Function<JavaType, CollectionJsonDeserializerBase<PagedModel<?>>> CONTEXTUAL_CREATOR = CollectionJsonPagedResourcesDeserializer::new;

		CollectionJsonPagedResourcesDeserializer() {
			super(FINISHER, CONTEXTUAL_CREATOR);
		}

		private CollectionJsonPagedResourcesDeserializer(JavaType contentType) {
			super(contentType, FINISHER, CONTEXTUAL_CREATOR);
		}
	}

	private static List<CollectionJsonItem<Object>> resourcesToCollectionJsonItems(CollectionModel<?> resources) {

		return resources.getContent().stream().map(content -> {

			if (!EntityModel.class.isInstance(content)) {
				return new CollectionJsonItem<>().withRawData(content);
			}

			EntityModel<?> resource = (EntityModel<?>) content;

			return new CollectionJsonItem<>() //
					.withHref(resource.getRequiredLink(IanaLinkRelations.SELF).getHref())
					.withLinks(resource.getLinks().without(IanaLinkRelations.SELF)) //
					.withRawData(resource.getContent());

		}).collect(Collectors.toList());
	}

	/**
	 * Scan through the {@link Affordance}s and find any {@literal GET} calls against non-self URIs.
	 *
	 * @param resource
	 * @return
	 */
	private static List<CollectionJsonQuery> findQueries(RepresentationModel<?> resource) {

		if (!resource.hasLink(IanaLinkRelations.SELF)) {
			return Collections.emptyList();
		}

		Link selfLink = resource.getRequiredLink(IanaLinkRelations.SELF);

		return selfLink.getAffordances().stream() //
				.map(it -> it.getAffordanceModel(MediaTypes.COLLECTION_JSON)) //
				.peek(it -> Assert.notNull(it, "No Collection/JSON affordance model found but expected!"))
				.map(CollectionJsonAffordanceModel.class::cast) //
				.filter(it -> !it.hasHttpMethod(HttpMethod.GET)) //
				.filter(it -> !it.pointsToTargetOf(selfLink)) //
				.map(it -> new CollectionJsonQuery() //
						.withRel(it.getName()) //
						.withHref(it.getURI()) //
						.withData(it.getQueryProperties())) //
				.collect(Collectors.toList());
	}

	/**
	 * Scan through the {@link Affordance}s and
	 *
	 * @param resource
	 * @return
	 */
	@Nullable
	private static CollectionJsonTemplate findTemplate(RepresentationModel<?> resource) {

		if (!resource.hasLink(IanaLinkRelations.SELF)) {
			return null;
		}

		return resource.getRequiredLink(IanaLinkRelations.SELF).getAffordances() //
				.stream() //
				.map(it -> it.getAffordanceModel(MediaTypes.COLLECTION_JSON)) //
				.map(CollectionJsonAffordanceModel.class::cast) //
				.filter(it -> !it.hasHttpMethod(HttpMethod.GET)) //
				.map(it -> new CollectionJsonTemplate().withData(it.getInputProperties())) //
				.findFirst().orElse(null);
	}
}
