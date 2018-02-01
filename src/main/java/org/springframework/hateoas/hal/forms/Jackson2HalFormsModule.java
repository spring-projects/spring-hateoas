/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule.EmbeddedMapper;
import org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListSerializer;
import org.springframework.hateoas.hal.LinkMixin;
import org.springframework.hateoas.hal.ResourceSupportMixin;
import org.springframework.hateoas.hal.forms.HalFormsDeserializers.HalFormsResourcesDeserializer;
import org.springframework.hateoas.hal.forms.HalFormsSerializers.HalFormsResourceSerializer;
import org.springframework.hateoas.hal.forms.HalFormsSerializers.HalFormsResourcesSerializer;
import org.springframework.hateoas.mvc.JacksonSerializers.MediaTypeDeserializer;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

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
		setMixInAnnotation(ResourceSupport.class, ResourceSupportMixin.class);
		setMixInAnnotation(Resource.class, ResourceMixin.class);
		setMixInAnnotation(Resources.class, ResourcesMixin.class);
		setMixInAnnotation(PagedResources.class, PagedResourcesMixin.class);
		setMixInAnnotation(MediaType.class, MediaTypeMixin.class);
	}

	@JsonSerialize(using = HalFormsResourceSerializer.class)
	static interface ResourceMixin {}

	@JsonSerialize(using = HalFormsResourcesSerializer.class)
	abstract class ResourcesMixin<T> extends Resources<T> {

		@Override
		@JsonProperty("_embedded")
		@JsonInclude(Include.NON_EMPTY)
		@JsonDeserialize(using = HalFormsResourcesDeserializer.class)
		public abstract Collection<T> getContent();
	}

	abstract class PagedResourcesMixin<T> extends PagedResources<T> {

		@Override
		@JsonProperty("page")
		@JsonInclude(Include.NON_EMPTY)
		public PageMetadata getMetadata() {
			return super.getMetadata();
		}
	}

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = MediaTypeDeserializer.class)
	static interface MediaTypeMixin {}

	/**
	 * Create new HAL-FORMS serializers based on the context.
	 */
	public static class HalFormsHandlerInstantiator extends HalHandlerInstantiator {

		private final Map<Class<?>, Object> serializers = new HashMap<Class<?>, Object>();

		public HalFormsHandlerInstantiator(RelProvider resolver, CurieProvider curieProvider,
										   MessageSourceAccessor messageSource, boolean enforceEmbeddedCollections,
										   HalFormsConfiguration halFormsConfiguration) {

			super(resolver, curieProvider, messageSource, enforceEmbeddedCollections, halFormsConfiguration.toHalConfiguration());

			EmbeddedMapper mapper = new EmbeddedMapper(resolver, curieProvider, enforceEmbeddedCollections);

			this.serializers.put(HalFormsResourcesSerializer.class, new HalFormsResourcesSerializer(mapper));
			this.serializers.put(HalLinkListSerializer.class,
				new HalLinkListSerializer(curieProvider, mapper, messageSource, halFormsConfiguration.toHalConfiguration()));
		}

		public HalFormsHandlerInstantiator(RelProvider relProvider, CurieProvider curieProvider,
										   MessageSourceAccessor messageSource, boolean enforceEmbeddedCollections,
										   AutowireCapableBeanFactory beanFactory) {
			this(relProvider, curieProvider, messageSource, enforceEmbeddedCollections, beanFactory.getBean(HalFormsConfiguration.class));
		}

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
