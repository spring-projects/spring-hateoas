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
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;
import static org.springframework.hateoas.mediatype.MediaTypeTestUtils.*;
import static org.springframework.hateoas.support.ContextTester.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class HypermediaRestTemplateConfigurerTest {

	private static MediaType FRODO_JSON = MediaType.parseMediaType("application/frodo+json");

	@Test // #1223
	void webConvertersShouldAddHypermediaMessageConverters() {

		withContext(HalConfig.class, context -> {

			HypermediaRestTemplateConfigurer configurer = context.getBean(HypermediaRestTemplateConfigurer.class);
			RestTemplate restTemplate = configurer.registerHypermediaTypes(new RestTemplate());

			assertThat(getSupportedHypermediaTypes(restTemplate.getMessageConverters())) //
					.contains(MediaTypes.HAL_JSON) //
					.doesNotContain(MediaTypes.HAL_FORMS_JSON, MediaTypes.COLLECTION_JSON, MediaTypes.UBER_JSON);
		});
	}

	@Test // #1223
	void webConvertersShouldAddAllHypermediaMessageConverters() {

		withContext(AllHypermediaConfig.class, context -> {

			HypermediaRestTemplateConfigurer configurer = context.getBean(HypermediaRestTemplateConfigurer.class);
			RestTemplate restTemplate = configurer.registerHypermediaTypes(new RestTemplate());

			assertThat(getSupportedHypermediaTypes(restTemplate.getMessageConverters())) //
					.contains(MediaTypes.HAL_JSON, MediaTypes.HAL_FORMS_JSON, MediaTypes.COLLECTION_JSON, MediaTypes.UBER_JSON);
		});
	}

	@Test // #1223
	void webConvertersShouldSupportCustomHypermediaTypes() {

		withContext(CustomHypermediaConfig.class, context -> {

			HypermediaRestTemplateConfigurer configurer = context.getBean(HypermediaRestTemplateConfigurer.class);
			RestTemplate restTemplate = configurer.registerHypermediaTypes(new RestTemplate());

			assertThat(getSupportedHypermediaTypes(restTemplate.getMessageConverters())) //
					.contains(MediaTypes.HAL_JSON, FRODO_JSON) //
					.doesNotContain(MediaTypes.HAL_FORMS_JSON, MediaTypes.COLLECTION_JSON, MediaTypes.UBER_JSON);
		});
	}

	@EnableHypermediaSupport(type = HAL)
	static class HalConfig {

	}

	@EnableHypermediaSupport(type = { HAL, HAL_FORMS, COLLECTION_JSON, UBER })
	static class AllHypermediaConfig {

	}

	static class CustomHypermediaConfig extends HalConfig {

		@Bean
		HypermediaMappingInformation hypermediaMappingInformation() {
			return () -> Collections.singletonList(FRODO_JSON);
		}
	}
}
