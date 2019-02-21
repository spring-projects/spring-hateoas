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

import lombok.RequiredArgsConstructor;

import java.util.Collection;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.hateoas.config.Hypermedia;
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
public class HypermediaWebFluxConfigurer implements WebFluxConfigurer {

	private final ObjectMapper mapper;
	private final Collection<Hypermedia> hypermediaTypes;

	/**
	 * Configure custom HTTP message readers and writers or override built-in ones.
	 * <p>
	 * The configured readers and writers will be used for both annotated controllers and functional endpoints.
	 *
	 * @param configurer the configurer to use
	 */
	@Override
	public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {

		CodecConfigurer.CustomCodecs customCodecs = configurer.customCodecs();

		this.hypermediaTypes.forEach(hypermedia -> {

			ObjectMapper objectMapper = hypermedia.createObjectMapper(this.mapper);
			customCodecs.encoder(new Jackson2JsonEncoder(objectMapper, hypermedia.getMimeTypes()));
			customCodecs.decoder(new Jackson2JsonDecoder(objectMapper, hypermedia.getMimeTypes()));
		});

		customCodecs.encoder(CharSequenceEncoder.allMimeTypes());
		customCodecs.decoder(StringDecoder.allMimeTypes());

		configurer.registerDefaults(false);
	}
}
