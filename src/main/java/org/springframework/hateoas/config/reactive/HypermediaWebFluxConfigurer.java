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
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.Hypermedia;
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
	private final Collection<Hypermedia> hypermediaTypes;

	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Configure custom HTTP message readers and writers or override built-in ones.
	 * <p>
	 * The configured readers and writers will be used for both annotated controllers and functional endpoints.
	 *
	 * @param configurer the configurer to use
	 */
	@Override
	public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {

		if (configurer.getReaders().stream()
				.flatMap(httpMessageReader -> httpMessageReader.getReadableMediaTypes().stream())
				.anyMatch(mediaType -> mediaType == MediaTypes.HAL_JSON || mediaType == MediaTypes.HAL_JSON_UTF8
						|| mediaType == MediaTypes.HAL_FORMS_JSON || mediaType == MediaTypes.COLLECTION_JSON
						|| mediaType == MediaTypes.UBER_JSON)) {

			// Already configured for hypermedia!
			return;
		}

		MessageSourceAccessor linkRelationMessageSource = this.beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME,
				MessageSourceAccessor.class);

		CodecConfigurer.CustomCodecs customCodecs = configurer.customCodecs();

		this.hypermediaTypes.forEach(hypermedia -> {

			ObjectMapper objectMapper;

			if (hypermedia == HypermediaType.HAL) {

				objectMapper = createHalObjectMapper(this.mapper, this.curieProvider, this.relProvider,
					linkRelationMessageSource, this.halConfiguration);

			} else if (hypermedia == HypermediaType.HAL_FORMS) {

				objectMapper = createHalFormsObjectMapper(this.mapper, this.curieProvider,
					this.relProvider, linkRelationMessageSource, this.halFormsConfiguration);

			} else if (hypermedia == HypermediaType.COLLECTION_JSON) {

				objectMapper = createCollectionJsonObjectMapper(this.mapper);

			} else if (hypermedia == HypermediaType.UBER) {

				objectMapper = createUberObjectMapper(this.mapper);

			} else {

				objectMapper = hypermedia.createObjectMapper(this.mapper);
			}

			customCodecs.encoder(new Jackson2JsonEncoder(objectMapper, hypermedia.getMimeTypes()));
			customCodecs.decoder(new Jackson2JsonDecoder(objectMapper, hypermedia.getMimeTypes()));
		});

		customCodecs.encoder(CharSequenceEncoder.allMimeTypes());
		customCodecs.decoder(StringDecoder.allMimeTypes());

		configurer.registerDefaults(false);
	}
}
