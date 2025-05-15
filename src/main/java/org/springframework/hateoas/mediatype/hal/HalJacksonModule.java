/*
 * Copyright 2012-2024 the original author or authors.
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

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.Version;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.PropertyNamingStrategies.NamingBase;
import tools.jackson.databind.PropertyNamingStrategy;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.std.ContainerDeserializerBase;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.jdk.MapSerializer;
import tools.jackson.databind.ser.std.StdContainerSerializer;
import tools.jackson.databind.ser.std.StdScalarSerializer;
import tools.jackson.databind.type.TypeFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.ConfigurableHandlerInstantiator;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.MessageSourceResolvableSerializer;
import org.springframework.hateoas.mediatype.hal.HalConfiguration.RenderSingleLinks;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Jackson 2 module implementation to render {@link Link} and {@link RepresentationModel} instances in HAL compatible
 * JSON.
 *
 * @author Alexander Baetz
 * @author Oliver Gierke
 * @author Jeffrey Walraven
 */
public class HalJacksonModule extends SimpleModule {

	private static final long serialVersionUID = 7806951456457932384L;
	private static final Link CURIES_REQUIRED_DUE_TO_EMBEDS = Link.of("__rel__", "¯\\_(ツ)_/¯");
	private static final TypeFactory TYPE_FACTORY = TypeFactory.createDefaultInstance();

