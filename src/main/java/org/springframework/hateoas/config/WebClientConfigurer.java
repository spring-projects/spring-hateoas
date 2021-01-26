/*
 * Copyright 2019-2021 the original author or authors.
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

import java.util.List;

import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Assembles {@link ExchangeStrategies} needed to wire a {@link WebClient} with hypermedia support.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 * @since 1.0
 * @deprecated Migrate to {@link HypermediaWebClientConfigurer} and it's {@link WebClient.Builder}-oriented approach.
 */
@Deprecated
public class WebClientConfigurer {

	private final WebfluxCodecCustomizer customizer;

	/**
	 * Creates a new {@link WebClientConfigurer} for the given {@link ObjectMapper} and
	 * {@link HypermediaMappingInformation}s.
	 *
	 * @param mapper must not be {@literal null}.
	 * @param hypermediaTypes must not be {@literal null}.
	 */
	public WebClientConfigurer(ObjectMapper mapper, List<HypermediaMappingInformation> hypermediaTypes) {
		this.customizer = new WebfluxCodecCustomizer(hypermediaTypes, mapper);
	}

	/**
	 * Return a set of {@link ExchangeStrategies} driven by registered {@link HypermediaType}s.
	 *
	 * @return a collection of {@link Encoder}s and {@link Decoder} assembled into a {@link ExchangeStrategies}.
	 */
	public ExchangeStrategies hypermediaExchangeStrategies() {

		return ExchangeStrategies.builder() //
				.codecs(it -> it.defaultCodecs().configureDefaultCodec(customizer)) //
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
				.codecs(it -> it.defaultCodecs().configureDefaultCodec(customizer))
				.build();
	}
}
