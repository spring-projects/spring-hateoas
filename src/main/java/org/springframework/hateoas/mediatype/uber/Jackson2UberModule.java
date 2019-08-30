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
package org.springframework.hateoas.mediatype.uber;

import static org.springframework.hateoas.mediatype.JacksonHelper.*;
import static org.springframework.hateoas.mediatype.uber.UberData.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
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
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Jackson {@link SimpleModule} for {@literal UBER+JSON} serializers and deserializers.
 *
 * @author Greg Turnquist
 * @author Jens Schauder
 * @since 1.0
 */
public class Jackson2UberModule extends SimpleModule {

	private static final long serialVersionUID = -2396790508486870880L;

	public Jackson2UberModule() {

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
	 * Custom {@link JsonSerializer} to render {@link RepresentationModel} into {@literal UBER+JSON}.
	 */
	static class UberRepresentationModelSerializer extends ContainerSerializer<RepresentationModel<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = -572866287910993300L;
		private final BeanProperty property;

		UberRepresentationModelSerializer(@Nullable BeanProperty property) {

			super(RepresentationModel.class, false);
			this.property = property;
		}

		UberRepresentationModelSerializer() {
			this(null);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(RepresentationModel<?> value, JsonGenerator gen, SerializerProvider provider)
				throws IOException {

			UberDocument doc = new UberDocument() //
					.withUber(new Uber() //
							.withVersion("1.0") //
							.withData(extractLinksAndContent(value)));

			provider //
					.findValueSerializer(UberDocument.class, property) //
					.serialize(doc, gen, provider);
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

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new UberRepresentationModelSerializer(property);
		}
	}

	/**
	 * Custom {@link JsonSerializer} to render {@link EntityModel} into {@literal UBER+JSON}.
	 */
	static class UberEntityModelSerializer extends ContainerSerializer<EntityModel<?>> implements ContextualSerializer {

		private static final long serialVersionUID = -5538560800604582741L;

		private final BeanProperty property;

		UberEntityModelSerializer(@Nullable BeanProperty property) {

			super(EntityModel.class, false);
			this.property = property;
		}

		UberEntityModelSerializer() {
			this(null);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(EntityModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			UberDocument doc = new UberDocument().withUber(new Uber() //
					.withVersion("1.0") //
					.withData(extractLinksAndContent(value)));

			provider //
					.findValueSerializer(UberDocument.class, property) //
					.serialize(doc, gen, provider);
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

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new UberEntityModelSerializer(property);
		}
	}

	/**
	 * Custom {@link JsonSerializer} to render {@link CollectionModel} into {@literal UBER+JSON}.
	 */
	static class UberCollectionModelSerializer extends ContainerSerializer<CollectionModel<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = 3422019794262694127L;

		private BeanProperty property;

		UberCollectionModelSerializer(@Nullable BeanProperty property) {

			super(CollectionModel.class, false);
			this.property = property;
		}

		UberCollectionModelSerializer() {
			this(null);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(CollectionModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			UberDocument doc = new UberDocument() //
					.withUber(new Uber() //
							.withVersion("1.0") //
							.withData(extractLinksAndContent(value)));

			provider //
					.findValueSerializer(UberDocument.class, property) //
					.serialize(doc, gen, provider);
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

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new UberCollectionModelSerializer(property);
		}
	}

	/**
	 * Custom {@link JsonSerializer} to render {@link PagedModel} into {@literal UBER+JSON}.
	 */
	static class UberPagedModelSerializer extends ContainerSerializer<PagedModel<?>> implements ContextualSerializer {

		private static final long serialVersionUID = -7892297813593085984L;

		private BeanProperty property;

		UberPagedModelSerializer(@Nullable BeanProperty property) {

			super(PagedModel.class, false);
			this.property = property;
		}

		UberPagedModelSerializer() {
			this(null);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(PagedModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			UberDocument doc = new UberDocument() //
					.withUber(new Uber() //
							.withVersion("1.0") //
							.withData(extractLinksAndContent(value)));

			provider //
					.findValueSerializer(UberDocument.class, property) //
					.serialize(doc, gen, provider);
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

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
			return new UberPagedModelSerializer(property);
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link RepresentationModel}.
	 */
	static class UberRepresentationModelDeserializer extends ContainerDeserializerBase<RepresentationModel<?>>
			implements ContextualDeserializer {

		private static final long serialVersionUID = -8738539821441549016L;
		private final JavaType contentType;

		UberRepresentationModelDeserializer() {
			this(TypeFactory.defaultInstance().constructType(RepresentationModel.class));
		}

		private UberRepresentationModelDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public RepresentationModel<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

			UberDocument doc = p.getCodec().readValue(p, UberDocument.class);
			Links links = doc.getUber().getLinks();

			RepresentationModel<?> result = doc.getUber().getData().stream() //
					.filter(uberData -> !StringUtils.isEmpty(uberData.getName())) //
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
		 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentType()
		 */
		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.ContextualDeserializer#createContextual(com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {

			JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();

			return new UberRepresentationModelDeserializer(type);
		}

		/**
		 * Accessor for deserializer use for deserializing content values.
		 */
		@Override
		@Nullable
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link EntityModel}.
	 */
	static class UberEntityModelDeserializer extends ContainerDeserializerBase<EntityModel<?>>
			implements ContextualDeserializer {

		private static final long serialVersionUID = 1776321413269082414L;

		private final JavaType contentType;

		UberEntityModelDeserializer() {
			this(TypeFactory.defaultInstance().constructSimpleType(UberDocument.class, new JavaType[0]));
		}

		private UberEntityModelDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public EntityModel<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

			UberDocument doc = p.getCodec().readValue(p, UberDocument.class);
			Links links = doc.getUber().getLinks();

			return doc.getUber().getData().stream() //
					.filter(uberData -> !StringUtils.isEmpty(uberData.getName())) //
					.findFirst() //
					.map(uberData -> convertToResource(uberData, links)) //
					.orElseThrow(
							() -> new IllegalStateException("No data entry containing a 'value' was found in this document!"));
		}

		@NotNull
		private EntityModel<Object> convertToResource(UberData uberData, Links links) {

			// Primitive type
			List<UberData> data = uberData.getData();

			if (data == null) {
				throw new IllegalStateException();
			}

			if (isPrimitiveType(data)) {

				UberData firstItem = data.get(0);

				Object scalarValue = firstItem.getValue();
				return new EntityModel<>(scalarValue, links);
			}

			Map<String, Object> properties = data == null //
					? new HashMap<>() //
					: data.stream().collect(Collectors.toMap(UberData::getName, UberData::getValue));

			JavaType rootType = JacksonHelper.findRootType(this.contentType);
			Object value = PropertyUtils.createObjectFromProperties(rootType.getRawClass(), properties);

			return new EntityModel<>(value, links);
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
		 * @see com.fasterxml.jackson.databind.deser.ContextualDeserializer#createContextual(com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {

			JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();

			return new UberEntityModelDeserializer(type);
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		@Nullable
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link CollectionModel}.
	 */
	static class UberCollectionModelDeserializer extends ContainerDeserializerBase<CollectionModel<?>>
			implements ContextualDeserializer {

		private static final long serialVersionUID = 8722467561709171145L;

		private final JavaType contentType;

		UberCollectionModelDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberCollectionModelDeserializer() {
			this(TypeFactory.defaultInstance().constructSimpleType(UberDocument.class, new JavaType[0]));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public CollectionModel<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

			JavaType rootType = JacksonHelper.findRootType(this.contentType);
			UberDocument doc = p.getCodec().readValue(p, UberDocument.class);

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
		 * @see com.fasterxml.jackson.databind.deser.ContextualDeserializer#createContextual(com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {

			JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();

			return new UberCollectionModelDeserializer(type);
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		@Nullable
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link PagedModel}.
	 */
	static class UberPagedModelDeserializer extends ContainerDeserializerBase<PagedModel<?>>
			implements ContextualDeserializer {

		private static final long serialVersionUID = 4123359694609188745L;

		private JavaType contentType;

		UberPagedModelDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberPagedModelDeserializer() {
			this(TypeFactory.defaultInstance().constructSimpleType(UberDocument.class, new JavaType[0]));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public PagedModel<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

			JavaType rootType = JacksonHelper.findRootType(this.contentType);

			UberDocument doc = p.getCodec().readValue(p, UberDocument.class);

			CollectionModel<?> resources = extractResources(doc, rootType, this.contentType);
			PageMetadata pageMetadata = extractPagingMetadata(doc);

			return new PagedModel<>(resources.getContent(), pageMetadata, resources.getLinks());
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
		 * @see com.fasterxml.jackson.databind.deser.ContextualDeserializer#createContextual(com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {

			JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();

			return new UberPagedModelDeserializer(type);
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		@Nullable
		public JsonDeserializer<Object> getContentDeserializer() {
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
			EntityModel<?> resource = null;

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
						resource = new EntityModel<>(scalarValue, uberData.getLinks());

					} else {

						Map<String, Object> properties = itemData == null //
								? new HashMap<>() //
								: itemData.stream() //
										.collect(Collectors.toMap(UberData::getName, UberData::getValue));

						Object obj = PropertyUtils.createObjectFromProperties(rootType.getRawClass(), properties);
						resource = new EntityModel<>(obj, uberData.getLinks());
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
			return new CollectionModel<>(content, doc.getUber().getLinks());
		} else {
			/*
			 * ...or return a Resources<T>
			 */

			List<Object> resourceLessContent = content.stream().map(item -> (EntityModel<?>) item)
					.map(EntityModel::getContent).collect(Collectors.toList());

			return new CollectionModel<>(resourceLessContent, doc.getUber().getLinks());
		}
	}

	private static boolean isPrimitiveType(@Nullable List<UberData> data) {
		return data != null && data.size() == 1 && data.get(0).getName() == null;
	}

	@Nullable
	private static PageMetadata extractPagingMetadata(UberDocument doc) {

		return doc.getUber().getData().stream() //
				.filter(uberData -> Optional.ofNullable(uberData.getName()).map("page"::equals).orElse(false)) //
				.findFirst().map(Jackson2UberModule::convertUberDataToPageMetaData) //
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

		private static final long serialVersionUID = -6198451472474285487L;

		UberActionDeserializer() {
			super(UberAction.class);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public UberAction deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return UberAction.valueOf(p.getText().toUpperCase());
		}
	}
}
