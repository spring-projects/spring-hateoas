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

import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Assembles {@link Jackson2JsonEncoder}s and {@link Jackson2JsonDecoder}s needed to wire a {@link WebClient} with
 * hypermedia support.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 * @since 1.1
 */
public class HypermediaWebClientConfigurer {

	final WebfluxCodecCustomizer customizer;

	/**
	 * Creates a new {@link HypermediaWebClientConfigurer} for the given {@link ObjectMapper} and
	 * {@link HypermediaMappingInformation}s.
	 *
	 * @param mapper must not be {@literal null}.
	 * @param hypermediaTypes must not be {@literal null}.
	 */
	HypermediaWebClientConfigurer(WebfluxCodecCustomizer customizer) {
		this.customizer = customizer;
	}

	/**
	 * Apply the proper {@link Jackson2JsonEncoder}s and {@link Jackson2JsonDecoder}s to this {@link WebClient.Builder}.
	 *
	 * @param builder
	 * @return {@link WebClient.Builder} registered to handle hypermedia types.
	 */
	public WebClient.Builder registerHypermediaTypes(WebClient.Builder builder) {

		return builder.codecs(it -> {
			it.defaultCodecs().configureDefaultCodec(customizer);
		});
	}
}
