/*
 * Copyright 2019 the original author or authors.
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

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring WebFlux HATEOAS configuration.
 *
 * @author Greg Turnquist
 * @since 1.0 TODO: Inspect ApplicationContext -> WebApplicationContext -> WebMVC
 */
@Configuration
class WebFluxHateoasConfiguration {

	@Bean
	HypermediaWebFluxConfigurer hypermediaWebFluxConfigurer(ObjectProvider<ObjectMapper> mapper,
			List<HypermediaMappingInformation> hypermediaTypes) {

		return new HypermediaWebFluxConfigurer(mapper.getIfAvailable(ObjectMapper::new), hypermediaTypes);
	}

	@Bean
	@Lazy
	ServerWebExchangeContextFilter serverWebExchangeContextFilter() {
		return new ServerWebExchangeContextFilter();
	}

	/**
	 * {@link WebFluxConfigurer} to register hypermedia-aware {@link org.springframework.core.codec.Encoder}s and
	 * {@link org.springframework.core.codec.Decoder}s that will render hypermedia for WebFlux controllers.
	 *
	 * @author Greg Turnquist
	 * @since 1.0
	 */
	@RequiredArgsConstructor
	static class HypermediaWebFluxConfigurer implements WebFluxConfigurer {

		private final ObjectMapper mapper;
		private final List<HypermediaMappingInformation> hypermediaTypes;

		/**
		 * Configure custom HTTP message readers and writers or override built-in ones.
		 * <p>
		 * The configured readers and writers will be used for both annotated controllers and functional endpoints.
		 *
		 * @param configurer the configurer to use
		 */
		@Override
		public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {

			this.hypermediaTypes.forEach(hypermedia -> {

				ObjectMapper objectMapper = hypermedia.configureObjectMapper(this.mapper.copy());
				MimeType[] mimeTypes = hypermedia.getMediaTypes().toArray(new MimeType[0]);
				
				configurer.customCodecs().encoder(new Jackson2JsonEncoder(objectMapper, mimeTypes));
				configurer.customCodecs().decoder(new Jackson2JsonDecoder(objectMapper, mimeTypes));
			});
		}
	}
}
