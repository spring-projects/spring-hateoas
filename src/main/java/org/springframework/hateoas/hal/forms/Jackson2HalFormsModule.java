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

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.affordance.Suggestions;
import org.springframework.hateoas.affordance.Suggestions.ExternalSuggestions;
import org.springframework.hateoas.affordance.Suggestions.RemoteSuggestions;
import org.springframework.hateoas.affordance.Suggestions.ValueSuggestions;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule.EmbeddedMapper;
import org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListSerializer;
import org.springframework.hateoas.hal.LinkMixin;
import org.springframework.hateoas.hal.forms.HalFormsDeserializers.SuggestDeserializer;
import org.springframework.hateoas.hal.forms.HalFormsSerializers.ExternalSuggestionSerializer;
import org.springframework.hateoas.hal.forms.HalFormsSerializers.HalEmbeddedResourcesSerializer;
import org.springframework.hateoas.hal.forms.HalFormsSerializers.HalFormsTemplateListSerializer;
import org.springframework.hateoas.hal.forms.HalFormsSerializers.RemoteSuggestionsSerializer;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Serialize/Deserialize all the parts of HAL-Form documents using Jackson.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 */
public class Jackson2HalFormsModule extends SimpleModule {

	private static final long serialVersionUID = -4496351128468451196L;

	public Jackson2HalFormsModule() {

		super("hal-forms-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(Suggestions.class, SuggestionsMixin.class);
		setMixInAnnotation(ValueSuggestions.class, ValueSuggestionsMixin.class);

		addSerializer(RemoteSuggestions.class, new RemoteSuggestionsSerializer());
		addSerializer(ExternalSuggestions.class, new ExternalSuggestionSerializer());
		addDeserializer(Suggestions.class, new SuggestDeserializer());
	}

	/**
	 * Create new HAL-Forms serializers based on the context.
	 */
	public static class HalFormsHandlerInstantiator extends HalHandlerInstantiator {

		private final Map<Class<?>, Object> serializers = new HashMap<Class<?>, Object>();

		public HalFormsHandlerInstantiator(RelProvider resolver, CurieProvider curieProvider,
										   MessageSourceAccessor messageSource, boolean enforceEmbeddedCollections) {

			super(resolver, curieProvider, messageSource, enforceEmbeddedCollections);

			EmbeddedMapper mapper = new EmbeddedMapper(resolver, curieProvider, enforceEmbeddedCollections);

			this.serializers.put(HalLinkListSerializer.class, new HalLinkListSerializer(curieProvider, mapper, messageSource));
			this.serializers.put(HalFormsTemplateListSerializer.class, new HalFormsTemplateListSerializer(messageSource));
			this.serializers.put(HalEmbeddedResourcesSerializer.class, new HalEmbeddedResourcesSerializer(mapper));
		}

		private Object findInstance(Class<?> type) {
			return this.serializers.get(type);
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
