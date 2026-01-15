/*
 * Copyright 2015-2026 the original author or authors.
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

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.std.ContainerDeserializerBase;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdContainerSerializer;
import tools.jackson.databind.type.TypeFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
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
import org.springframework.util.Assert;

/**
 * Jackson 2 module implementation to render {@link CollectionModel}, {@link EntityModel}, and
 * {@link RepresentationModel} instances in Collection+JSON compatible JSON.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class CollectionJsonJacksonModule extends SimpleModule {

	private static final long serialVersionUID = -6540574644565592709L;
	private static final TypeFactory TYPE_FACTORY = TypeFactory.createDefaultInstance();

	public CollectionJsonJacksonModule() {

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
	 * Custom {@link ValueSerializer} to render Link instances in JSON Collection compatible JSON.
	 *
	 * @author Alexander Baetz
	 * @author Oliver Gierke
	 */
	static class CollectionJsonLinksSerializer extends StdContainerSerializer<Links> {

		CollectionJsonLinksSerializer() {
			super(Links.class);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(Links links, JsonGenerator jgen, SerializationContext provider) {

			JavaType type = provider.getTypeFactory().constructCollectionType(List.class, Link.class);

			provider.findValueSerializer(type) //
					.serialize(links.toList(), jgen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#isEmpty(tools.jackson.databind.SerializationContext, java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean isEmpty(SerializationContext provider, Links value) {
			return value.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentType()
		 */
		@Override
		public JavaType getContentType() {
			return TYPE_FACTORY.constructType(Link.class);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentSerializer()
		 */
		@Override
		@Nullable
		public ValueSerializer<?> getContentSerializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(Links value) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected StdContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonResourceSupportSerializer extends StdContainerSerializer<RepresentationModel<?>> {

		private final @Nullable BeanProperty property;

		CollectionJsonResourceSupportSerializer() {
			this(null);
		}

		CollectionJsonResourceSupportSerializer(@Nullable BeanProperty property) {

			super(RepresentationModel.class);
			this.property = property;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(RepresentationModel<?> value, JsonGenerator jgen, SerializationContext context) {

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

			context.findPrimaryPropertySerializer(CollectionJsonDocument.class, property)
					.serialize(doc, jgen, context);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializationContext, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) {
			return new CollectionJsonResourceSupportSerializer(property);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentSerializer()
		 */
		@Override
		@Nullable
		public ValueSerializer<?> getContentSerializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#isEmpty(tools.jackson.databind.SerializationContext, java.lang.Object)
		 */
		@Override
		public boolean isEmpty(SerializationContext prov, RepresentationModel<?> value) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(RepresentationModel<?> value) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected StdContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonResourceSerializer extends StdContainerSerializer<EntityModel<?>> {

		private final @Nullable BeanProperty property;

		CollectionJsonResourceSerializer() {
			this(null);
		}

		CollectionJsonResourceSerializer(@Nullable BeanProperty property) {

			super(EntityModel.class);
			this.property = property;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
		 */
		@Override
		public void serialize(EntityModel<?> value, JsonGenerator gen, SerializationContext provider)
				throws JacksonException {

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

			provider.findPrimaryPropertySerializer(CollectionJsonDocument.class, property) //
					.serialize(doc, gen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueSerializer#createContextual(tools.jackson.databind.SerializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
			return new CollectionJsonResourceSerializer(property);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentSerializer()
		 */
		@Override
		@Nullable
		public ValueSerializer<?> getContentSerializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#isEmpty(tools.jackson.databind.SerializationContext, java.lang.Object)
		 */
		@Override
		public boolean isEmpty(SerializationContext prov, EntityModel<?> value) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(EntityModel<?> value) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#_withValueTypeSerializer(tools.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected StdContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonResourcesSerializer extends StdContainerSerializer<CollectionModel<?>> {

		CollectionJsonResourcesSerializer() {
			super(CollectionModel.class);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(CollectionModel<?> value, JsonGenerator jgen, SerializationContext context) {

			CollectionJson<Object> collectionJson = new CollectionJson<>() //
					.withVersion("1.0") //
					.withHref(value.getRequiredLink(IanaLinkRelations.SELF).getHref()) //
					.withLinks(value.getLinks().without(IanaLinkRelations.SELF)) //
					.withItems(resourcesToCollectionJsonItems(value)) //
					.withQueries(findQueries(value)) //
					.withTemplate(findTemplate(value));

			CollectionJsonDocument<?> doc = new CollectionJsonDocument<>(collectionJson);

			context.findValueSerializer(CollectionJsonDocument.class) //
					.serialize(doc, jgen, context);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentSerializer()
		 */
		@Override
		@Nullable
		public ValueSerializer<?> getContentSerializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#isEmpty(tools.jackson.databind.SerializationContext, java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean isEmpty(SerializationContext provider, CollectionModel<?> value) {
			return value.getContent().isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(CollectionModel<?> value) {
			return value.getContent().size() == 1;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected StdContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonPagedResourcesSerializer extends StdContainerSerializer<PagedModel<?>> {

		private final @Nullable BeanProperty property;

		CollectionJsonPagedResourcesSerializer() {
			this(null);
		}

		CollectionJsonPagedResourcesSerializer(@Nullable BeanProperty property) {

			super(CollectionModel.class);
			this.property = property;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(PagedModel<?> value, JsonGenerator jgen, SerializationContext context) {

			CollectionJson<?> collectionJson = new CollectionJson<>() //
					.withVersion("1.0") //
					.withHref(value.getRequiredLink(IanaLinkRelations.SELF).getHref()) //
					.withLinks(value.getLinks().without(IanaLinkRelations.SELF)) //
					.withItems(resourcesToCollectionJsonItems(value)) //
					.withQueries(findQueries(value)) //
					.withTemplate(findTemplate(value));

			CollectionJsonDocument<?> doc = new CollectionJsonDocument<>(collectionJson);

			context.findPrimaryPropertySerializer(CollectionJsonDocument.class, property)
					.serialize(doc, jgen, context);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueSerializer#createContextual(tools.jackson.databind.SerializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
			return new CollectionJsonPagedResourcesSerializer(property);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentSerializer()
		 */
		@Override
		@Nullable
		public ValueSerializer<?> getContentSerializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#isEmpty(tools.jackson.databind.SerializationContext, java.lang.Object)
		 */
		@Override
		public boolean isEmpty(SerializationContext prov, PagedModel<?> value) {
			return value.getContent().isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(PagedModel<?> value) {
			return value.getContent().size() == 1;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.collectionjson.Jackson2CollectionJsonModule.CollectionJsonPagedResourcesSerializer#_withValueTypeSerializer(tools.jackson.databind.jsontype.TypeSerializer)
		 */
		@Nullable
		@Override
		protected StdContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonLinksDeserializer extends ContainerDeserializerBase<Links> {

		CollectionJsonLinksDeserializer() {
			super(TYPE_FACTORY.constructCollectionLikeType(List.class, Link.class));
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
		public ValueDeserializer<Object> getContentDeserializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ValueDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public Links deserialize(JsonParser jp, DeserializationContext ctx) {

			JavaType type = ctx.getTypeFactory().constructCollectionLikeType(List.class, Link.class);

			return Links.of(jp.<List<Link>> readValueAs(type));
		}
	}

	static class CollectionJsonResourceSupportDeserializer extends ContainerDeserializerBase<RepresentationModel<?>> {

		private final JavaType contentType;

		CollectionJsonResourceSupportDeserializer() {
			this(TYPE_FACTORY.constructType(RepresentationModel.class));
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
		public ValueDeserializer<Object> getContentDeserializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ValueDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		public RepresentationModel<?> deserialize(JsonParser jp, DeserializationContext ctxt) {

			TypeFactory typeFactory = ctxt.getTypeFactory();

			JavaType rootType = typeFactory.constructSimpleType(Object.class, new JavaType[] {});
			JavaType wrappedType = typeFactory.constructParametricType(CollectionJsonDocument.class, rootType);

			CollectionJsonDocument<?> document = jp.readValueAs(wrappedType);
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
		 * @see tools.jackson.databind.ValueDeserializer#createContextual(tools.jackson.databind.DeserializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueDeserializer<?> createContextual(DeserializationContext context, @Nullable BeanProperty property) {

			JavaType type = property == null //
					? context.getContextualType() //
					: property.getType().getContentType();

			return new CollectionJsonResourceSupportDeserializer(type);
		}
	}

	static class CollectionJsonResourceDeserializer extends ContainerDeserializerBase<EntityModel<?>> {

		private final JavaType contentType;

		CollectionJsonResourceDeserializer() {
			this(TYPE_FACTORY.constructType(CollectionJson.class));
		}

		CollectionJsonResourceDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
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
		public EntityModel<?> deserialize(JsonParser jp, DeserializationContext ctxt) {

			JavaType rootType = JacksonHelper.findRootType(this.contentType);
			JavaType wrappedType = ctxt.getTypeFactory().constructParametricType(CollectionJsonDocument.class, rootType);

			CollectionJsonDocument<?> document = jp.readValueAs(wrappedType);

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
		public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {

			return new CollectionJsonResourceDeserializer(
					property == null ? ctxt.getContextualType() : property.getType().getContentType());
		}
	}

	static abstract class CollectionValueDeserializerBase<T extends CollectionModel<?>>
			extends ContainerDeserializerBase<T> {

		private final JavaType contentType;
		private final BiFunction<List<Object>, Links, T> finalizer;
		private final Function<JavaType, CollectionValueDeserializerBase<T>> creator;

		CollectionValueDeserializerBase(BiFunction<List<Object>, Links, T> finalizer,
				Function<JavaType, CollectionValueDeserializerBase<T>> creator) {
			this(TYPE_FACTORY.constructType(CollectionJson.class), finalizer, creator);
		}

		private CollectionValueDeserializerBase(JavaType contentType, BiFunction<List<Object>, Links, T> finalizer,
				Function<JavaType, CollectionValueDeserializerBase<T>> creator) {

			super(contentType);

			this.contentType = contentType;
			this.finalizer = finalizer;
			this.creator = creator;
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
		public ValueDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {

			JavaType contextualType = property == null //
					? ctxt.getContextualType() //
					: property.getType().getContentType();

			return creator.apply(contextualType);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public T deserialize(JsonParser parser, DeserializationContext ctxt) {

			JavaType rootType = JacksonHelper.findRootType(contentType);
			JavaType wrappedType = ctxt.getTypeFactory().constructParametricType(CollectionJsonDocument.class, rootType);

			CollectionJsonDocument<?> document = parser.readValueAs(wrappedType);
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

	static class CollectionJsonResourcesDeserializer extends CollectionValueDeserializerBase<CollectionModel<?>> {

		private static final BiFunction<List<Object>, Links, CollectionModel<?>> FINISHER = CollectionModel::of;
		private static final Function<JavaType, CollectionValueDeserializerBase<CollectionModel<?>>> CONTEXTUAL_CREATOR = CollectionJsonResourcesDeserializer::new;

		CollectionJsonResourcesDeserializer() {
			super(FINISHER, CONTEXTUAL_CREATOR);
		}

		private CollectionJsonResourcesDeserializer(JavaType contentType) {
			super(contentType, FINISHER, CONTEXTUAL_CREATOR);
		}
	}

	static class CollectionJsonPagedResourcesDeserializer extends CollectionValueDeserializerBase<PagedModel<?>> {

		private static final BiFunction<List<Object>, Links, PagedModel<?>> FINISHER = (content, links) -> PagedModel
				.of(content, null, links);
		private static final Function<JavaType, CollectionValueDeserializerBase<PagedModel<?>>> CONTEXTUAL_CREATOR = CollectionJsonPagedResourcesDeserializer::new;

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
