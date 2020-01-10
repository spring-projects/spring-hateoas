/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.hateoas.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.CodecConfigurer.CustomCodecs;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Assembles {@link ExchangeStrategies} needed to wire a {@link WebClient} with hypermedia support.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 * @since 1.0
 */
@Configuration
public class WebClientConfigurer {

	Consumer<ClientCodecConfigurer> configurer;

	/**
	 * Creates a new {@link WebClientConfigurer} for the given {@link ObjectMapper} and
	 * {@link HypermediaMappingInformation}s.
	 *
	 * @param mapper must not be {@literal null}.
	 * @param hypermediaTypes must not be {@literal null}.
	 */
	public WebClientConfigurer(ObjectMapper mapper, List<HypermediaMappingInformation> hypermediaTypes) {

		Assert.notNull(mapper, "ObjectMapper must not be null!");
		Assert.notNull(hypermediaTypes, "HypermediaMappingInformations must not be null!");

		List<Encoder<?>> encoders = new ArrayList<>();
		List<AbstractJackson2Decoder> decoders = new ArrayList<>();

		hypermediaTypes.forEach(hypermedia -> {

			ObjectMapper objectMapper = hypermedia.configureObjectMapper(mapper.copy());
			MimeType[] mimeTypes = hypermedia.getMediaTypes().toArray(new MimeType[0]);

			encoders.add(new Jackson2JsonEncoder(objectMapper, mimeTypes));
			decoders.add(new Jackson2JsonDecoder(objectMapper, mimeTypes));
		});

		this.configurer = it -> {

			CustomCodecs codecs = it.customCodecs();

			encoders.forEach(codecs::encoder);
			decoders.stream() //
					.peek(applyDefaultConfiguration(codecs)) //
					.forEach(codecs::decoder);
		};
	}

	/**
	 * Return a set of {@link ExchangeStrategies} driven by registered {@link HypermediaType}s.
	 *
	 * @return a collection of {@link Encoder}s and {@link Decoder} assembled into a {@link ExchangeStrategies}.
	 */
	public ExchangeStrategies hypermediaExchangeStrategies() {

		return ExchangeStrategies.builder() //
				.codecs(configurer) //
				.build();
	}

	/**
	 * Register the proper {@link ExchangeStrategies} for a given {@link WebClient}.
	 *
	 * @param webClient
	 * @return mutated webClient with hypermedia support.
	 */
	public WebClient registerHypermediaTypes(WebClient webClient) {

		return webClient.mutate() //
				.exchangeStrategies(it -> it.codecs(configurer)) //
				.build();
	}

	/**
	 * Returns a {@link Consumer} of {@link AbstractJackson2Decoder} that will copy the default configuration of the given
	 * {@link CustomCodecs} to a decoder.
	 *
	 * @param codecs must not be {@literal null}.
	 * @return
	 */
	private static Consumer<AbstractJackson2Decoder> applyDefaultConfiguration(CustomCodecs codecs) {

		return decoder -> codecs.withDefaultCodecConfig(config -> {

			Integer maxInMemorySize = config.maxInMemorySize();

			if (maxInMemorySize != null) {
				decoder.setMaxInMemorySize(maxInMemorySize);
			}
		});
	}
}