	public HalJacksonModule() {

		super("json-hal-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(RepresentationModel.class, RepresentationModelMixin.class);
		setMixInAnnotation(CollectionModel.class, CollectionModelMixin.class);
	}

	/**
	 * Returns whether the module was already registered in the given {@link JsonMapper}.
	 *
	 * @param mapper must not be {@literal null}.
	 * @return
	 */
	public static boolean isAlreadyRegisteredIn(JsonMapper mapper) {

		Assert.notNull(mapper, "JsonMapper must not be null!");

		return LinkMixin.class.equals(mapper.serializationConfig().findMixInClassFor(Link.class));
	}

	/**
	 * Custom {@link ValueSerializer} to render Link instances in HAL compatible JSON.
	 *
	 * @author Alexander Baetz
	 * @author Oliver Gierke
	 */
	public static class HalLinkListSerializer extends StdContainerSerializer<Links> {

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

			super(Links.class);

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
		 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
		 */
		@Override
		public void serialize(Links value, JsonGenerator jgen, SerializationContext provider) {

			// sort links according to their relation
			MultiValueMap<String, Object> sortedLinks = new LinkedMultiValueMap<>();
			List<Link> links = new ArrayList<>();

			boolean prefixingRequired = curieProvider != CurieProvider.NONE;
			boolean curiedLinkPresent = false;

			boolean skipCuries = !jgen.streamWriteContext().getParent().inRoot();

			Object currentValue = jgen.currentValue();

			PropertyNamingStrategy propertyNamingStrategy = provider.getConfig().getPropertyNamingStrategy();
			EmbeddedMapper transformingMapper = halConfiguration.isApplyPropertyNamingStrategy() //
					? mapper.with(propertyNamingStrategy)
					: mapper;

			if (currentValue instanceof CollectionModel
					&& transformingMapper.hasCuriedEmbed((CollectionModel<?>) currentValue)) {

				curiedLinkPresent = true;
			}

			for (Link link : value) {

				if (link.equals(CURIES_REQUIRED_DUE_TO_EMBEDS)) {
					continue;
				}

				LinkRelation rel = prefixingRequired ? curieProvider.getNamespacedRelFrom(link) : link.getRel();
				HalLinkRelation relation = transformingMapper.map(rel);

				if (relation.isCuried()) {
					curiedLinkPresent = true;
				}

				sortedLinks.add(relation.value(), toHalLink(link, relation));
				links.add(link);
			}

			if (!skipCuries && prefixingRequired && curiedLinkPresent) {

				Collection<?> curies = curieProvider.getCurieInformation(Links.of(links));

				if (!curies.isEmpty()) {
					sortedLinks.addAll(HalLinkRelation.CURIES.value(), new ArrayList<>(curies));
				}
			}

			TypeFactory typeFactory = provider.getConfig().getTypeFactory();
			JavaType keyType = typeFactory.constructType(String.class);
			JavaType valueType = typeFactory.constructCollectionType(ArrayList.class, Object.class);
			JavaType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);

			MapSerializer serializer = MapSerializer.construct(mapType, true, null,
					provider.findKeySerializer(keyType, null), new OptionalListJackson2Serializer(property, halConfiguration),
					null, Collections.emptySet(), null);

			serializer.serialize(sortedLinks, jgen, provider);
		}

		/**
		 * Wraps the given link into a HAL specific extension.
		 *
		 * @param link must not be {@literal null}.
		 * @return
		 */
		private HalLink toHalLink(Link link, HalLinkRelation rel) {
			return new HalLink(link, resolver.resolve(rel));
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueSerializer#createContextual(tools.jackson.databind.SerializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		public ValueSerializer<?> createContextual(SerializationContext provider, BeanProperty property) {
			return new HalLinkListSerializer(property, curieProvider, mapper, resolver, halConfiguration);
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
		public boolean isEmpty(SerializationContext provider, Links value) {
			return value == null || value.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		public boolean hasSingleElement(Links value) {
			return value.toList().size() == 1;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#_withValueTypeSerializer(tools.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		protected StdContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	/**
	 * Custom {@link ValueSerializer} to render {@link EntityModel}-Lists in HAL compatible JSON. Renders the list as a
	 * Map.
	 *
	 * @author Alexander Baetz
	 * @author Oliver Gierke
	 */
	public static class HalResourcesSerializer extends StdContainerSerializer<Collection<?>> {

		private final EmbeddedMapper embeddedMapper;
		private final HalConfiguration configuration;
		private final @Nullable BeanProperty property;

		public HalResourcesSerializer(EmbeddedMapper embeddedMapper, HalConfiguration configuration) {
			this(embeddedMapper, configuration, null);
		}

		public HalResourcesSerializer(EmbeddedMapper embeddedMapper, HalConfiguration configuration,
				@Nullable BeanProperty property) {

			super(Collection.class);

			this.embeddedMapper = embeddedMapper;
			this.configuration = configuration;
			this.property = property;
		}

		@Override
		public void serialize(Collection<?> value, JsonGenerator jgen, SerializationContext provider) {

			var mapper = configuration.isApplyPropertyNamingStrategy() //
					? embeddedMapper.with(provider.getConfig().getPropertyNamingStrategy()) //
					: embeddedMapper;

			var embeddeds = mapper.map(value);
			var currentValue = jgen.currentValue();

			if (currentValue instanceof RepresentationModel) {

				if (mapper.hasCuriedEmbed(value)) {
					((RepresentationModel<?>) currentValue).add(CURIES_REQUIRED_DUE_TO_EMBEDS);
				}
			}

			var map = new LinkedHashMap<>(embeddeds.size());
			embeddeds.forEach((key, it) -> map.put(key.value(), it));

			provider.findPrimaryPropertySerializer(Map.class, property) //
					.serialize(map, jgen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueSerializer#createContextual(tools.jackson.databind.SerializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) {
			return new HalResourcesSerializer(embeddedMapper, configuration, property);
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
		public boolean isEmpty(SerializationContext provider, Collection<?> value) {
			return value.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		public boolean hasSingleElement(Collection<?> value) {
			return value.size() == 1;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#_withValueTypeSerializer(tools.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		@Nullable
		protected StdContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	/**
	 * Custom {@link ValueSerializer} to render Link instances in HAL compatible JSON. Renders the {@link Link} as
	 * immediate object if we have a single one or as array if we have multiple ones.
	 *
	 * @author Alexander Baetz
	 * @author Oliver Gierke
	 */
	public static class OptionalListJackson2Serializer extends StdContainerSerializer<Object> {

		private final @Nullable BeanProperty property;
		private final Map<Class<?>, ValueSerializer<Object>> serializers;
		private final HalConfiguration halConfiguration;

		/**
		 * Creates a new {@link OptionalListJackson2Serializer} using the given {@link BeanProperty}.
		 *
		 * @param property
		 */
		public OptionalListJackson2Serializer(@Nullable BeanProperty property, HalConfiguration halConfiguration) {

			super(List.class);

			this.property = property;
			this.serializers = new HashMap<>();
			this.halConfiguration = halConfiguration;
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdContainerSerializer#_withValueTypeSerializer(tools.jackson.databind.jsontype.TypeSerializer)
		 */
		@Override
		public StdContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			throw new UnsupportedOperationException("not implemented");
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
		 */
		@Override
		public void serialize(Object value, JsonGenerator jgen, SerializationContext provider) {

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
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#getContentSerializer()
		 */
		@Override
		@Nullable
		public ValueSerializer<?> getContentSerializer() {
			return null;
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
		 *
		 * @see com.fasterxml.jackson.databind.ser.StdContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		public boolean hasSingleElement(Object arg0) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ValueSerializer#isEmpty(com.fasterxml.jackson.databind.SerializationContext, java.lang.Object)
		 */
		@Override
		public boolean isEmpty(SerializationContext provider, Object value) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializationContext,
		 * com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		public ValueSerializer<?> createContextual(SerializationContext provider, BeanProperty property) {
			return new OptionalListJackson2Serializer(property, halConfiguration);
		}

		private void serializeContents(Object value, JsonGenerator jgen, SerializationContext provider) {
			getOrLookupSerializerFor(value, provider).serialize(value, jgen, provider);
		}

		private ValueSerializer<Object> getOrLookupSerializerFor(Object value, SerializationContext context) {

			var type = value.getClass();
			var serializer = serializers.get(type);

			if (serializer == null) {
				serializer = context.findPrimaryPropertySerializer(type, property);
				serializers.put(type, serializer);
			}

			return serializer;
		}
	}

	public static class HalLinkListDeserializer extends ContainerDeserializerBase<List<Link>> {

		public HalLinkListDeserializer() {
			super(TYPE_FACTORY.constructCollectionLikeType(List.class, Link.class));
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
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
		public List<Link> deserialize(JsonParser jp, DeserializationContext ctxt) {

			List<Link> result = new ArrayList<>();

			// links is an object, so we parse till we find its end.
			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {

				if (!JsonToken.PROPERTY_NAME.equals(jp.currentToken())) {
					throw new StreamReadException(jp, "Expected relation name");
				}

				// save the relation in case the link does not contain it
				String relation = jp.getString();

				if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
					while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
						result.add(jp.readValueAs(Link.class).withRel(relation));
					}
				} else {
					result.add(jp.readValueAs(Link.class).withRel(relation));
				}
			}

			return result;
		}
	}

	public static class HalResourcesDeserializer extends ContainerDeserializerBase<List<Object>> {

		private @Nullable JavaType contentType;

		public HalResourcesDeserializer() {
			this(TYPE_FACTORY.constructCollectionLikeType(List.class, Object.class), null);
		}

		public HalResourcesDeserializer(JavaType vc) {
			this(TYPE_FACTORY.constructCollectionLikeType(List.class, vc), vc);
		}

		private HalResourcesDeserializer(JavaType type, @Nullable JavaType contentType) {

			super(type);

			this.contentType = contentType;
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
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
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
		 * @see com.fasterxml.jackson.databind.deser.ContextualDeserializer#createContextual(com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		public ValueDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {

			JavaType type = property.getType().getContentType();

			return new HalResourcesDeserializer(type);
		}
	}

	/**
	 * HandlerInstantiator to create HAL-specific serializers, deserializers etc.
	 *
	 * @author Oliver Gierke
	 */
	public static class HalHandlerInstantiator extends ConfigurableHandlerInstantiator {

		/**
		 * Convenience constructor for testing purposes. Prefer
		 * {@link #HalHandlerInstantiator(LinkRelationProvider, CurieProvider, MessageResolver, HalConfiguration, AutowireCapableBeanFactory)}
		 *
		 * @param provider must not be {@literal null}.
		 * @param curieProvider must not be {@literal null}.
		 * @param resolver must not be {@literal null}.
		 */
		public HalHandlerInstantiator(LinkRelationProvider provider, CurieProvider curieProvider,
				MessageResolver resolver) {
			this(provider, curieProvider, resolver, new HalConfiguration(), new DefaultListableBeanFactory());
		}

		/**
		 * @param provider must not be {@literal null}.
		 * @param curieProvider must not be {@literal null}.
		 * @param resolver must not be {@literal null}.
		 * @param halConfiguration must not be {@literal null}.
		 * @param delegate must not be {@literal null}.
		 */
		public HalHandlerInstantiator(LinkRelationProvider provider, CurieProvider curieProvider, MessageResolver resolver,
				HalConfiguration halConfiguration, AutowireCapableBeanFactory delegate) {

			super(delegate);

			Assert.notNull(provider, "RelProvider must not be null!");
			Assert.notNull(curieProvider, "CurieProvider must not be null!");

			EmbeddedMapper mapper = new EmbeddedMapper(provider, curieProvider,
					halConfiguration.isEnforceEmbeddedCollections());

			registerInstance(new HalResourcesSerializer(mapper, halConfiguration));
			registerInstance(new HalLinkListSerializer(curieProvider, mapper, resolver, halConfiguration));
			registerInstance(new MessageSourceResolvableSerializer(resolver));
		}
	}

	/**
	 * {@link ValueSerializer} to only render {@link Boolean} values if they're set to {@literal true}.
	 *
	 * @author Oliver Gierke
	 * @since 0.9
	 */
	public static class TrueOnlyBooleanSerializer extends StdScalarSerializer<Boolean> {

		public TrueOnlyBooleanSerializer() {
			super(Boolean.class);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ValueSerializer#isEmpty(com.fasterxml.jackson.databind.SerializationContext, java.lang.Object)
		 */
		@Override
		public boolean isEmpty(SerializationContext provider, Boolean value) {
			return value == null || Boolean.FALSE.equals(value);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
		 */
		@Override
		public void serialize(Boolean value, JsonGenerator jgen, SerializationContext provider) {
			jgen.writeBoolean(value);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ser.std.StdScalarSerializer#acceptJsonFormatVisitor(tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper, tools.jackson.databind.JavaType)
		 */
		@Override
		public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) {

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

		private static final Function<String, String> NO_OP = Function.identity();

		private final LinkRelationProvider relProvider;
		private final CurieProvider curieProvider;
		private final boolean preferCollectionRels;

		private Function<String, String> relationTransformer = Function.identity();

		public EmbeddedMapper(LinkRelationProvider relProvider, CurieProvider curieProvider, boolean preferCollectionRels) {

			Assert.notNull(relProvider, "relProvider must not be null!");

			this.relProvider = relProvider;
			this.curieProvider = curieProvider;
			this.preferCollectionRels = preferCollectionRels;
		}

		private EmbeddedMapper(LinkRelationProvider relProvider, CurieProvider curieProvider, boolean preferCollectionRels,
				Function<String, String> relationTransformer) {

			Assert.notNull(relProvider, "relProvider must not be null!");

			this.relProvider = relProvider;
			this.curieProvider = curieProvider;
			this.preferCollectionRels = preferCollectionRels;
			this.relationTransformer = relationTransformer;
		}

		/**
		 * Registers the given {@link PropertyNamingStrategy} with the current mapper to forward that strategy as relation
		 * transformer, so that {@link LinkRelation}s used as key for the embedding will be transformed using the given
		 * strategy.
		 *
		 * @param strategy must not be {@literal null}.
		 * @return an {@link EmbeddedMapper} applying the given strategy when mapping embedded objects.
		 */
		public EmbeddedMapper with(@Nullable PropertyNamingStrategy strategy) {

			Function<String, String> mapper = NamingBase.class.isInstance(strategy)
					? it -> Translator.translate(it, strategy)
					: Function.identity();

			return mapper == null
					? this
					: new EmbeddedMapper(relProvider, curieProvider, preferCollectionRels, mapper);
		}

		/**
		 * Maps the given source elements as embedded values.
		 *
		 * @param source must not be {@literal null}.
		 * @return
		 */
		public Map<HalLinkRelation, Object> map(Iterable<?> source) {

			Assert.notNull(source, "Elements must not be null!");

			HalEmbeddedBuilder builder = new HalEmbeddedBuilder(relProvider, curieProvider, preferCollectionRels) //
					.withRelationTransformer(relationTransformer);

			source.forEach(builder::add);

			return builder.asMap();
		}

		/**
		 * Maps the given {@link HalLinkRelation} using the underlying relation transformer.
		 *
		 * @param source must not be {@literal null}.
		 * @return
		 */
		public HalLinkRelation map(LinkRelation source) {

			Assert.notNull(source, "Link relation must not be null!");

			return HalLinkRelation.of(relationTransformer == NO_OP ? source : source.map(relationTransformer));
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

		private static class Translator {

			private static final Method METHOD;

			static {
				METHOD = Objects.requireNonNull(ReflectionUtils.findMethod(NamingBase.class, "translate", String.class));
				ReflectionUtils.makeAccessible(METHOD);
			}

			static String translate(String source, PropertyNamingStrategy strategy) {

				if (!NamingBase.class.isInstance(strategy)) {
					return source;
				}

				try {

					var result = METHOD.invoke(strategy, source);

					return (String) Objects.requireNonNull(result);

				} catch (Exception o_O) {
					throw new RuntimeException(o_O);
				}
			}
		}
	}

	static class HalLink {

		private final Link link;
		private final @Nullable String title;

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
		@JsonProperty
		public String getTitle() {
			return title;
		}
	}
}
