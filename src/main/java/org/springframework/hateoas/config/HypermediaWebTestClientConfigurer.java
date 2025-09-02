/*
 * Copyright 2019-2024 the original author or authors.
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

import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClientConfigurer;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

/**
 * Assembles {@link Jackson2JsonEncoder}s and {@link AbstractJacksonDecoder}s needed to wire a {@link WebTestClient}
 * with hypermedia support.
 *
 * @author Greg Turnquist
 * @since 1.1
 */
public class HypermediaWebTestClientConfigurer implements WebTestClientConfigurer {

	private Consumer<ClientCodecConfigurer> configurer;

	/**
	 * Creates a new {@link HypermediaWebTestClientConfigurer} for the given {@link JsonMapper} and
	 * {@link HypermediaMappingInformation}s.
	 *
	 * @param mapper must not be {@literal null}.
	 * @param hypermediaTypes must not be {@literal null}.
	 */
	HypermediaWebTestClientConfigurer(JsonMapper mapper, List<HypermediaMappingInformation> hypermediaTypes) {

		Assert.notNull(mapper, "mapper must not be null!");
		Assert.notNull(hypermediaTypes, "hypermediaTypes must not be null!");

		this.configurer = clientCodecConfigurer -> hypermediaTypes.forEach(hypermediaType -> {

			var customized = hypermediaType.configureJsonMapper(mapper.rebuild()).build();
			var mimeTypes = hypermediaType.getMediaTypes().toArray(new MimeType[0]);

			clientCodecConfigurer.customCodecs().registerWithDefaultConfig(new JacksonJsonEncoder(customized, mimeTypes));
			clientCodecConfigurer.customCodecs().registerWithDefaultConfig(new JacksonJsonDecoder(customized, mimeTypes));
		});
	}

	/**
	 * Register the proper {@link Jackson2JsonEncoder}s and {@link AbstractJacksonDecoder}s for a given
	 * {@link WebTestClient}.
	 */
	@Override
	public void afterConfigurerAdded(WebTestClient.Builder builder, //
			@Nullable WebHttpHandlerBuilder webHttpHandlerBuilder, //
			@Nullable ClientHttpConnector clientHttpConnector) {
		builder.codecs(this.configurer);
	}
}
