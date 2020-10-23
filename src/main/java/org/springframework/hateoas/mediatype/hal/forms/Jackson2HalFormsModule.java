/*
 * Copyright 2016-2020 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.EmbeddedMapper;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.HalLinkListDeserializer;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.HalLinkListSerializer;
import org.springframework.hateoas.mediatype.hal.LinkMixin;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsDeserializers.HalFormsCollectionModelDeserializer;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsSerializers.HalFormsCollectionModelSerializer;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsSerializers.HalFormsEntityModelSerializer;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsSerializers.HalFormsRepresentationModelSerializer;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.mvc.JacksonSerializers.MediaTypeDeserializer;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Serialize / deserialize all the parts of HAL-FORMS documents using Jackson.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public class Jackson2HalFormsModule extends SimpleModule {

	private static final long serialVersionUID = -4496351128468451196L;

	public Jackson2HalFormsModule() {

		super("hal-forms-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(Links.class, LinksMixin.class);
		setMixInAnnotation(RepresentationModel.class, RepresentationModelMixin.class);
		setMixInAnnotation(EntityModel.class, EntityModelMixin.class);
		setMixInAnnotation(CollectionModel.class, CollectionModelMixin.class);
		setMixInAnnotation(PagedModel.class, PagedModelMixin.class);
		setMixInAnnotation(MediaType.class, MediaTypeMixin.class);
	}

	@JsonSerialize(using = HalLinkListSerializer.class)
	abstract class LinksMixin {}

	@JsonSerialize(using = HalFormsRepresentationModelSerializer.class)
	abstract class RepresentationModelMixin extends org.springframework.hateoas.mediatype.hal.RepresentationModelMixin {}

	@JsonSerialize(using = HalFormsEntityModelSerializer.class)
	abstract class EntityModelMixin<T> extends EntityModel<T> {}

	@JsonSerialize(using = HalFormsCollectionModelSerializer.class)
	abstract class CollectionModelMixin<T> extends CollectionModel<T> {

		@Override
		@JsonProperty("_embedded")
		@JsonInclude(Include.NON_EMPTY)
		@JsonDeserialize(using = HalFormsCollectionModelDeserializer.class)
		public abstract Collection<T> getContent();
	}

	abstract class PagedModelMixin<T> extends PagedModel<T> {

		@Nullable
		@Override
		@JsonProperty("page")
		@JsonInclude(Include.NON_EMPTY)
		public PageMetadata getMetadata() {
			return super.getMetadata();
		}
	}

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = MediaTypeDeserializer.class)
	interface MediaTypeMixin {}

	static class HalFormsLinksDeserializer extends ContainerDeserializerBase<Links> {

		private static final long serialVersionUID = -848240531474910385L;

		private final HalLinkListDeserializer delegate = new HalLinkListDeserializer();

		public HalFormsLinksDeserializer() {
			super(TypeFactory.defaultInstance().constructType(Links.class));
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentDeserializer()
		 */
		@Override
		@Nullable
		public JsonDeserializer<Object> getContentDeserializer() {
			return delegate.getContentDeserializer();
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		@SuppressWarnings("null")
		public Links deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			return Links.of(delegate.deserialize(p, ctxt));
		}
	}

	/**
	 * Create new HAL-FORMS serializers based on the context.
	 */
	public static class HalFormsHandlerInstantiator extends HalHandlerInstantiator {

		private final Map<Class<?>, Object> serializers = new HashMap<>();

		public HalFormsHandlerInstantiator(LinkRelationProvider resolver, CurieProvider curieProvider,
				MessageResolver accessor, HalFormsConfiguration configuration, AutowireCapableBeanFactory beanFactory) {

			super(resolver, curieProvider, accessor, configuration.getHalConfiguration(), beanFactory);

			HalConfiguration halConfiguration = configuration.getHalConfiguration();
			EmbeddedMapper mapper = new EmbeddedMapper(resolver, curieProvider,
					halConfiguration.isEnforceEmbeddedCollections());
			HalFormsTemplateBuilder builder = new HalFormsTemplateBuilder(configuration, accessor);

			this.serializers.put(HalFormsRepresentationModelSerializer.class,
					new HalFormsRepresentationModelSerializer(builder));
			this.serializers.put(HalFormsEntityModelSerializer.class, new HalFormsEntityModelSerializer(builder));
			this.serializers.put(HalFormsCollectionModelSerializer.class,
					new HalFormsCollectionModelSerializer(builder, mapper, halConfiguration));
			this.serializers.put(HalLinkListSerializer.class,
					new HalLinkListSerializer(curieProvider, mapper, accessor, halConfiguration));
		}

		public HalFormsHandlerInstantiator(LinkRelationProvider relProvider, CurieProvider curieProvider,
				MessageResolver resolver, AutowireCapableBeanFactory beanFactory) {

			this(relProvider, curieProvider, resolver, beanFactory.getBean(HalFormsConfiguration.class), beanFactory);
		}

		@Nullable
		private Object findInstance(Class<?> type) {
			return this.serializers.get(type);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator#deserializerInstance(com.fasterxml.jackson.databind.DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
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
		 * @see org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator#keyDeserializerInstance(com.fasterxml.jackson.databind.DeserializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
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
		 * @see org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator#serializerInstance(com.fasterxml.jackson.databind.SerializationConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {

			Object jsonSer = findInstance(serClass);
			return jsonSer != null ? (JsonSerializer<?>) jsonSer : super.serializerInstance(config, annotated, serClass);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator#typeResolverBuilderInstance(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
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
		 * @see org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator#typeIdResolverInstance(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.Annotated, java.lang.Class)
		 */
		@Override
		public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {

			Object resolver = findInstance(resolverClass);
			return resolver != null ? (TypeIdResolver) resolver
					: super.typeIdResolverInstance(config, annotated, resolverClass);
		}
	}
}
