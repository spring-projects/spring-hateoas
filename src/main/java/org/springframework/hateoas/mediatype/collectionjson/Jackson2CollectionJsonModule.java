/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.mediatype.collectionjson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.support.JacksonHelper;
import org.springframework.hateoas.support.PropertyUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Jackson 2 module implementation to render {@link Resources}, {@link Resource}, and {@link ResourceSupport}
 * instances in Collection+JSON compatible JSON.
 *
 * @author Greg Turnquist
 */
public class Jackson2CollectionJsonModule extends SimpleModule {

	public Jackson2CollectionJsonModule() {

		super("collection-json-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

		setMixInAnnotation(ResourceSupport.class, ResourceSupportMixin.class);
		setMixInAnnotation(Resource.class, ResourceMixin.class);
		setMixInAnnotation(Resources.class, ResourcesMixin.class);
		setMixInAnnotation(PagedResources.class, PagedResourcesMixin.class);
	}

	/**
	 * Custom {@link JsonSerializer} to render Link instances in JSON Collection compatible JSON.
	 *
	 * @author Alexander Baetz
	 * @author Oliver Gierke
	 */
	static class CollectionJsonLinkListSerializer extends ContainerSerializer<List<Link>> implements ContextualSerializer {

		private final BeanProperty property;
		private final MessageSourceAccessor messageSource;

		CollectionJsonLinkListSerializer(MessageSourceAccessor messageSource) {
			this(null, messageSource);
		}

		CollectionJsonLinkListSerializer(BeanProperty property, MessageSourceAccessor messageSource) {

			super(List.class, false);
			this.property = property;
			this.messageSource = messageSource;
		}

		@Override
		public void serialize(List<Link> value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException {

			ResourceSupport resource = new ResourceSupport();
			resource.add(value);

			CollectionJson<?> collectionJson = new CollectionJson()
				.withVersion("1.0")
				.withHref(resource.getRequiredLink(Link.REL_SELF).expand().getHref())
				.withLinks(withoutSelfLink(value))
				.withItems(Collections.EMPTY_LIST);

			provider
				.findValueSerializer(CollectionJson.class, property)
				.serialize(collectionJson, jgen, provider);
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
			return new CollectionJsonLinkListSerializer(property, messageSource);
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
		public boolean isEmpty(List<Link> value) {
			return value.isEmpty();
		}

		@Override
		public boolean hasSingleElement(List<Link> value) {
			return value.size() == 1;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonResourceSupportSerializer extends ContainerSerializer<ResourceSupport> implements ContextualSerializer {

		private final BeanProperty property;

		CollectionJsonResourceSupportSerializer() {
			this(null);
		}

		CollectionJsonResourceSupportSerializer(BeanProperty property) {

			super(ResourceSupport.class, false);
			this.property = property;
		}

		@Override
		public void serialize(ResourceSupport value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

			String href = value.getRequiredLink(Link.REL_SELF).getHref();

			CollectionJson<?> collectionJson = new CollectionJson()
				.withVersion("1.0")
				.withHref(href)
				.withLinks(withoutSelfLink(value.getLinks()))
				.withQueries(findQueries(value))
				.withTemplate(findTemplate(value));

			CollectionJsonItem item = new CollectionJsonItem()
				.withHref(href)
				.withLinks(withoutSelfLink(value.getLinks()))
				.withRawData(value);

			if (!item.getData().isEmpty()) {
				collectionJson = collectionJson.withItems(Collections.singletonList(item));
			}

			CollectionJsonDocument<?> doc = new CollectionJsonDocument<>(collectionJson);

			provider
				.findValueSerializer(CollectionJsonDocument.class, property)
				.serialize(doc, jgen, provider);
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
			return new CollectionJsonResourceSupportSerializer(property);
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
		public boolean hasSingleElement(ResourceSupport value) {
			return true;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonResourceSerializer extends ContainerSerializer<Resource<?>> implements ContextualSerializer {

		private final BeanProperty property;

		CollectionJsonResourceSerializer() {
			this(null);
		}

		CollectionJsonResourceSerializer(BeanProperty property) {

			super(Resource.class, false);
			this.property = property;
		}

		@Override
		public void serialize(Resource<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

			String href = value.getRequiredLink(Link.REL_SELF).getHref();

			CollectionJson<?> collectionJson = new CollectionJson()
				.withVersion("1.0")
				.withHref(href)
				.withLinks(withoutSelfLink(value.getLinks()))
				.withItems(Collections.singletonList(new CollectionJsonItem<>()
					.withHref(href)
					.withLinks(withoutSelfLink(value.getLinks()))
					.withRawData(value.getContent())))
				.withQueries(findQueries(value))
				.withTemplate(findTemplate(value));

			CollectionJsonDocument<?> doc = new CollectionJsonDocument<>(collectionJson);

			provider
				.findValueSerializer(CollectionJsonDocument.class, property)
				.serialize(doc, jgen, provider);
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
			return new CollectionJsonResourceSerializer(property);
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
		public boolean hasSingleElement(Resource<?> value) {
			return true;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonResourcesSerializer extends ContainerSerializer<Resources<?>> implements ContextualSerializer {

		private final BeanProperty property;

		CollectionJsonResourcesSerializer() {
			this(null);
		}

		CollectionJsonResourcesSerializer(BeanProperty property) {

			super(Resources.class, false);
			this.property = property;
		}

		@Override
		public void serialize(Resources<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {

			CollectionJson<?> collectionJson = new CollectionJson()
				.withVersion("1.0")
				.withHref(value.getRequiredLink(Link.REL_SELF).getHref())
				.withLinks(withoutSelfLink(value.getLinks()))
				.withItems(resourcesToCollectionJsonItems(value))
				.withQueries(findQueries(value))
				.withTemplate(findTemplate(value));

			CollectionJsonDocument<?> doc = new CollectionJsonDocument<>(collectionJson);

			provider
				.findValueSerializer(CollectionJsonDocument.class, property)
				.serialize(doc, jgen, provider);
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
			return new CollectionJsonResourcesSerializer(property);
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
		public boolean isEmpty(Resources<?> value) {
			return value.getContent().size() == 0;
		}

		@Override
		public boolean hasSingleElement(Resources<?> value) {
			return value.getContent().size() == 1;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonPagedResourcesSerializer extends ContainerSerializer<PagedResources<?>> implements ContextualSerializer {

		private final BeanProperty property;

		CollectionJsonPagedResourcesSerializer() {
			this(null);
		}

		CollectionJsonPagedResourcesSerializer(BeanProperty property) {

			super(Resources.class, false);
			this.property = property;
		}

		@Override
		public void serialize(PagedResources<?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {

			CollectionJson<?> collectionJson = new CollectionJson()
				.withVersion("1.0")
				.withHref(value.getRequiredLink(Link.REL_SELF).getHref())
				.withLinks(withoutSelfLink(value.getLinks()))
				.withItems(resourcesToCollectionJsonItems(value))
				.withQueries(findQueries(value))
				.withTemplate(findTemplate(value));

			CollectionJsonDocument<?> doc = new CollectionJsonDocument<>(collectionJson);

			provider
				.findValueSerializer(CollectionJsonDocument.class, property)
				.serialize(doc, jgen, provider);
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
			return new CollectionJsonPagedResourcesSerializer(property);
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
		public boolean isEmpty(PagedResources<?> value) {
			return value.getContent().size() == 0;
		}

		@Override
		public boolean hasSingleElement(PagedResources<?> value) {
			return value.getContent().size() == 1;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}
	}

	static class CollectionJsonLinkListDeserializer extends ContainerDeserializerBase<List<Link>> {

		CollectionJsonLinkListDeserializer() {
			super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Link.class));
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
		public List<Link> deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

			CollectionJsonDocument<?> document = jp.getCodec().readValue(jp, CollectionJsonDocument.class);

			return potentiallyAddSelfLink(document.getCollection().getLinks(), document.getCollection().getHref());
		}
	}

	static class CollectionJsonResourceSupportDeserializer extends ContainerDeserializerBase<ResourceSupport>
		implements ContextualDeserializer {

		private final JavaType contentType;

		CollectionJsonResourceSupportDeserializer() {
			this(TypeFactory.defaultInstance().constructType(ResourceSupport.class));
		}

		CollectionJsonResourceSupportDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		@Override
		public ResourceSupport deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

			JavaType rootType = ctxt.getTypeFactory().constructSimpleType(Object.class, new JavaType[]{});
			JavaType wrappedType = ctxt.getTypeFactory().constructParametricType(CollectionJsonDocument.class, rootType);

			CollectionJsonDocument<?> document = jp.getCodec().readValue(jp, wrappedType);

			List<? extends CollectionJsonItem<?>> items = Optional.ofNullable(document.getCollection().getItems()).orElse(new ArrayList<>());
			List<Link> links = Optional.ofNullable(document.getCollection().getLinks()).orElse(new ArrayList<>());

			if (items.size() == 0) {
				if (document.getCollection().getTemplate() != null) {

					Map<String, Object> properties = document.getCollection().getTemplate().getData().stream()
						.collect(Collectors.toMap(CollectionJsonData::getName, CollectionJsonData::getValue));

					ResourceSupport obj = (ResourceSupport) PropertyUtils.createObjectFromProperties(this.contentType.getRawClass(), properties);

					obj.add(potentiallyAddSelfLink(links, document.getCollection().getHref()));

					return obj;
				} else {
					ResourceSupport resource = new ResourceSupport();
					resource.add(potentiallyAddSelfLink(links, document.getCollection().getHref()));

					return resource;
				}
			} else {

				items.stream()
					.flatMap(item -> Optional.ofNullable(item.getLinks())
						.map(Collection::stream)
						.orElse(Stream.empty()))
					.forEach(link -> {
						if (!links.contains(link))
							links.add(link);
					});

				ResourceSupport resource = (ResourceSupport) items.get(0).toRawData(this.contentType);
				resource.add(potentiallyAddSelfLink(links, items.get(0).getHref()));

				return resource;
			}
		}

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
													BeanProperty property) throws JsonMappingException {

			if (property != null) {
				return new CollectionJsonResourceSupportDeserializer(property.getType().getContentType());
			} else {
				return new CollectionJsonResourceSupportDeserializer(ctxt.getContextualType());
			}
		}
	}

	static class CollectionJsonResourceDeserializer extends ContainerDeserializerBase<Resource<?>>
			implements ContextualDeserializer {

		private final JavaType contentType;

		CollectionJsonResourceDeserializer() {
			this(TypeFactory.defaultInstance().constructType(CollectionJson.class));
		}

		CollectionJsonResourceDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		@Override
		public Resource<?> deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException {

			JavaType rootType = JacksonHelper.findRootType(this.contentType);
			JavaType wrappedType = ctxt.getTypeFactory().constructParametricType(CollectionJsonDocument.class, rootType);

			CollectionJsonDocument<?> document = jp.getCodec().readValue(jp, wrappedType);

			List<? extends CollectionJsonItem<?>> items = Optional.ofNullable(document.getCollection().getItems()).orElse(new ArrayList<>());
			List<Link> links = Optional.ofNullable(document.getCollection().getLinks()).orElse(new ArrayList<>());

			if (items.size() == 0 && document.getCollection().getTemplate() != null) {

				Map<String, Object> properties = document.getCollection().getTemplate().getData().stream()
					.collect(Collectors.toMap(CollectionJsonData::getName, CollectionJsonData::getValue));

				Object obj = PropertyUtils.createObjectFromProperties(rootType.getRawClass(), properties);

				return new Resource<>(obj, potentiallyAddSelfLink(links, document.getCollection().getHref()));
			} else {

				items.stream()
					.flatMap(item -> Optional.ofNullable(item.getLinks())
						.map(Collection::stream)
						.orElse(Stream.empty()))
					.forEach(link -> {
						if (!links.contains(link))
							links.add(link);
					});

				return new Resource<>(items.get(0).toRawData(rootType),
					potentiallyAddSelfLink(links, items.get(0).getHref()));
			}
		}

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
													BeanProperty property) throws JsonMappingException {

			if (property != null) {
				return new CollectionJsonResourceDeserializer(property.getType().getContentType());
			} else {
				return new CollectionJsonResourceDeserializer(ctxt.getContextualType());
			}
		}
	}

	static class CollectionJsonResourcesDeserializer extends ContainerDeserializerBase<Resources>
			implements ContextualDeserializer {

		private final JavaType contentType;

		CollectionJsonResourcesDeserializer() {
			this(TypeFactory.defaultInstance().constructType(CollectionJson.class));
		}

		CollectionJsonResourcesDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		@Override
		public Resources deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

			JavaType rootType = JacksonHelper.findRootType(this.contentType);
			JavaType wrappedType = ctxt.getTypeFactory().constructParametricType(CollectionJsonDocument.class, rootType);

			CollectionJsonDocument<?> document = jp.getCodec().readValue(jp, wrappedType);

			List<Object> contentList = new ArrayList<Object>();

			if (document.getCollection().getItems() != null) {
				for (CollectionJsonItem<?> item : document.getCollection().getItems()) {

					Object data = item.toRawData(rootType);

					if (this.contentType.hasGenericTypes()) {
						if (isResource(this.contentType)) {
							contentList.add(new Resource<>(data, potentiallyAddSelfLink(item.getLinks(), item.getHref())));
						} else {
							contentList.add(data);
						}
					}
				}
			}

			return new Resources(contentList, potentiallyAddSelfLink(document.getCollection().getLinks(), document.getCollection().getHref()));
		}

		static boolean isResource(JavaType type) {
			return type.containedType(0).hasRawClass(Resource.class);
		}

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
													BeanProperty property) throws JsonMappingException {

			if (property != null) {

				JavaType vc = property.getType().getContentType();
				CollectionJsonResourcesDeserializer des = new CollectionJsonResourcesDeserializer(vc);
				
				return des;
			} else {
				return new CollectionJsonResourcesDeserializer(ctxt.getContextualType());
			}
		}
	}

	static class CollectionJsonPagedResourcesDeserializer extends ContainerDeserializerBase<PagedResources>
			implements ContextualDeserializer {

		private final JavaType contentType;

		CollectionJsonPagedResourcesDeserializer() {
			this(TypeFactory.defaultInstance().constructType(CollectionJson.class));
		}

		CollectionJsonPagedResourcesDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

		@Override
		public PagedResources deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

			JavaType rootType = JacksonHelper.findRootType(this.contentType);
			JavaType wrappedType = ctxt.getTypeFactory().constructParametricType(CollectionJsonDocument.class, rootType);

			CollectionJsonDocument<?> document = jp.getCodec().readValue(jp, wrappedType);

			List<Object> items = new ArrayList<>();

			document.getCollection().getItems().forEach(item -> {

				Object data = item.toRawData(rootType);
				List<Link> links = item.getLinks() == null ? Collections.EMPTY_LIST : item.getLinks();

				if (this.contentType.hasGenericTypes()) {

					if (this.contentType.containedType(0).hasRawClass(Resource.class)) {
						items.add(new Resource<>(data, potentiallyAddSelfLink(links, item.getHref())));
					} else {
						items.add(data);
					}
				}
			});

			PagedResources.PageMetadata pageMetadata = null;

			return new PagedResources(items, pageMetadata,
				potentiallyAddSelfLink(document.getCollection().getLinks(), document.getCollection().getHref()));
		}

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {

			if (property != null) {

				JavaType vc = property.getType().getContentType();
				CollectionJsonPagedResourcesDeserializer des = new CollectionJsonPagedResourcesDeserializer(vc);
				
				return des;
			} else {
				return new CollectionJsonPagedResourcesDeserializer(ctxt.getContextualType());
			}
		}

	}

	public static class CollectionJsonHandlerInstantiator extends HandlerInstantiator {

		private final Map<Class<?>, Object> instanceMap = new HashMap<Class<?>, Object>();

		public CollectionJsonHandlerInstantiator(MessageSourceAccessor messageSource) {

			this.instanceMap.put(CollectionJsonPagedResourcesSerializer.class, new CollectionJsonPagedResourcesSerializer());
			this.instanceMap.put(CollectionJsonResourcesSerializer.class, new CollectionJsonResourcesSerializer());
			this.instanceMap.put(CollectionJsonResourceSerializer.class, new CollectionJsonResourceSerializer());
			this.instanceMap.put(CollectionJsonResourceSupportSerializer.class, new CollectionJsonResourceSupportSerializer());
			this.instanceMap.put(CollectionJsonLinkListSerializer.class, new CollectionJsonLinkListSerializer(messageSource));
		}

		private Object findInstance(Class<?> type) {

			Object result = instanceMap.get(type);
			return result != null ? result : BeanUtils.instantiateClass(type);
		}

		@Override
		public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
			return (JsonDeserializer<?>) findInstance(deserClass);
		}

		@Override
		public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
			return (KeyDeserializer) findInstance(keyDeserClass);
		}

		@Override
		public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
			return (JsonSerializer<?>) findInstance(serClass);
		}

		@Override
		public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
			return (TypeResolverBuilder<?>) findInstance(builderClass);
		}

		@Override
		public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
			return (TypeIdResolver) findInstance(resolverClass);
		}
	}

	/**
	 * Return a list of {@link Link}s that includes a "self" link.
	 * 
	 * @param links - base set of {@link Link}s.
	 * @param href - the URI of the "self" link
	 * @return
	 */
	private static List<Link> potentiallyAddSelfLink(List<Link> links, String href) {

		if (links == null) {

			if (href == null) {
				return Collections.emptyList();
			}

			return Collections.singletonList(new Link(href));
		}

		if (href == null || links.stream().map(Link::getRel).anyMatch(s -> s.equals(Link.REL_SELF))) {
			return links;
		}

		// Clone and add the self link

		List<Link> newLinks = new ArrayList<>();
		newLinks.add(new Link(href));
		newLinks.addAll(links);

		return newLinks;
	}

	private static List<Link> withoutSelfLink(List<Link> links) {

		return links.stream()
			.filter(link -> !link.getRel().equals(Link.REL_SELF))
			.collect(Collectors.toList());
	}

	private static List<CollectionJsonItem<?>> resourcesToCollectionJsonItems(Resources<?> resources) {
		
		return resources.getContent().stream()
			.map(content -> {
				if (ClassUtils.isAssignableValue(Resource.class, content)) {

					Resource resource = (Resource) content;

					return new CollectionJsonItem<>()
						.withHref(resource.getRequiredLink(Link.REL_SELF).getHref())
						.withLinks(withoutSelfLink(resource.getLinks()))
						.withRawData(resource.getContent());
				} else {
					return new CollectionJsonItem<>().withRawData(content);
				}
			})
			.collect(Collectors.toList());
	}

	/**
	 * Scan through the {@link Affordance}s and find any {@literal GET} calls against non-self URIs.
	 *
	 * @param resource
	 * @return
	 */
	private static List<CollectionJsonQuery> findQueries(ResourceSupport resource) {

		List<CollectionJsonQuery> queries = new ArrayList<>();

		if (resource.hasLink(Link.REL_SELF)) {
			Link selfLink = resource.getRequiredLink(Link.REL_SELF);

			selfLink.getAffordances().forEach(affordance -> {

				CollectionJsonAffordanceModel model = affordance.getAffordanceModel(MediaTypes.COLLECTION_JSON);

				/**
				 * For Collection+JSON, "queries" are only collected for GET affordances where the URI is NOT a self link.
				 */
				if (affordance.getHttpMethod().equals(HttpMethod.GET) && !model.getUri().equals(selfLink.getHref())) {

					queries.add(new CollectionJsonQuery()
						.withRel(model.getRel())
						.withHref(model.getUri())
						.withData(model.getQueryProperties()));
				}
			});
		}

		return queries;
	}

	/**
	 * Scan through the {@link Affordance}s and
	 * @param resource
	 * @return
	 */
	private static CollectionJsonTemplate findTemplate(ResourceSupport resource) {

		List<CollectionJsonTemplate> templates = new ArrayList<>();

		if (resource.hasLink(Link.REL_SELF)) {
			resource.getRequiredLink(Link.REL_SELF).getAffordances().forEach(affordance -> {

				CollectionJsonAffordanceModel model = affordance.getAffordanceModel(MediaTypes.COLLECTION_JSON);

				/**
				 * For Collection+JSON, "templates" are made of any non-GET affordances.
				 */
				if (!affordance.getHttpMethod().equals(HttpMethod.GET)) {

					CollectionJsonTemplate template = new CollectionJsonTemplate() //
						.withData(model.getInputProperties());

					templates.add(template);
				}
			});
		}

		/**
		 * Collection+JSON can only have one template, so grab the first one.
		 */
		return templates.stream()
			.findFirst()
			.orElse(null);
	}

}
