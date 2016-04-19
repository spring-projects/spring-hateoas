package org.springframework.hateoas.hal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.forms.Template;
import org.springframework.hateoas.forms.ValueSuggestSerializer;
import org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.hal.Jackson2HalModule.OptionalListJackson2Serializer;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class Jackson2HalFormsModule extends SimpleModule {

	private static final long serialVersionUID = -4496351128468451196L;
	private static final Link CURIES_REQUIRED_DUE_TO_EMBEDS = new Link("__rel__", "¯\\_(ツ)_/¯");

	public Jackson2HalFormsModule() {
		super("json-hal-forms-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));
	}

	public static class HalTemplateListSerializer extends ContainerSerializer<List<Template>>
			implements ContextualSerializer {

		private static final String RELATION_MESSAGE_TEMPLATE = "_templates.%s.title";

		private final BeanProperty property;

		private final EmbeddedMapper mapper;

		private final MessageSourceAccessor messageSource;

		public HalTemplateListSerializer(EmbeddedMapper mapper, MessageSourceAccessor messageSource) {
			this(null, mapper, messageSource);
		}

		public HalTemplateListSerializer(BeanProperty property, EmbeddedMapper mapper,
				MessageSourceAccessor messageSource) {

			super(List.class, false);
			this.property = property;
			this.mapper = mapper;
			this.messageSource = messageSource;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object,
		 * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(List<Template> value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonGenerationException {

			// sort templates according to their relation
			Map<String, List<Object>> sortedTemplates = new LinkedHashMap<String, List<Object>>();

			for (Template template : value) {
				if (sortedTemplates.get(template.getRel()) == null) {
					sortedTemplates.put(template.getRel(), new ArrayList<Object>());
				}
				sortedTemplates.get(template.getRel()).add(toHalTemplate(template));
			}

			TypeFactory typeFactory = provider.getConfig().getTypeFactory();
			JavaType keyType = typeFactory.uncheckedSimpleType(String.class);
			JavaType valueType = typeFactory.constructCollectionType(ArrayList.class, Object.class);
			JavaType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);

			MapSerializer serializer = MapSerializer.construct(new String[] {}, mapType, true, null,
					provider.findKeySerializer(keyType, null), new OptionalListJackson2Serializer(property), null);

			serializer.serialize(sortedTemplates, jgen, provider);
		}

		/**
		 * Wraps the given link into a HAL specifc extension.
		 * 
		 * @param template must not be {@literal null}.
		 * @return
		 */
		private HalTemplate toHalTemplate(Template template) {

			String rel = template.getRel();
			String title = getTitle(rel);

			if (title == null) {
				title = getTitle(rel.contains(":") ? rel.substring(rel.indexOf(":") + 1) : rel);
			}

			return new HalTemplate(template, title);
		}

		/**
		 * Returns the title for the given local link relation resolved through the configured
		 * {@link MessageSourceAccessor} .
		 * 
		 * @param localRel must not be {@literal null} or empty.
		 * @return
		 */
		private String getTitle(String localRel) {

			Assert.hasText(localRel, "Local relation must not be null or empty!");

			try {
				return messageSource == null ? null
						: messageSource.getMessage(String.format(RELATION_MESSAGE_TEMPLATE, localRel));
			}
			catch (NoSuchMessageException o_O) {
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.
		 * SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
				throws JsonMappingException {
			return new HalTemplateListSerializer(property, mapper, messageSource);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentType()
		 */
		@Override
		public JavaType getContentType() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#getContentSerializer()
		 */
		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#isEmpty(java.lang.Object)
		 */
		public boolean isEmpty(List<Template> value) {
			return isEmpty(null, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(com.fasterxml.jackson.databind.SerializerProvider,
		 * java.lang.Object)
		 */
		public boolean isEmpty(SerializerProvider provider, List<Template> value) {
			return value.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#hasSingleElement(java.lang.Object)
		 */
		@Override
		public boolean hasSingleElement(List<Template> value) {
			return value.size() == 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.ser.ContainerSerializer#_withValueTypeSerializer(com.fasterxml.jackson.
		 * databind.jsontype.TypeSerializer)
		 */
		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class HalTemplate {

		private final Template template;

		private final String title;

		public HalTemplate(Template template, String title) {
			this.template = template;
			this.title = title;
		}

		@JsonUnwrapped
		public Link getTemplate() {
			return template;
		}

		@JsonInclude(Include.NON_NULL)
		public String getTitle() {
			return title;
		}
	}
	
	public static class HalEmbeddedResourcesSerializer extends ContainerSerializer<Iterable<?>>implements ContextualSerializer {

		private final BeanProperty property;
		private final EmbeddedMapper embeddedMapper;

		public HalEmbeddedResourcesSerializer(EmbeddedMapper embeddedMapper) {
			this(null, embeddedMapper);
		}

		public HalEmbeddedResourcesSerializer(BeanProperty property, EmbeddedMapper embeddedMapper) {

			super(Collection.class, false);

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
		public void serialize(Iterable<?> value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonGenerationException {

			Map<String, Object> embeddeds = embeddedMapper.map(value);

			Object currentValue = jgen.getCurrentValue();

			if (currentValue instanceof ResourceSupport) {

				if (embeddedMapper.hasCuriedEmbed(value)) {
					((ResourceSupport) currentValue).add(CURIES_REQUIRED_DUE_TO_EMBEDS);
				}
			}

			provider.findValueSerializer(Map.class, property).serialize(embeddeds, jgen, provider);
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new HalEmbeddedResourcesSerializer(property, embeddedMapper);
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		public boolean isEmpty(Collection<?> value) {
			return isEmpty(null, value);
		}

		public boolean isEmpty(SerializerProvider provider, Collection<?> value) {
			return value.isEmpty();
		}

		@Override
		public boolean hasSingleElement(Iterable<?> value) {
			return value.iterator().hasNext();
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	public static class HalFormsHandlerInstantiator extends HalHandlerInstantiator {
		private final Map<Class<?>, Object> instanceMap = new HashMap<Class<?>, Object>();

		public HalFormsHandlerInstantiator(RelProvider resolver, CurieProvider curieProvider,
				MessageSourceAccessor messageSource, boolean enforceEmbeddedCollections) {
			super(resolver, curieProvider, messageSource, enforceEmbeddedCollections);

			EmbeddedMapper mapper = new EmbeddedMapper(resolver, curieProvider, enforceEmbeddedCollections);

			this.instanceMap.put(HalTemplateListSerializer.class, new HalTemplateListSerializer(mapper, messageSource));
			this.instanceMap.put(ValueSuggestSerializer.class, new ValueSuggestSerializer(resolver, null));
			this.instanceMap.put(HalEmbeddedResourcesSerializer.class, new HalEmbeddedResourcesSerializer(mapper));
		}

		private Object findInstance(Class<?> type) {
			return instanceMap.get(type);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.fasterxml.jackson.databind.cfg.HandlerInstantiator#deserializerInstance(com.fasterxml.jackson.databind.
		 * DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
				Class<?> deserClass) {
			Object jsonDeser = findInstance(deserClass);
			return jsonDeser != null ? (JsonDeserializer<?>) jsonDeser
					: super.deserializerInstance(config, annotated, deserClass);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.cfg.HandlerInstantiator#keyDeserializerInstance(com.fasterxml.jackson.
		 * databind. DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated,
				Class<?> keyDeserClass) {
			Object keyDeser = findInstance(keyDeserClass);
			return keyDeser != null ? (KeyDeserializer) keyDeser
					: super.keyDeserializerInstance(config, annotated, keyDeserClass);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.fasterxml.jackson.databind.cfg.HandlerInstantiator#serializerInstance(com.fasterxml.jackson.databind.
		 * SerializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated,
				Class<?> serClass) {
			Object jsonSer = findInstance(serClass);
			return jsonSer != null ? (JsonSerializer<?>) jsonSer
					: super.serializerInstance(config, annotated, serClass);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.fasterxml.jackson.databind.cfg.HandlerInstantiator#typeResolverBuilderInstance(com.fasterxml.jackson.
		 * databind .cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated,
				Class<?> builderClass) {
			Object builder = findInstance(builderClass);
			return builder != null ? (TypeResolverBuilder<?>) builder
					: super.typeResolverBuilderInstance(config, annotated, builderClass);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.fasterxml.jackson.databind.cfg.HandlerInstantiator#typeIdResolverInstance(com.fasterxml.jackson.databind.
		 * cfg. MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated,
				Class<?> resolverClass) {
			Object resolver = findInstance(resolverClass);
			return resolver != null ? (TypeIdResolver) resolver
					: super.typeIdResolverInstance(config, annotated, resolverClass);
		}
	}

	private static class EmbeddedMapper {

		private RelProvider relProvider;

		private CurieProvider curieProvider;

		private boolean preferCollectionRels;

		/**
		 * Creates a new {@link EmbeddedMapper} for the given {@link RelProvider}, {@link CurieProvider} and flag
		 * whether to prefer collection relations.
		 * 
		 * @param relProvider must not be {@literal null}.
		 * @param curieProvider can be {@literal null}.
		 * @param preferCollectionRels
		 */
		public EmbeddedMapper(RelProvider relProvider, CurieProvider curieProvider, boolean preferCollectionRels) {

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
		public Map<String, Object> map(Iterable<?> source) {

			Assert.notNull(source, "Elements must not be null!");

			HalEmbeddedBuilder builder = new HalEmbeddedBuilder(relProvider, curieProvider, preferCollectionRels);

			for (Object resource : source) {
				builder.add(resource);
			}

			return builder.asMap();
		}

		/**
		 * Returns whether the given source elements will be namespaced.
		 * 
		 * @param source must not be {@literal null}.
		 * @return
		 */
		public boolean hasCuriedEmbed(Iterable<?> source) {

			for (String rel : map(source).keySet()) {
				if (rel.contains(":")) {
					return true;
				}
			}

			return false;
		}
	}

}
