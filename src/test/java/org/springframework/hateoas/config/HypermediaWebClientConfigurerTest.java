/*
 * Copyright 2021 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.MediaTypes.*;
import static org.springframework.hateoas.mediatype.MediaTypeTestUtils.*;
import static org.springframework.hateoas.support.ContextTester.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.mediatype.MediaTypeTestUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

public class HypermediaWebClientConfigurerTest {

	private static MediaType FRODO_JSON = MediaType.parseMediaType("application/frodo+json");

	@Test // #1224
	void webClientConfigurerHandlesSingleHypermediaType() {

		withContext(HalConfig.class, context -> {

			HypermediaWebClientConfigurer configurer = context.getBean(HypermediaWebClientConfigurer.class);
			WebClient webClient = configurer.registerHypermediaTypes(WebClient.builder()).build();

			assertThat(getSupportedHypermediaTypes(webClient)) //
					.contains(HAL_JSON) //
					.doesNotContain(HAL_FORMS_JSON, COLLECTION_JSON, UBER_JSON);
		});
	}

	@Test // #1224
	void webClientConfigurerHandlesMultipleHypermediaTypes() {

		withContext(AllHypermediaConfig.class, context -> {

			HypermediaWebClientConfigurer configurer = context.getBean(HypermediaWebClientConfigurer.class);
			WebClient webClient = configurer.registerHypermediaTypes(WebClient.builder()).build();

			assertThat(getSupportedHypermediaTypes(webClient)) //
					.contains(HAL_JSON, HAL_FORMS_JSON, COLLECTION_JSON, UBER_JSON);
		});
	}

	@Test // #1224
	void webClientConfigurerHandlesCustomHypermediaTypes() {

		withContext(CustomHypermediaConfig.class, context -> {

			HypermediaWebClientConfigurer configurer = context.getBean(HypermediaWebClientConfigurer.class);
			WebClient webClient = configurer.registerHypermediaTypes(WebClient.builder()).build();

			assertThat(MediaTypeTestUtils.getSupportedHypermediaTypes(webClient)) //
					.contains(HAL_JSON, FRODO_JSON) //
					.doesNotContain(HAL_FORMS_JSON, COLLECTION_JSON, UBER_JSON);
		});
	}

	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class HalConfig {

	}

	@EnableHypermediaSupport(
			type = { HypermediaType.HAL, HypermediaType.HAL_FORMS, HypermediaType.COLLECTION_JSON, HypermediaType.UBER })
	static class AllHypermediaConfig {

	}

	static class CustomHypermediaConfig extends HalConfig {

		@Bean
		HypermediaMappingInformation hypermediaMappingInformation() {
			return () -> Collections.singletonList(FRODO_JSON);
		}
	}
}
