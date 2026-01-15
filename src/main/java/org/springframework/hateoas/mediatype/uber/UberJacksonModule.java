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
package org.springframework.hateoas.mediatype.uber;

import static org.springframework.hateoas.mediatype.JacksonHelper.*;
import static org.springframework.hateoas.mediatype.uber.UberData.*;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.ContainerDeserializerBase;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdContainerSerializer;
import tools.jackson.databind.type.TypeFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.JacksonHelper;
import org.springframework.hateoas.mediatype.PropertyUtils;
import org.springframework.util.StringUtils;

/**
 * Jackson {@link SimpleModule} for {@literal UBER+JSON} serializers and deserializers.
 *
 * @author Greg Turnquist
 * @author Jens Schauder
 * @since 1.0
 */
public class UberJacksonModule extends SimpleModule {

	private static final long serialVersionUID = -2396790508486870880L;
	private static final TypeFactory TYPE_FACTORY = TypeFactory.createDefaultInstance();

	public UberJacksonModule() {

		super("uber-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

		addSerializer(new UberPagedModelSerializer());
		addSerializer(new UberCollectionModelSerializer());
		addSerializer(new UberEntityModelSerializer());
		addSerializer(new UberRepresentationModelSerializer());

		setMixInAnnotation(RepresentationModel.class, RepresentationModelMixin.class);
		setMixInAnnotation(EntityModel.class, EntityModelMixin.class);
		setMixInAnnotation(CollectionModel.class, CollectionModelMixin.class);
		setMixInAnnotation(PagedModel.class, PagedModelMixin.class);
	}

	/**
	 * Jackson 2 mixin to handle {@link RepresentationModel} for {@literal UBER+JSON}.
	 *
	 * @author Greg Turnquist
	 * @since 1.0
	 */
	@JsonDeserialize(using = UberRepresentationModelDeserializer.class)
	abstract class RepresentationModelMixin extends RepresentationModel<RepresentationModelMixin> {}

	/**
	 * Jackson 2 mixin to handle {@link EntityModel} for {@literal UBER+JSON}.
	 *
	 * @author Greg Turnquist
	 * @since 1.0
	 */
	@JsonDeserialize(using = UberEntityModelDeserializer.class)
	abstract class EntityModelMixin<T> extends EntityModel<T> {}

	/**
	 * Jackson 2 mixin to handle {@link CollectionModel} for {@literal UBER+JSON}.
	 *
	 * @author Greg Turnquist
	 * @since 1.0
	 */
	@JsonDeserialize(using = UberCollectionModelDeserializer.class)
	abstract class CollectionModelMixin<T> extends CollectionModel<T> {}

	/**
	 * Jackson 2 mixin to handle {@link PagedModel} for {@literal UBER+JSON}.
	 *
	 * @author Greg Turnquist
	 * @since 1.0
	 */
	@JsonDeserialize(using = UberPagedModelDeserializer.class)
	abstract class PagedModelMixin<T> extends PagedModel<T> {}

	/**
	 * Custom {@link ValueSerializer} to render {@link RepresentationModel} into {@literal UBER+JSON}.
	 */
	static class UberRepresentationModelSerializer extends StdContainerSerializer<RepresentationModel<?>> {

		private static final long serialVersionUID = -572866287910993300L;
		private final @Nullable BeanProperty property;

		UberRepresentationModelSerializer(@Nullable BeanProperty property) {

			super(RepresentationModel.class);
			this.property = property;
		}

		UberRepresentationModelSerializer() {
			this(null);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(RepresentationModel<?> value, JsonGenerator gen, SerializationContext provider) {

			UberDocument doc = new UberDocument() //
					.withUber(new Uber() //
							.withVersion("1.0") //
							.withData(extractLinksAndContent(value)));

			provider.findPrimaryPropertySerializer(UberDocument.class, property) //
					.serialize(doc, gen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#getContentSerializer()
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
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(RepresentationModel<?> value) {
			return false;
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

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueSerializer#createContextual(tools.jackson.databind.SerializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) {
			return new UberRepresentationModelSerializer(property);
		}
	}

	/**
	 * Custom {@link ValueSerializer} to render {@link EntityModel} into {@literal UBER+JSON}.
	 */
	static class UberEntityModelSerializer extends StdContainerSerializer<EntityModel<?>> {

		private final @Nullable BeanProperty property;

		UberEntityModelSerializer(@Nullable BeanProperty property) {

			super(EntityModel.class);
			this.property = property;
		}

		UberEntityModelSerializer() {
			this(null);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(EntityModel<?> value, JsonGenerator gen, SerializationContext context) {

			UberDocument doc = new UberDocument().withUber(new Uber() //
					.withVersion("1.0") //
					.withData(extractLinksAndContent(value)));

			context.findPrimaryPropertySerializer(UberDocument.class, property) //
					.serialize(doc, gen, context);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#getContentSerializer()
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
			return value.getContent() == null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(EntityModel<?> value) {
			return false;
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

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueSerializer#createContextual(tools.jackson.databind.SerializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) {
			return new UberEntityModelSerializer(property);
		}
	}

	/**
	 * Custom {@link ValueSerializer} to render {@link CollectionModel} into {@literal UBER+JSON}.
	 */
	static class UberCollectionModelSerializer extends StdContainerSerializer<CollectionModel<?>> {

		private @Nullable BeanProperty property;

		UberCollectionModelSerializer(@Nullable BeanProperty property) {

			super(CollectionModel.class);
			this.property = property;
		}

		UberCollectionModelSerializer() {
			this(null);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(CollectionModel<?> value, JsonGenerator gen, SerializationContext provider) {

			UberDocument doc = new UberDocument() //
					.withUber(new Uber() //
							.withVersion("1.0") //
							.withData(extractLinksAndContent(value)));

			provider.findPrimaryPropertySerializer(UberDocument.class, property)
					.serialize(doc, gen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#getContentSerializer()
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
		public boolean isEmpty(SerializationContext prov, CollectionModel<?> value) {
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
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#_withValueTypeSerializer(tools.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected StdContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueSerializer#createContextual(tools.jackson.databind.SerializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) {
			return new UberCollectionModelSerializer(property);
		}
	}

	/**
	 * Custom {@link ValueSerializer} to render {@link PagedModel} into {@literal UBER+JSON}.
	 */
	static class UberPagedModelSerializer extends StdContainerSerializer<PagedModel<?>> {

		private @Nullable BeanProperty property;

		UberPagedModelSerializer(@Nullable BeanProperty property) {

			super(PagedModel.class);
			this.property = property;
		}

		UberPagedModelSerializer() {
			this(null);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(PagedModel<?> value, JsonGenerator gen, SerializationContext provider) {

			UberDocument doc = new UberDocument() //
					.withUber(new Uber() //
							.withVersion("1.0") //
							.withData(extractLinksAndContent(value)));

			provider.findPrimaryPropertySerializer(UberDocument.class, property)
					.serialize(doc, gen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#getContentSerializer()
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
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#_withValueTypeSerializer(tools.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		@SuppressWarnings("null")
		protected StdContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueSerializer#createContextual(tools.jackson.databind.SerializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) {
			return new UberPagedModelSerializer(property);
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link RepresentationModel}.
	 */
	static class UberRepresentationModelDeserializer extends ContainerDeserializerBase<RepresentationModel<?>> {

		private final JavaType contentType;

		UberRepresentationModelDeserializer() {
			this(TYPE_FACTORY.constructType(RepresentationModel.class));
		}

		private UberRepresentationModelDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public RepresentationModel<?> deserialize(JsonParser p, DeserializationContext ctxt) {

			UberDocument doc = p.readValueAs(UberDocument.class);
			Links links = doc.getUber().getLinks();

			RepresentationModel<?> result = doc.getUber().getData().stream() //
					.filter(uberData -> StringUtils.hasText(uberData.getName())) //
					.findFirst() //
					.map(uberData -> convertToResourceSupport(uberData, links)) //
					.orElse(null);

			return result == null ? new RepresentationModel<>().add(links) : result;
		}

		@NotNull
		private RepresentationModel<?> convertToResourceSupport(UberData uberData, Links links) {

			List<UberData> data = uberData.getData();
			Map<String, Object> properties;

			if (data == null) {
				properties = new HashMap<>();
			} else {
				properties = data.stream() //
						.collect(Collectors.toMap(UberData::getName, UberData::getValue));
			}

			RepresentationModel<?> resourceSupport = (RepresentationModel<?>) PropertyUtils
					.createObjectFromProperties(this.getContentType().getRawClass(), properties);

			return resourceSupport.add(links);
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
		 * @see tools.jackson.databind.ValueDeserializer#createContextual(tools.jackson.databind.DeserializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {

			JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();

			return new UberRepresentationModelDeserializer(type);
		}

		/**
		 * Accessor for deserializer use for deserializing content values.
		 */
		@Override
		@Nullable
		public ValueDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link EntityModel}.
	 */
	static class UberEntityModelDeserializer extends ContainerDeserializerBase<RepresentationModel<?>> {

		private final JavaType contentType;

		UberEntityModelDeserializer() {
			this(TYPE_FACTORY.constructSimpleType(UberDocument.class, new JavaType[0]));
		}

		private UberEntityModelDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ValueDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public RepresentationModel<?> deserialize(JsonParser p, DeserializationContext ctxt) {

			UberDocument doc = p.readValueAs(UberDocument.class);
			Links links = doc.getUber().getLinks();

			return doc.getUber().getData().stream() //
					.filter(uberData -> StringUtils.hasText(uberData.getName())) //
					.findFirst() //
					.map(uberData -> convertToResource(uberData, links)) //
					.orElseThrow(
							() -> new IllegalStateException("No data entry containing a 'value' was found in this document!"));
		}

		private RepresentationModel<?> convertToResource(UberData uberData, Links links) {

			// Primitive type
			List<UberData> data = uberData.getData();

			if (data == null) {
				throw new IllegalStateException();
			}

			if (isPrimitiveType(data)) {

				UberData firstItem = data.get(0);
				Object scalarValue = firstItem.getValue();

				return RepresentationModel.of(scalarValue, links);
			}

			Map<String, Object> properties = data == null //
					? new HashMap<>() //
					: data.stream().collect(Collectors.toMap(UberData::getName, UberData::getValue));

			JavaType rootType = JacksonHelper.findRootType(this.contentType);
			Object value = PropertyUtils.createObjectFromProperties(rootType.getRawClass(), properties);

			return EntityModel.of(value, links);
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
		 * @see tools.jackson.databind.ValueDeserializer#createContextual(tools.jackson.databind.DeserializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {

			JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();

			return new UberEntityModelDeserializer(type);
		}

		/**
		 * Accessor for deserializer use for deserializing content values.
		 */
		@Override
		@Nullable
		public ValueDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link CollectionModel}.
	 */
	static class UberCollectionModelDeserializer extends ContainerDeserializerBase<CollectionModel<?>> {

		private final JavaType contentType;

		UberCollectionModelDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberCollectionModelDeserializer() {
			this(TYPE_FACTORY.constructSimpleType(UberDocument.class, new JavaType[0]));
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public CollectionModel<?> deserialize(JsonParser p, DeserializationContext ctxt) {

			JavaType rootType = JacksonHelper.findRootType(this.contentType);
			UberDocument doc = p.readValueAs(UberDocument.class);

			return extractResources(doc, rootType, this.contentType);
		}

		/**
		 * Accessor for declared type of contained value elements; either exact type, or one of its supertypes.
		 */
		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#createContextual(tools.jackson.databind.DeserializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {

			JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();

			return new UberCollectionModelDeserializer(type);
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		@Nullable
		public ValueDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link PagedModel}.
	 */
	static class UberPagedModelDeserializer extends ContainerDeserializerBase<PagedModel<?>> {

		private JavaType contentType;

		UberPagedModelDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberPagedModelDeserializer() {
			this(TYPE_FACTORY.constructSimpleType(UberDocument.class, new JavaType[0]));
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public PagedModel<?> deserialize(JsonParser p, DeserializationContext ctxt) {

			JavaType rootType = JacksonHelper.findRootType(this.contentType);

			UberDocument doc = p.readValueAs(UberDocument.class);

			CollectionModel<?> resources = extractResources(doc, rootType, this.contentType);
			PageMetadata pageMetadata = extractPagingMetadata(doc);

			return PagedModel.of(resources.getContent(), pageMetadata, resources.getLinks());
		}

		/**
		 * Accessor for declared type of contained value elements; either exact type, or one of its supertypes.
		 */
		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#createContextual(tools.jackson.databind.DeserializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {

			JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();

			return new UberPagedModelDeserializer(type);
		}

		/**
		 * Accessor for deserializer use for deserializing content values.
		 */
		@Override
		@Nullable
		public ValueDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	/**
	 * Convert an {@link UberDocument} into a {@link CollectionModel}.
	 *
	 * @param doc
	 * @param rootType
	 * @param contentType
	 * @return
	 */
	private static CollectionModel<?> extractResources(UberDocument doc, JavaType rootType, JavaType contentType) {

		List<Object> content = new ArrayList<>();

		for (UberData uberData : doc.getUber().getData()) {

			String name = uberData.getName();

			if (name != null && name.equals("page")) {
				continue;
			}

			if (!uberData.getLinks().isEmpty()) {
				continue;
			}

			List<Link> resourceLinks = new ArrayList<>();
			RepresentationModel<?> resource = null;

			List<UberData> data = uberData.getData();

			if (data == null) {
				throw new RuntimeException("No content!");
			}

			for (UberData item : data) {

				List<LinkRelation> rel = item.getRel();

				if (rel != null) {
					resourceLinks.addAll(item.getLinks());
				} else {

					// Primitive type
					List<UberData> itemData = item.getData();

					if (isPrimitiveType(itemData)) {

						if (itemData == null) {
							throw new IllegalStateException();
						}

						UberData firstItem = itemData.get(0);
						Object scalarValue = firstItem.getValue();

						resource = RepresentationModel.of(scalarValue, uberData.getLinks());

					} else {

						Map<String, Object> properties = itemData == null //
								? new HashMap<>() //
								: itemData.stream() //
										.collect(Collectors.toMap(UberData::getName, UberData::getValue));

						Object obj = PropertyUtils.createObjectFromProperties(rootType.getRawClass(), properties);
						resource = EntityModel.of(obj, uberData.getLinks());
					}
				}
			}

			if (resource != null) {

				resource.add(resourceLinks);
				content.add(resource);
			} else {
				throw new RuntimeException("No content!");
			}
		}

		if (isResourcesOfResource(contentType)) {
			/*
			 * Either return a Resources<Resource<T>>...
			 */
			return CollectionModel.of(content, doc.getUber().getLinks());
		} else {
			/*
			 * ...or return a Resources<T>
			 */

			List<Object> resourceLessContent = content.stream().map(item -> (EntityModel<?>) item)
					.map(EntityModel::getContent).collect(Collectors.toList());

			return CollectionModel.of(resourceLessContent, doc.getUber().getLinks());
		}
	}

	private static boolean isPrimitiveType(@Nullable List<UberData> data) {
		return data != null && data.size() == 1 && data.get(0).getName() == null;
	}

	@Nullable
	private static PageMetadata extractPagingMetadata(UberDocument doc) {

		return doc.getUber().getData().stream() //
				.filter(uberData -> Optional.ofNullable(uberData.getName()).map("page"::equals).orElse(false)) //
				.findFirst().map(UberJacksonModule::convertUberDataToPageMetaData) //
				.orElse(null);
	}

	@SuppressWarnings("null")
	private static PageMetadata convertUberDataToPageMetaData(UberData uberData) {

		int size = 0;
		int number = 0;
		int totalElements = 0;
		int totalPages = 0;

		List<UberData> content = uberData.getData();

		if (content != null) {

			for (UberData data : content) {

				String name = data.getName();
				Object value = data.getValue();

				switch (name) {

					case "size":
						size = (int) value;
						break;

					case "number":
						number = (int) value;
						break;

					case "totalElements":
						totalElements = (int) value;
						break;

					case "totalPages":
						totalPages = (int) value;
						break;

					default:
				}
			}
		}

		return new PageMetadata(size, number, totalElements, totalPages);
	}

	/**
	 * Customer deserializer to handle {@link UberAction}.
	 */
	static class UberActionDeserializer extends StdDeserializer<UberAction> {

		UberActionDeserializer() {
			super(UberAction.class);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public UberAction deserialize(JsonParser p, DeserializationContext ctxt) {
			return UberAction.valueOf(p.getString().toUpperCase());
		}
	}
}
