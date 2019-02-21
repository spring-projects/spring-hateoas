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
package org.springframework.hateoas.uber;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.Hypermedia;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Greg Turnquist
 */
@Configuration
public class UberConfigurer {

	@Bean
	Hypermedia uberBean() {

		return new Hypermedia() {
			@Override
			public List<MediaType> getMediaTypes() {
				return HypermediaType.UBER.getMediaTypes();
			}

			@Override
			public ObjectMapper createObjectMapper(ObjectMapper mapper) {

				ObjectMapper mapper1 = mapper.copy();

				mapper1.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
				mapper1.registerModule(new Jackson2UberModule());

				return mapper1;
			}
		};
	}

	@Bean
	LinkDiscoverer linkDiscoverer() {
		return new UberLinkDiscoverer();
	}

}
