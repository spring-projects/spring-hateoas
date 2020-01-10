/*
 * Copyright 2012-2020 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.HalConfiguration.RenderSingleLinks;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Jackson 2 module implementation to render {@link Link} and {@link RepresentationModel} instances in HAL compatible
 * JSON.
 *
 * @author Alexander Baetz
 * @author Oliver Gierke
 * @author Jeffrey Walraven
 */
public class Jackson2HalModule extends SimpleModule {

	private static final long serialVersionUID = 7806951456457932384L;
	private static final Link CURIES_REQUIRED_DUE_TO_EMBEDS = new Link("__rel__", "¯\\_(ツ)_/¯");

	public Jackson2HalModule() {

		super("json-hal-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(RepresentationModel.class, RepresentationModelMixin.class);
		setMixInAnnotation(CollectionModel.class, CollectionModelMixin.class);
	}

	/**
	 * Returns whether the module was already registered in the given {@link ObjectMapper}.
	 *
	 * @param mapper must not be {@literal null}.
	 * @return
	 */
	public static boolean isAlreadyRegisteredIn(ObjectMapper mapper) {

		Assert.notNull(mapper, "ObjectMapper must not be null!");
		return LinkMixin.class.equals(mapper.findMixInClassFor(Link.class));
	}

	/**
	 * Custom {@link JsonSerializer} to render Link instances in HAL compatible JSON.
	 *
	 * @author Alexander Baetz
	 * @author Oliver Gierke
	 */
	public static class HalLinkListSerializer extends ContainerSerializer<Links> implements ContextualSerializer {

		private static final long serialVersionUID = -1844788111509966406L;

		private final @Nullable BeanProperty property;
		private final CurieProvider curieProvider;
		private final EmbeddedMapper mapper;
		private final MessageResolver resolver;
		private final HalConfiguration halConfiguration;

		public HalLinkListSerializer(CurieProvider curieProvider, EmbeddedMapper mapper, MessageResolver resolver,
				HalConfiguration halConfiguration) {
			this(null, curieProvider, mapper, resolver, halConfiguration);
		}

		public HalLinkListSerializer(@Nullable BeanProperty property, CurieProvider curieProvider, EmbeddedMapper mapper,
				MessageResolver resolver, HalConfiguration halConfiguration) {

			super(TypeFactory.defaultInstance().constructType(Links.class));

			Assert.notNull(curieProvider, "CurieProvider must not be null!");
			Assert.notNull(mapper, "EmbeddedMapper must not be null!");
			Assert.notNull(resolver, "MessageResolver must not be null!");
			Assert.notNull(halConfiguration, "HalConfiguration must not be null!");

			this.property = property;
			this.curieProvider = curieProvider;
			this.mapper = mapper;
			this.resolver = resolver;
			this.halConfiguration = halConfiguration;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(Links value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

			// sort links according to their relation
			Map<LinkRelation, List<Object>> sortedLinks = new LinkedHashMap<>();
			List<Link> links = new ArrayList<>();

			boolean prefixingRequired = curieProvider != CurieProvider.NONE;
			boolean curiedLinkPresent = false;
			boolean skipCuries = !jgen.getOutputContext().getParent().inRoot();

			Object currentValue = jgen.getCurrentValue();

			if (currentValue instanceof CollectionModel) {
				if (mapper.hasCuriedEmbed((CollectionModel<?>) currentValue)) {
					curiedLinkPresent = true;
				}
			}

			for (Link link : value) {

				if (link.equals(CURIES_REQUIRED_DUE_TO_EMBEDS)) {
					continue;
				}

				LinkRelation rel = prefixingRequired ? curieProvider.getNamespacedRelFrom(link) : link.getRel();

				if (!link.hasRel(rel)) {
					curiedLinkPresent = true;
				}

				sortedLinks //
						.computeIfAbsent(rel, key -> new ArrayList<>())//
						.add(toHalLink(link));

				links.add(link);
			}

			if (!skipCuries && prefixingRequired && curiedLinkPresent) {

				List<Object> curies = new ArrayList<>(curieProvider.getCurieInformation(Links.of(links)));

				sortedLinks.put(HalLinkRelation.CURIES, curies);
			}

			TypeFactory typeFactory = provider.getConfig().getTypeFactory();
			JavaType keyType = typeFactory.constructType(LinkRelation.class);
			JavaType valueType = typeFactory.constructCollectionType(ArrayList.class, Object.class);
			JavaType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);

			MapSerializer serializer = MapSerializer.construct(Collections.emptySet(), mapType, true, null,
					provider.findKeySerializer(keyType, null), new OptionalListJackson2Serializer(property, halConfiguration),
					null);

			serializer.serialize(sortedLinks, jgen, provider);
		}

		/**
		 * Wraps the given link into a HAL specific extension.
		 *
		 * @param link must not be {@literal null}.
		 * @return
		 */
		private HalLink toHalLink(Link link) {

			HalLinkRelation rel = HalLinkRelation.of(link.getRel());

			return new HalLink(link, resolver.resolve(rel));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
				throws JsonMappingException {
			return new HalLinkListSerializer(property, curieProvider, mapper, resolver, halConfiguration);
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
		public boolean isEmpty(SerializerProvider provider, Links value) {
			return value.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(Links value) {
			return value.toList().size() == 1;
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

	/**
	 * Custom {@link JsonSerializer} to render {@link EntityModel}-Lists in HAL compatible JSON. Renders the list as a
	 * Map.
	 *
	 * @author Alexander Baetz
	 * @author Oliver Gierke
	 */
	public static class HalResourcesSerializer extends ContainerSerializer<Collection<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = 8030706944344625390L;

		private final BeanProperty property;
		private final EmbeddedMapper embeddedMapper;

		public HalResourcesSerializer(EmbeddedMapper embeddedMapper) {
			this(null, embeddedMapper);
		}

		public HalResourcesSerializer(@Nullable BeanProperty property, EmbeddedMapper embeddedMapper) {

			super(TypeFactory.defaultInstance().constructType(Collection.class));

			this.property = property;
			this.embeddedMapper = embeddedMapper;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.codehaus.jackson.map.ser.std.SerializerBase#serialize(java.lang.Object, org.codehaus.jackson.JsonGenerator,
		 * org.codehaus.jackson.map.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(Collection<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

			Map<HalLinkRelation, Object> embeddeds = embeddedMapper.map(value);

			Object currentValue = jgen.getCurrentValue();

			if (currentValue instanceof RepresentationModel) {

				if (embeddedMapper.hasCuriedEmbed(value)) {
					((RepresentationModel<?>) currentValue).add(CURIES_REQUIRED_DUE_TO_EMBEDS);
				}
			}

			provider.findValueSerializer(Map.class, property).serialize(embeddeds, jgen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new HalResourcesSerializer(property, embeddedMapper);
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
		public boolean isEmpty(SerializerProvider provider, Collection<?> value) {
			return value.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(Collection<?> value) {
			return value.size() == 1;
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

	/**
	 * Custom {@link JsonSerializer} to render Link instances in HAL compatible JSON. Renders the {@link Link} as
	 * immediate object if we have a single one or as array if we have multiple ones.
	 *
	 * @author Alexander Baetz
	 * @author Oliver Gierke
	 */
	public static class OptionalListJackson2Serializer extends ContainerSerializer<Object>
			implements ContextualSerializer {

		private static final long serialVersionUID = 3700806118177419817L;

		private final @Nullable BeanProperty property;
		private final Map<Class<?>, JsonSerializer<Object>> serializers;
		private final HalConfiguration halConfiguration;

		/**
		 * Creates a new {@link OptionalListJackson2Serializer} using the given {@link BeanProperty}.
		 *
		 * @param property
		 */
		public OptionalListJackson2Serializer(@Nullable BeanProperty property, HalConfiguration halConfiguration) {

			super(TypeFactory.defaultInstance().constructType(List.class));

			this.property = property;
			this.serializers = new HashMap<>();
			this.halConfiguration = halConfiguration;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@SuppressWarnings("null")
		public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			throw new UnsupportedOperationException("not implemented");
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

			List<?> list = (List<?>) value;

			if (list.isEmpty()) {
				return;
			}

			Object firstElement = list.get(0);

			if (!HalLink.class.isInstance(firstElement)) {
				serializeContents(list, jgen, provider);
				return;
			}

			HalLink halLink = (HalLink) firstElement;

			if (list.size() == 1 && halConfiguration.getSingleLinkRenderModeFor(halLink.getLink().getRel())
					.equals(RenderSingleLinks.AS_SINGLE)) {

				serializeContents(halLink, jgen, provider);

				return;
			}

			serializeContents(list, jgen, provider);
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
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentType()
		 */
		@Override
		@Nullable
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean hasSingleElement(Object arg0) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(com.fasterxml.jackson.databind.SerializerProvider, java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean isEmpty(SerializerProvider provider, Object value) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider,
		 * com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
				throws JsonMappingException {
			return new OptionalListJackson2Serializer(property, halConfiguration);
		}

		private void serializeContents(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			getOrLookupSerializerFor(value, provider).serialize(value, jgen, provider);
		}

		private JsonSerializer<Object> getOrLookupSerializerFor(Object value, SerializerProvider provider)
				throws JsonMappingException {

			Class<?> type = value.getClass();
			JsonSerializer<Object> serializer = serializers.get(type);

			if (serializer == null) {
				serializer = provider.findValueSerializer(type, property);
				serializers.put(type, serializer);
			}

			return serializer;
		}
	}

	public static class HalLinkListDeserializer extends ContainerDeserializerBase<List<Link>> {

		private static final long serialVersionUID = 6420432361123210955L;

		public HalLinkListDeserializer() {
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
		public List<Link> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			List<Link> result = new ArrayList<>();
			String relation;
			Link link;

			// links is an object, so we parse till we find its end.
			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

				if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
					throw new JsonParseException(jp, "Expected relation name");
				}

				// save the relation in case the link does not contain it
				relation = jp.getText();

				if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
					while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
						link = jp.readValueAs(Link.class);
						result.add(new Link(link.getHref(), relation).withHreflang(link.getHreflang()).withTitle(link.getTitle())
								.withType(link.getType()).withDeprecation(link.getDeprecation()));
					}
				} else {
					link = jp.readValueAs(Link.class);
					result.add(new Link(link.getHref(), relation).withHreflang(link.getHreflang()).withTitle(link.getTitle())
							.withType(link.getType()).withDeprecation(link.getDeprecation()));
				}
			}

			return result;
		}
	}

	public static class HalResourcesDeserializer extends ContainerDeserializerBase<List<Object>>
			implements ContextualDeserializer {

		private static final long serialVersionUID = 4755806754621032622L;

		private JavaType contentType;

		public HalResourcesDeserializer() {
			this(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Object.class), null);
		}

		public HalResourcesDeserializer(JavaType vc) {
			this(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, vc), vc);
		}

		private HalResourcesDeserializer(JavaType type, @Nullable JavaType contentType) {

			super(type);
			this.contentType = contentType;
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
		 * @see com.fasterxml.jackson.databind.deser.ContextualDeserializer#createContextual(com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property)
				throws JsonMappingException {

			JavaType type = property.getType().getContentType();

			return new HalResourcesDeserializer(type);
		}
	}

	/**
	 * HandlerInstantiator to create HAL-specific serializers, deserializers etc.
	 *
	 * @author Oliver Gierke
	 */
	public static class HalHandlerInstantiator extends HandlerInstantiator {

		private final Map<Class<?>, Object> serializers = new HashMap<>();
		private final @Nullable AutowireCapableBeanFactory delegate;

		public HalHandlerInstantiator(LinkRelationProvider provider, CurieProvider curieProvider,
				MessageResolver resolver) {
			this(provider, curieProvider, resolver, new HalConfiguration());
		}

		/**
		 * Creates a new {@link HalHandlerInstantiator} using the given {@link LinkRelationProvider}, {@link CurieProvider}
		 * and {@link MessageResolver}. Registers a prepared {@link HalResourcesSerializer} and
		 * {@link HalLinkListSerializer} falling back to instantiation expecting a default constructor.
		 *
		 * @param provider must not be {@literal null}.
		 * @param curieProvider can be {@literal null}.
		 * @param resolver must not be {@literal null}.
		 */
		public HalHandlerInstantiator(LinkRelationProvider provider, CurieProvider curieProvider, MessageResolver resolver,
				HalConfiguration halConfiguration) {
			this(provider, curieProvider, resolver, true, halConfiguration);
		}

		/**
		 * Creates a new {@link HalHandlerInstantiator} using the given {@link LinkRelationProvider}, {@link CurieProvider}
		 * and {@link MessageResolver} and whether to enforce embedded collections. Registers a prepared
		 * {@link HalResourcesSerializer} and {@link HalLinkListSerializer} falling back to instantiation expecting a
		 * default constructor.
		 *
		 * @param provider must not be {@literal null}.
		 * @param curieProvider can be {@literal null}
		 * @param resolver must not be {@literal null}..
		 * @param enforceEmbeddedCollections
		 */
		public HalHandlerInstantiator(LinkRelationProvider provider, CurieProvider curieProvider, MessageResolver resolver,
				boolean enforceEmbeddedCollections, HalConfiguration halConfiguration) {
			this(provider, curieProvider, resolver, enforceEmbeddedCollections, null, halConfiguration);
		}

		private HalHandlerInstantiator(LinkRelationProvider provider, CurieProvider curieProvider, MessageResolver resolver,
				boolean enforceEmbeddedCollections, @Nullable AutowireCapableBeanFactory delegate,
				HalConfiguration halConfiguration) {

			Assert.notNull(provider, "RelProvider must not be null!");
			Assert.notNull(curieProvider, "CurieProvider must not be null!");

			EmbeddedMapper mapper = new EmbeddedMapper(provider, curieProvider, enforceEmbeddedCollections);

			this.delegate = delegate;

			this.serializers.put(HalResourcesSerializer.class, new HalResourcesSerializer(mapper));
			this.serializers.put(HalLinkListSerializer.class,
					new HalLinkListSerializer(curieProvider, mapper, resolver, halConfiguration));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#deserializerInstance(com.fasterxml.jackson.databind.DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonDeserializer<?> deserializerInstance(@NonNull DeserializationConfig config, @NonNull Annotated annotated,
				@NonNull Class<?> deserClass) {
			return (JsonDeserializer<?>) findInstance(deserClass);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#keyDeserializerInstance(com.fasterxml.jackson.databind.DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		@SuppressWarnings("null")
		public KeyDeserializer keyDeserializerInstance(@NonNull DeserializationConfig config, @NonNull Annotated annotated,
				@NonNull Class<?> keyDeserClass) {
			return (KeyDeserializer) findInstance(keyDeserClass);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#serializerInstance(com.fasterxml.jackson.databind.SerializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonSerializer<?> serializerInstance(@NonNull SerializationConfig config, @NonNull Annotated annotated,
				@NonNull Class<?> serClass) {
			return (JsonSerializer<?>) findInstance(serClass);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#typeResolverBuilderInstance(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		@SuppressWarnings("null")
		public TypeResolverBuilder<?> typeResolverBuilderInstance(@NonNull MapperConfig<?> config,
				@NonNull Annotated annotated, @NonNull Class<?> builderClass) {
			return (TypeResolverBuilder<?>) findInstance(builderClass);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#typeIdResolverInstance(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		@SuppressWarnings("null")
		public TypeIdResolver typeIdResolverInstance(@NonNull MapperConfig<?> config, @NonNull Annotated annotated,
				@NonNull Class<?> resolverClass) {
			return (TypeIdResolver) findInstance(resolverClass);
		}

		private Object findInstance(Class<?> type) {

			Object result = serializers.get(type);

			return result != null ? result : delegate != null ? delegate.createBean(type) : BeanUtils.instantiateClass(type);
		}
	}

	/**
	 * {@link JsonSerializer} to only render {@link Boolean} values if they're set to {@literal true}.
	 *
	 * @author Oliver Gierke
	 * @since 0.9
	 */
	public static class TrueOnlyBooleanSerializer extends StdScalarSerializer<Boolean> {

		private static final long serialVersionUID = 5817795880782727569L;

		public TrueOnlyBooleanSerializer() {
			super(Boolean.class);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(com.fasterxml.jackson.databind.SerializerProvider, java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public boolean isEmpty(SerializerProvider provider, Boolean value) {
			return value == null || Boolean.FALSE.equals(value);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings("null")
		public void serialize(Boolean value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeBoolean(value);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdScalarSerializer#getSchema(com.fasterxml.jackson.databind.SerializerProvider, java.lang.reflect.Type)
		 */
		@Override
		@SuppressWarnings("null")
		public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
			return createSchemaNode("boolean", true);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdScalarSerializer#acceptJsonFormatVisitor(com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper, com.fasterxml.jackson.databind.JavaType)
		 */
		@Override
		@SuppressWarnings("null")
		public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint)
				throws JsonMappingException {

			if (visitor != null) {
				visitor.expectBooleanFormat(typeHint);
			}
		}
	}

	/**
	 * Helper to easily map embedded resources and find out whether they were curied.
	 *
	 * @author Oliver Gierke
	 */
	public static class EmbeddedMapper {

		private LinkRelationProvider relProvider;
		private CurieProvider curieProvider;
		private boolean preferCollectionRels;

		/**
		 * Creates a new {@link EmbeddedMapper} for the given {@link LinkRelationProvider}, {@link CurieProvider} and flag
		 * whether to prefer collection relations.
		 *
		 * @param relProvider must not be {@literal null}.
		 * @param curieProvider must not be {@literal null}.
		 * @param preferCollectionRels
		 */
		public EmbeddedMapper(LinkRelationProvider relProvider, CurieProvider curieProvider, boolean preferCollectionRels) {

			Assert.notNull(relProvider, "RelProvider must not be null!");

			this.relProvider = relProvider;
			this.curieProvider = curieProvider;
			this.preferCollectionRels = preferCollectionRels;
		}

		/**
		 * Maps the given source elements as embedded values.
		 *
		 * @param source must not be {@literal null}.
		 * @return
		 */
		public Map<HalLinkRelation, Object> map(Iterable<?> source) {

			Assert.notNull(source, "Elements must not be null!");

			HalEmbeddedBuilder builder = new HalEmbeddedBuilder(relProvider, curieProvider, preferCollectionRels);

			source.forEach(builder::add);

			return builder.asMap();
		}

		/**
		 * Returns whether the given source elements will be namespaced.
		 *
		 * @param source must not be {@literal null}.
		 * @return
		 */
		public boolean hasCuriedEmbed(Iterable<?> source) {

			return map(source).keySet().stream() //
					.anyMatch(HalLinkRelation::isCuried);
		}
	}

	static class HalLink {

		private final Link link;
		private final String title;

		public HalLink(Link link, @Nullable String title) {

			this.link = link;
			this.title = title;
		}

		@JsonUnwrapped
		public Link getLink() {
			return link;
		}

		@Nullable
		@JsonInclude(Include.NON_EMPTY)
		public String getTitle() {
			return title;
		}
	}
}
