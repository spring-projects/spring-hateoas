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

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.CodecConfigurer.CustomCodecs;
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
 * @author Oliver Drotbohm
 * @since 1.0
 */
@Configuration
class WebFluxHateoasConfiguration {

	@Bean
	WebFluxCodecs hypermediaConverters(ObjectProvider<ObjectMapper> mapper,
			List<HypermediaMappingInformation> mappingInformation) {
		return new WebFluxCodecs(mapper.getIfAvailable(ObjectMapper::new), mappingInformation);
	}

	@Bean
	HypermediaWebFluxConfigurer hypermediaWebFluxConfigurer(ObjectProvider<ObjectMapper> mapper,
			List<HypermediaMappingInformation> mappingInformation) {

		WebFluxCodecs codecs = new WebFluxCodecs(mapper.getIfAvailable(ObjectMapper::new), mappingInformation);

		return new HypermediaWebFluxConfigurer(codecs);
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

		private final WebFluxCodecs codecs;

		/**
		 * Configure custom HTTP message readers and writers or override built-in ones.
		 * <p>
		 * The configured readers and writers will be used for both annotated controllers and functional endpoints.
		 *
		 * @param configurer the configurer to use, must not be {@literal null}.
		 */
		@Override
		public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
			codecs.registerCodecs(configurer.customCodecs());
		}
	}

	private static class WebFluxCodecs {

		private final List<Decoder<?>> decoders;
		private final List<Encoder<?>> encoders;

		private WebFluxCodecs(ObjectMapper mapper, List<HypermediaMappingInformation> mappingInformation) {

			this.decoders = new ArrayList<>();
			this.encoders = new ArrayList<>();

			for (HypermediaMappingInformation information : mappingInformation) {

				ObjectMapper objectMapper = information.configureObjectMapper(mapper.copy());
				List<MediaType> mediaTypes = information.getMediaTypes();

				this.decoders.add(getDecoder(objectMapper, mediaTypes));
				this.encoders.add(getEncoder(objectMapper, mediaTypes));
			}
		}

		public void registerCodecs(CustomCodecs codecs) {

			decoders.forEach(codecs::decoder);
			encoders.forEach(codecs::encoder);
		}

		private static Decoder<?> getDecoder(ObjectMapper mapper, List<MediaType> mediaTypes) {
			return new Jackson2JsonDecoder(mapper, mediaTypes.toArray(new MimeType[0]));
		}

		private static Encoder<?> getEncoder(ObjectMapper mapper, List<MediaType> mediaTypes) {
			return new Jackson2JsonEncoder(mapper, mediaTypes.toArray(new MimeType[0]));
		}
	}
}
