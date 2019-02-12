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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.hateoas.hal.forms.HalFormsConfiguration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Assembles {@link ExchangeStrategies} needed to wire a {@link WebClient} with hypermedia support.
 * 
 * @author Greg Turnquist
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfigurer implements BeanFactoryAware {

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
	 * Return a set of {@link ExchangeStrategies} driven by registered {@link HypermediaType}s.
	 * @return a collection of {@link Encoder}s and {@link Decoder} assembled into a {@link ExchangeStrategies}.
	 */
	public ExchangeStrategies hypermediaExchangeStrategies() {

		MessageSourceAccessor linkRelationMessageSource = this.beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME,
			MessageSourceAccessor.class);

		List<Encoder<?>> encoders = new ArrayList<>();
		List<Decoder<?>> decoders = new ArrayList<>();

		if (this.hypermediaTypes.contains(HypermediaType.HAL)) {

			ObjectMapper halObjectMapper = createHalObjectMapper(this.mapper, this.curieProvider, this.relProvider,
				linkRelationMessageSource, this.halConfiguration);

			encoders.add(new Jackson2JsonEncoder(halObjectMapper, MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8));
			decoders.add(new Jackson2JsonDecoder(halObjectMapper, MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8));
		}

		if (this.hypermediaTypes.contains(HypermediaType.HAL_FORMS)) {

			ObjectMapper halFormsObjectMapper = createHalFormsObjectMapper(this.mapper, this.curieProvider,
				this.relProvider, linkRelationMessageSource, this.halFormsConfiguration);

			encoders.add(new Jackson2JsonEncoder(halFormsObjectMapper, MediaTypes.HAL_FORMS_JSON));
			decoders.add(new Jackson2JsonDecoder(halFormsObjectMapper, MediaTypes.HAL_FORMS_JSON));
		}

		if (this.hypermediaTypes.contains(HypermediaType.COLLECTION_JSON)) {

			ObjectMapper collectionJsonObjectMapper = createCollectionJsonObjectMapper(this.mapper, linkRelationMessageSource);

			encoders.add(new Jackson2JsonEncoder(collectionJsonObjectMapper, MediaTypes.COLLECTION_JSON));
			decoders.add(new Jackson2JsonDecoder(collectionJsonObjectMapper, MediaTypes.COLLECTION_JSON));
		}

		if (this.hypermediaTypes.contains(HypermediaType.UBER)) {

			ObjectMapper uberObjectMapper = createUberObjectMapper(this.mapper);

			encoders.add(new Jackson2JsonEncoder(uberObjectMapper, MediaTypes.UBER_JSON));
			decoders.add(new Jackson2JsonDecoder(uberObjectMapper, MediaTypes.UBER_JSON));
		}

		encoders.add(CharSequenceEncoder.allMimeTypes());
		decoders.add(StringDecoder.allMimeTypes());

		return ExchangeStrategies.builder()
			.codecs(clientCodecConfigurer -> {

				encoders.forEach(encoder -> clientCodecConfigurer.customCodecs().encoder(encoder));
				decoders.forEach(decoder -> clientCodecConfigurer.customCodecs().decoder(decoder));

				clientCodecConfigurer.registerDefaults(false);
			})
			.build();
	}

	/**
	 * Register the proper {@link ExchangeStrategies} for a given {@link WebClient}.
	 *
	 * @param webClient
	 * @return mutated webClient with hypermedia support.
	 */
	public WebClient registerHypermediaTypes(WebClient webClient) {

		return webClient.mutate()
			.exchangeStrategies(hypermediaExchangeStrategies())
			.build();
	}
}

