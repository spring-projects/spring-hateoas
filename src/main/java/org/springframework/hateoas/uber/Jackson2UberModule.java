/*
 * Copyright 2017-2018 the original author or authors.
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
package org.springframework.hateoas.uber;

import static org.springframework.hateoas.PagedResources.*;
import static org.springframework.hateoas.support.JacksonHelper.*;
import static org.springframework.hateoas.uber.UberData.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.support.JacksonHelper;
import org.springframework.hateoas.support.PropertyUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Jackson {@link SimpleModule} for {@literal UBER+JSON} serializers and deserializers.
 * 
 * @author Greg Turnquist
 * @author Jens Schauder
 * @since 1.0
 */
public class Jackson2UberModule extends SimpleModule {

	public Jackson2UberModule() {

		super("uber-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

		setMixInAnnotation(ResourceSupport.class, ResourceSupportMixin.class);
		setMixInAnnotation(Resource.class, ResourceMixin.class);
		setMixInAnnotation(Resources.class, ResourcesMixin.class);
		setMixInAnnotation(PagedResources.class, PagedResourcesMixin.class);

		addSerializer(new UberPagedResourcesSerializer());
		addSerializer(new UberResourcesSerializer());
		addSerializer(new UberResourceSerializer());
		addSerializer(new UberResourceSupportSerializer());
	}

	/**
	 * Custom {@link JsonSerializer} to render {@link ResourceSupport} into {@literal UBER+JSON}.
	 */
	static class UberResourceSupportSerializer extends ContainerSerializer<ResourceSupport>
			implements ContextualSerializer {

		private final BeanProperty property;

		UberResourceSupportSerializer(BeanProperty property) {

			super(ResourceSupport.class, false);
			this.property = property;
		}

		UberResourceSupportSerializer() {
			this(null);
		}

		@Override
		public void serialize(ResourceSupport value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			UberDocument doc = new UberDocument() //
					.withUber(new Uber() //
							.withVersion("1.0") //
							.withData(extractLinksAndContent(value)));

			provider //
					.findValueSerializer(UberDocument.class, property) //
					.serialize(doc, gen, provider);
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
			return false;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new UberResourceSupportSerializer(property);
		}
	}

	/**
	 * Custom {@link JsonSerializer} to render {@link Resource} into {@literal UBER+JSON}.
	 */
	static class UberResourceSerializer extends ContainerSerializer<Resource<?>> implements ContextualSerializer {

		private final BeanProperty property;

		UberResourceSerializer(BeanProperty property) {

			super(Resource.class, false);
			this.property = property;
		}

		UberResourceSerializer() {
			this(null);
		}

		@Override
		public void serialize(Resource<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			UberDocument doc = new UberDocument()
					.withUber(new Uber()
							.withVersion("1.0")
							.withData(extractLinksAndContent(value)));

			provider //
					.findValueSerializer(UberDocument.class, property) //
					.serialize(doc, gen, provider);
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
			return false;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new UberResourceSerializer(property);
		}
	}

	/**
	 * Custom {@link JsonSerializer} to render {@link Resources} into {@literal UBER+JSON}.
	 */
	static class UberResourcesSerializer extends ContainerSerializer<Resources<?>> implements ContextualSerializer {

		private BeanProperty property;

		UberResourcesSerializer(BeanProperty property) {

			super(Resources.class, false);
			this.property = property;
		}

		UberResourcesSerializer() {
			this(null);
		}

		@Override
		public void serialize(Resources<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			UberDocument doc = new UberDocument() //
					.withUber(new Uber() //
							.withVersion("1.0") //
							.withData(extractLinksAndContent(value)));

			provider //
					.findValueSerializer(UberDocument.class, property) //
					.serialize(doc, gen, provider);
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
		public boolean hasSingleElement(Resources<?> value) {
			return value.getContent().size() == 1;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new UberResourcesSerializer(property);
		}
	}

	/**
	 * Custom {@link JsonSerializer} to render {@link PagedResources} into {@literal UBER+JSON}.
	 */
	static class UberPagedResourcesSerializer extends ContainerSerializer<PagedResources<?>>
			implements ContextualSerializer {

		private BeanProperty property;

		UberPagedResourcesSerializer(BeanProperty property) {

			super(PagedResources.class, false);
			this.property = property;
		}

		UberPagedResourcesSerializer() {
			this(null);
		}

		@Override
		public void serialize(PagedResources<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			UberDocument doc = new UberDocument() //
					.withUber(new Uber() //
							.withVersion("1.0") //
							.withData(extractLinksAndContent(value)));

			provider //
					.findValueSerializer(UberDocument.class, property) //
					.serialize(doc, gen, provider);
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
		public boolean hasSingleElement(PagedResources<?> value) {
			return value.getContent().size() == 1;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			return null;
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new UberPagedResourcesSerializer(property);
		}
	}

	/**
	 * Custom {@link StdSerializer} to translate {@link UberAction} into the proper JSON representation.
	 */
	static class UberActionSerializer extends StdSerializer<UberAction> {

		UberActionSerializer() {
			super(UberAction.class);
		}

		@Override
		public void serialize(UberAction value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			gen.writeString(value.toString());
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link ResourceSupport}.
	 */
	static class UberResourceSupportDeserializer extends ContainerDeserializerBase<ResourceSupport>
			implements ContextualDeserializer {

		private JavaType contentType;

		UberResourceSupportDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberResourceSupportDeserializer() {
			this(TypeFactory.defaultInstance().constructSimpleType(UberDocument.class, new JavaType[0]));
		}

		@Override
		public ResourceSupport deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

			UberDocument doc = p.getCodec().readValue(p, UberDocument.class);
			List<Link> links = doc.getUber().getLinks();

			return doc.getUber().getData().stream() //
					.filter(uberData -> !StringUtils.isEmpty(uberData.getName())) //
					.findFirst() //
					.map(uberData -> convertToResourceSupport(uberData, links)) //
					.orElseGet(() -> {

						ResourceSupport resourceSupport = new ResourceSupport();
						resourceSupport.add(links);

						return resourceSupport;
					});
		}

		@NotNull
		private ResourceSupport convertToResourceSupport(UberData uberData, List<Link> links) {

			List<UberData> data = uberData.getData();
			Map<String, Object> properties;

			if (data == null) {
				properties = new HashMap<>();
			} else {
				properties = data.stream() //
						.collect(Collectors.toMap(UberData::getName, UberData::getValue));
			}
			ResourceSupport resourceSupport = (ResourceSupport) PropertyUtils
					.createObjectFromProperties(this.getContentType().getRawClass(), properties);

			resourceSupport.add(links);

			return resourceSupport;
		}

		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {

			if (property != null) {
				JavaType vc = property.getType().getContentType();
				return new UberResourceSupportDeserializer(vc);
			} else {
				return new UberResourceSupportDeserializer(ctxt.getContextualType());
			}
		}

		/**
		 * Accessor for deserializer use for deserializing content values.
		 */
		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link Resource}.
	 */
	static class UberResourceDeserializer extends ContainerDeserializerBase<Resource<?>>
			implements ContextualDeserializer {

		private JavaType contentType;

		UberResourceDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberResourceDeserializer() {
			this(TypeFactory.defaultInstance().constructSimpleType(UberDocument.class, new JavaType[0]));
		}

		@Override
		public Resource<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

			UberDocument doc = p.getCodec().readValue(p, UberDocument.class);
			List<Link> links = doc.getUber().getLinks();

			return doc.getUber().getData().stream().filter(uberData -> !StringUtils.isEmpty(uberData.getName())).findFirst()
					.map(uberData -> convertUberDataToResource(uberData, links)).orElseThrow(
							() -> new IllegalStateException("No data entry containing a 'value' was found in this document!"));
		}

		@NotNull
		private Resource<Object> convertUberDataToResource(UberData uberData, List<Link> links) {

			// Primitive type
			List<UberData> data = uberData.getData();
			if (data != null && data.size() == 1 && data.get(0).getName() == null) {
				Object scalarValue = data.get(0).getValue();
				return new Resource<>(scalarValue, links);
			}

			Map<String, Object> properties;
			if (data == null) {
				properties = new HashMap<>();
			} else {
				properties = data.stream().collect(Collectors.toMap(UberData::getName, UberData::getValue));
			}

			JavaType rootType = JacksonHelper.findRootType(this.contentType);

			Object value = PropertyUtils.createObjectFromProperties(rootType.getRawClass(), properties);

			return new Resource<>(value, links);
		}

		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {

			if (property != null) {
				JavaType vc = property.getType().getContentType();
				return new UberResourceDeserializer(vc);
			} else {
				return new UberResourceDeserializer(ctxt.getContextualType());
			}
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link Resources}.
	 */
	static class UberResourcesDeserializer extends ContainerDeserializerBase<Resources<?>>
			implements ContextualDeserializer {

		private JavaType contentType;

		UberResourcesDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberResourcesDeserializer() {
			this(TypeFactory.defaultInstance().constructSimpleType(UberDocument.class, new JavaType[0]));
		}

		@Override
		public Resources<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

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

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {

			if (property != null) {
				JavaType vc = property.getType().getContentType();
				return new UberResourcesDeserializer(vc);
			} else {
				return new UberResourcesDeserializer(ctxt.getContextualType());
			}
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}
	}

	/**
	 * Custom {@link StdDeserializer} to deserialize {@link PagedResources}.
	 */
	static class UberPagedResourcesDeserializer extends ContainerDeserializerBase<PagedResources<?>>
			implements ContextualDeserializer {

		private JavaType contentType;

		UberPagedResourcesDeserializer(JavaType contentType) {

			super(contentType);
			this.contentType = contentType;
		}

		UberPagedResourcesDeserializer() {
			this(TypeFactory.defaultInstance().constructSimpleType(UberDocument.class, new JavaType[0]));
		}

		@Override
		public PagedResources<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

			JavaType rootType = JacksonHelper.findRootType(this.contentType);

			UberDocument doc = p.getCodec().readValue(p, UberDocument.class);

			Resources<?> resources = extractResources(doc, rootType, this.contentType);
			PageMetadata pageMetadata = extractPagingMetadata(doc);

			return new PagedResources<>(resources.getContent(), pageMetadata, resources.getLinks());
		}

		/**
		 * Accessor for declared type of contained value elements; either exact type, or one of its supertypes.
		 */
		@Override
		public JavaType getContentType() {
			return this.contentType;
		}

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
				throws JsonMappingException {

			if (property != null) {
				JavaType vc = property.getType().getContentType();
				return new UberPagedResourcesDeserializer(vc);
			} else {
				return new UberPagedResourcesDeserializer(ctxt.getContextualType());
			}
		}

		/**
		 * Accesor for deserializer use for deserializing content values.
		 */
		@Override
		public JsonDeserializer<Object> getContentDeserializer() {
			return null;
		}

	}

	/**
	 * Convert an {@link UberDocument} into a {@link Resources}.
	 * 
	 * @param doc
	 * @param rootType
	 * @param contentType
	 * @return
	 */
	private static Resources<?> extractResources(UberDocument doc, JavaType rootType, JavaType contentType) {

		List<Object> content = new ArrayList<>();

		for (UberData uberData : doc.getUber().getData()) {

			if (uberData.getName() != null && uberData.getName().equals("page")) {
				continue;
			}

			if (uberData.getLinks().isEmpty()) {

				List<Link> resourceLinks = new ArrayList<>();
				Resource<?> resource = null;

				List<UberData> data = uberData.getData();
				if (data == null) {
					throw new RuntimeException("No content!");
				}

				for (UberData item : data) {

					if (item.getRel() != null) {
						item.getRel().forEach(rel -> resourceLinks.add(new Link(item.getUrl(), rel)));
					} else {

						// Primitive type
						if (item.getData().size() == 1 && item.getData().get(0).getName() == null) {

							Object scalarValue = item.getData().get(0).getValue();
							resource = new Resource<>(scalarValue, uberData.getLinks());
						} else {

							Map<String, Object> properties = item.getData().stream()
									.collect(Collectors.toMap(UberData::getName, UberData::getValue));
							Object obj = PropertyUtils.createObjectFromProperties(rootType.getRawClass(), properties);
							resource = new Resource<>(obj, uberData.getLinks());
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
		}

		if (isResourcesOfResource(contentType)) {
			/*
			 * Either return a Resources<Resource<T>>...
			 */
			return new Resources<>(content, doc.getUber().getLinks());
		} else {
			/*
			 * ...or return a Resources<T>
			 */

			List<Object> resourceLessContent = content.stream().map(item -> (Resource<?>) item).map(Resource::getContent)
					.collect(Collectors.toList());

			return new Resources<>(resourceLessContent, doc.getUber().getLinks());
		}
	}

	private static PageMetadata extractPagingMetadata(UberDocument doc) {

		return doc.getUber().getData().stream()
				.filter(uberData -> uberData.getName() != null && uberData.getName().equals("page")).findFirst()
				.map(Jackson2UberModule::convertUberDataToPageMetaData).orElse(null);
	}

	@NotNull
	private static PageMetadata convertUberDataToPageMetaData(UberData uberData) {

		int size = 0;
		int number = 0;
		int totalElements = 0;
		int totalPages = 0;

		for (UberData data : uberData.getData()) {

			String name = data.getName();
			switch (name) {

				case "size":
					size = (int) data.getValue();
					break;

				case "number":
					number = (int) data.getValue();
					break;

				case "totalElements":
					totalElements = (int) data.getValue();
					break;

				case "totalPages":
					totalPages = (int) data.getValue();

				default:
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

		@Override
		public UberAction deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			return UberAction.valueOf(p.getText().toUpperCase());
		}
	}

	public static class UberHandlerInstantiator extends HandlerInstantiator {

		private final Map<Class<?>, Object> serializers = new HashMap<>();

		public UberHandlerInstantiator() {

			this.serializers.put(UberResourceSupportSerializer.class, new UberResourceSupportSerializer());
			this.serializers.put(UberResourceSerializer.class, new UberResourceSerializer());
			this.serializers.put(UberResourcesSerializer.class, new UberResourcesSerializer());
			this.serializers.put(UberPagedResourcesSerializer.class, new UberPagedResourcesSerializer());
		}

		@Override
		public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
				Class<?> deserClass) {
			return (JsonDeserializer<?>) findInstance(deserClass);
		}

		@Override
		public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated,
				Class<?> keyDeserClass) {
			return (KeyDeserializer) findInstance(keyDeserClass);
		}

		@Override
		public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
			return (JsonSerializer<?>) findInstance(serClass);
		}

		@Override
		public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated,
				Class<?> builderClass) {
			return (TypeResolverBuilder<?>) findInstance(builderClass);
		}

		@Override
		public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
			return (TypeIdResolver) findInstance(resolverClass);
		}

		private Object findInstance(Class<?> type) {

			Object result = this.serializers.get(type);
			return result != null ? result : BeanUtils.instantiateClass(type);
		}
	}
}
