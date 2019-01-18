/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.config.reactive;

import static org.springframework.hateoas.config.HypermediaObjectMapperCreator.*;

import lombok.RequiredArgsConstructor;

import java.util.Collection;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.codec.StringDecoder;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.hateoas.hal.forms.HalFormsConfiguration;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link WebFluxConfigurer} to register hypermedia-aware {@link org.springframework.core.codec.Encoder}s and
 * {@link org.springframework.core.codec.Decoder}s that will render hypermedia for WebFlux controllers.
 * 
 * @author Greg Turnquist
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
public class HypermediaWebFluxConfigurer implements WebFluxConfigurer, BeanFactoryAware {

	private static final String MESSAGE_SOURCE_BEAN_NAME = "linkRelationMessageSource";

	private final ObjectMapper mapper;
	private final DelegatingRelProvider relProvider;
	private final CurieProvider curieProvider;
	private final HalConfiguration halConfiguration;
	private final HalFormsConfiguration halFormsConfiguration;
	private final Collection<HypermediaType> hypermediaTypes;

	private BeanFactory beanFactory;
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Configure custom HTTP message readers and writers or override built-in ones.
	 * <p>The configured readers and writers will be used for both annotated
	 * controllers and functional endpoints.
	 *
	 * @param configurer the configurer to use
	 */
	@Override
	public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {

		if (configurer.getReaders().stream()
			.flatMap(httpMessageReader -> httpMessageReader.getReadableMediaTypes().stream())
			.anyMatch(mediaType -> mediaType == MediaTypes.HAL_JSON
				|| mediaType == MediaTypes.HAL_JSON_UTF8
				|| mediaType == MediaTypes.HAL_FORMS_JSON
				|| mediaType == MediaTypes.COLLECTION_JSON
				|| mediaType == MediaTypes.UBER_JSON)) {

			// Already configured for hypermedia!
			return;
		}

		MessageSourceAccessor linkRelationMessageSource = this.beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME,
			MessageSourceAccessor.class);

		CodecConfigurer.CustomCodecs customCodecs = configurer.customCodecs();

		if (this.hypermediaTypes.stream().anyMatch(HypermediaType::isHalBasedMediaType)) {

			if (this.hypermediaTypes.contains(HypermediaType.HAL)) {

				ObjectMapper halObjectMapper = createHalObjectMapper(this.mapper, this.curieProvider, this.relProvider,
					linkRelationMessageSource, this.halConfiguration);

				customCodecs.encoder(
					new Jackson2JsonEncoder(halObjectMapper, MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8));
				customCodecs.decoder(
					new Jackson2JsonDecoder(halObjectMapper, MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8));
			}

			if (this.hypermediaTypes.contains(HypermediaType.HAL_FORMS)) {

				ObjectMapper halFormsObjectMapper = createHalFormsObjectMapper(this.mapper, this.curieProvider,
					this.relProvider, linkRelationMessageSource, this.halFormsConfiguration);

				customCodecs.encoder(
					new Jackson2JsonEncoder(halFormsObjectMapper, MediaTypes.HAL_FORMS_JSON));
				customCodecs.decoder(
					new Jackson2JsonDecoder(halFormsObjectMapper, MediaTypes.HAL_FORMS_JSON));
			}
		}

		if (this.hypermediaTypes.contains(HypermediaType.COLLECTION_JSON)) {

			ObjectMapper collectionJsonObjectMapper = createCollectionJsonObjectMapper(this.mapper, linkRelationMessageSource);

			customCodecs.encoder(
				new Jackson2JsonEncoder(collectionJsonObjectMapper, MediaTypes.COLLECTION_JSON));
			customCodecs.decoder(
				new Jackson2JsonDecoder(collectionJsonObjectMapper, MediaTypes.COLLECTION_JSON));
		}

		if (this.hypermediaTypes.contains(HypermediaType.UBER)) {

			ObjectMapper uberObjectMapper = createUberObjectMapper(this.mapper);

			customCodecs.encoder(
				new Jackson2JsonEncoder(uberObjectMapper, MediaTypes.UBER_JSON));
			customCodecs.decoder(
				new Jackson2JsonDecoder(uberObjectMapper, MediaTypes.UBER_JSON));
		}

		customCodecs.decoder(StringDecoder.allMimeTypes());

		configurer.registerDefaults(false);
	}
}
