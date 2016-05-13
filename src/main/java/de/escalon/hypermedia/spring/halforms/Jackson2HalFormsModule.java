package de.escalon.hypermedia.spring.halforms;

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
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule.EmbeddedMapper;
import org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListSerializer;
import org.springframework.hateoas.hal.Jackson2HalModule.OptionalListJackson2Serializer;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
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

	public Jackson2HalFormsModule() {
		super("json-hal-forms-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));
	}

	public static class HalTemplateListSerializer extends ContainerSerializer<List<Template>>
			implements ContextualSerializer {

		private static final long serialVersionUID = 1L;

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
		 * Returns the title for the given local link relation resolved through the configured {@link MessageSourceAccessor}
		 * .
		 * 
		 * @param localRel must not be {@literal null} or empty.
		 * @return
		 */
		private String getTitle(String localRel) {

			Assert.hasText(localRel, "Local relation must not be null or empty!");

			try {
				return messageSource == null ? null
						: messageSource.getMessage(String.format(RELATION_MESSAGE_TEMPLATE, localRel));
			} catch (NoSuchMessageException o_O) {
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
		@Override
		public boolean isEmpty(List<Template> value) {
			return isEmpty(null, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.fasterxml.jackson.databind.JsonSerializer#isEmpty(com.fasterxml.jackson.databind.SerializerProvider,
		 * java.lang.Object)
		 */
		@Override
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

	public static class HalTemplateListDeserializer extends ContainerDeserializerBase<List<Template>> {

		protected HalTemplateListDeserializer() {
			super(List.class);
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		@Override
		public List<Template> deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			List<Template> result = new ArrayList<Template>();

			String key;
			Template template;
			while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {
				if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
					throw new JsonParseException("Expected relation name", jp.getCurrentLocation());
				}

				// save the relation in case the link does not contain it
				key = jp.getText();

				if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
					while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
						template = jp.readValueAs(Template.class);
						result.add(new Template(template.getHref(), key));
					}
				} else {
					template = jp.readValueAs(Template.class);
					result.add(new Template("http://localhost", key));
				}
			}
			return result;
		}

	}

	public static class HalEmbeddedResourcesSerializer extends ContainerSerializer<Collection<?>>
			implements ContextualSerializer {

		private static final long serialVersionUID = 1L;
		private final BeanProperty property;
		private final EmbeddedMapper embeddedMapper;

		public HalEmbeddedResourcesSerializer(EmbeddedMapper embeddedMapper) {
			this(null, embeddedMapper);
		}

		public HalEmbeddedResourcesSerializer(BeanProperty property, EmbeddedMapper embeddedMapper) {
			super(Collection.class, false);
			this.embeddedMapper = embeddedMapper;
			this.property = property;
		}

		@Override
		public void serialize(Collection<?> value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonGenerationException {
			Map<String, Object> embeddeds = embeddedMapper.map(value);

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

		@Override
		public boolean hasSingleElement(Collection<?> value) {
			return value.size() == 1;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		@Override
		public boolean isEmpty(Collection<?> value) {
			return isEmpty(null, value);
		}

		@Override
		public boolean isEmpty(SerializerProvider provider, Collection<?> value) {
			return value.isEmpty();
		}

	}

	public static class HalFormsLinkLinkSerializer extends HalLinkListSerializer {

		private static final long serialVersionUID = 1L;

		private static final Link CURIES_REQUIRED_DUE_TO_EMBEDS = new Link("__rel__", "¯\\_(ツ)_/¯");
		private static final String RELATION_MESSAGE_TEMPLATE = "_links.%s.title";

		private final BeanProperty property;
		private final CurieProvider curieProvider;
		private final EmbeddedMapper mapper;
		private final MessageSourceAccessor messageSource;

		public HalFormsLinkLinkSerializer(CurieProvider curieProvider, EmbeddedMapper mapper,
				MessageSourceAccessor messageSource) {
			this(null, curieProvider, mapper, messageSource);
		}

		public HalFormsLinkLinkSerializer(BeanProperty property, CurieProvider curieProvider, EmbeddedMapper mapper,
				MessageSourceAccessor messageSource) {
			super(property, curieProvider, mapper, messageSource);
			this.property = property;
			this.curieProvider = curieProvider;
			this.mapper = mapper;
			this.messageSource = messageSource;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(List<Link> value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonGenerationException {

			// sort links according to their relation
			Map<String, List<Object>> sortedLinks = new LinkedHashMap<String, List<Object>>();
			List<Link> links = new ArrayList<Link>();

			boolean prefixingRequired = curieProvider != null;
			boolean curiedLinkPresent = false;
			boolean skipCuries = !jgen.getOutputContext().getParent().inRoot();

			Object currentValue = jgen.getCurrentValue();

			if (currentValue instanceof Resources) {
				if (mapper.hasCuriedEmbed((Resources<?>) currentValue)) {
					curiedLinkPresent = true;
				}
			} else if (currentValue instanceof HalFormsDocument) {
				if (mapper.hasCuriedEmbed(((HalFormsDocument) currentValue).getEmbeddeds())) {
					curiedLinkPresent = true;
				}
			}

			for (Link link : value) {

				if (link.equals(CURIES_REQUIRED_DUE_TO_EMBEDS)) {
					continue;
				}

				String rel = prefixingRequired ? curieProvider.getNamespacedRelFrom(link) : link.getRel();

				if (!link.getRel().equals(rel)) {
					curiedLinkPresent = true;
				}

				if (sortedLinks.get(rel) == null) {
					sortedLinks.put(rel, new ArrayList<Object>());
				}

				links.add(link);

				sortedLinks.get(rel).add(toHalLink(link));
			}

			if (!skipCuries && prefixingRequired && curiedLinkPresent) {

				ArrayList<Object> curies = new ArrayList<Object>();
				curies.add(curieProvider.getCurieInformation(new Links(links)));

				sortedLinks.put("curies", curies);
			}

			TypeFactory typeFactory = provider.getConfig().getTypeFactory();
			JavaType keyType = typeFactory.uncheckedSimpleType(String.class);
			JavaType valueType = typeFactory.constructCollectionType(ArrayList.class, Object.class);
			JavaType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);

			MapSerializer serializer = MapSerializer.construct(new String[] {}, mapType, true, null,
					provider.findKeySerializer(keyType, null), new OptionalListJackson2Serializer(property), null);

			serializer.serialize(sortedLinks, jgen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
				throws JsonMappingException {
			return new HalFormsLinkLinkSerializer(property, curieProvider, mapper, messageSource);
		}

		/**
		 * Wraps the given link into a HAL specifc extension.
		 * 
		 * @param link must not be {@literal null}.
		 * @return
		 */
		private HalLink toHalLink(Link link) {

			String rel = link.getRel();
			String title = getTitle(rel);

			if (title == null) {
				title = getTitle(rel.contains(":") ? rel.substring(rel.indexOf(":") + 1) : rel);
			}

			return new HalLink(link, title);
		}

		/**
		 * Returns the title for the given local link relation resolved through the configured {@link MessageSourceAccessor}
		 * .
		 * 
		 * @param localRel must not be {@literal null} or empty.
		 * @return
		 */
		private String getTitle(String localRel) {

			Assert.hasText(localRel, "Local relation must not be null or empty!");

			try {
				return messageSource == null ? null
						: messageSource.getMessage(String.format(RELATION_MESSAGE_TEMPLATE, localRel));
			} catch (NoSuchMessageException o_O) {
				return null;
			}
		}
	}

	static class HalLink {

		private final Link link;
		private final String title;

		public HalLink(Link link, String title) {
			this.link = link;
			this.title = title;
		}

		@JsonUnwrapped
		public Link getLink() {
			return link;
		}

		@JsonInclude(Include.NON_NULL)
		public String getTitle() {
			return title;
		}
	}

	public static class HalFormsHandlerInstantiator extends HalHandlerInstantiator {
		private final Map<Class<?>, Object> instanceMap = new HashMap<Class<?>, Object>();

		public HalFormsHandlerInstantiator(RelProvider resolver, CurieProvider curieProvider,
				MessageSourceAccessor messageSource, boolean enforceEmbeddedCollections) {
			super(resolver, curieProvider, messageSource, enforceEmbeddedCollections);

			EmbeddedMapper mapper = new EmbeddedMapper(resolver, curieProvider, enforceEmbeddedCollections);

			instanceMap.put(HalTemplateListSerializer.class, new HalTemplateListSerializer(mapper, messageSource));
			instanceMap.put(ValueSuggestSerializer.class, new ValueSuggestSerializer(mapper, resolver, null));
			instanceMap.put(HalEmbeddedResourcesSerializer.class, new HalEmbeddedResourcesSerializer(mapper));
			instanceMap.put(HalFormsLinkLinkSerializer.class,
					new HalFormsLinkLinkSerializer(curieProvider, mapper, messageSource));
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
		public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
			Object jsonSer = findInstance(serClass);
			return jsonSer != null ? (JsonSerializer<?>) jsonSer : super.serializerInstance(config, annotated, serClass);
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
		public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
			Object resolver = findInstance(resolverClass);
			return resolver != null ? (TypeIdResolver) resolver
					: super.typeIdResolverInstance(config, annotated, resolverClass);
		}
	}
}
