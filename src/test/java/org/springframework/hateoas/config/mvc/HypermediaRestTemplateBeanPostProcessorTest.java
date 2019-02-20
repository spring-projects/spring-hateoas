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
package org.springframework.hateoas.config.mvc;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.springframework.hateoas.support.ContextTester.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * @author Greg Turnquist
 */
public class HypermediaRestTemplateBeanPostProcessorTest {

	@Test
	public void shouldRegisterJustHal() {

		withContext(HalConfig.class, context -> {

			assertThat(lookupSupportedHypermediaTypes(context.getBean(RestTemplate.class)))
				.containsExactlyInAnyOrder(
					MediaTypes.HAL_JSON,
					MediaTypes.HAL_JSON_UTF8,
					MediaType.APPLICATION_JSON,
					MediaType.parseMediaType("application/*+json"));
		});
	}

	@Test
	public void shouldRegisterHalAndCollectionJsonMessageConverters() {

		withContext(HalAndCollectionJsonConfig.class, context -> {

			assertThat(lookupSupportedHypermediaTypes(context.getBean(RestTemplate.class)))
				.containsExactlyInAnyOrder(
					MediaTypes.HAL_JSON,
					MediaTypes.HAL_JSON_UTF8,
					MediaTypes.COLLECTION_JSON,
					MediaType.APPLICATION_JSON,
					MediaType.parseMediaType("application/*+json"));
		});
	}

	@Test
	public void shouldRegisterHypermediaMessageConverters() {

		withContext(AllHypermediaConfig.class, context -> {

			assertThat(lookupSupportedHypermediaTypes(context.getBean(RestTemplate.class)))
				.containsExactlyInAnyOrder(
					MediaTypes.HAL_JSON,
					MediaTypes.HAL_JSON_UTF8,
					MediaTypes.HAL_FORMS_JSON,
					MediaTypes.COLLECTION_JSON,
					MediaTypes.UBER_JSON,
					MediaType.APPLICATION_JSON,
					MediaType.parseMediaType("application/*+json"));
		});
	}

	private List<MediaType> lookupSupportedHypermediaTypes(RestTemplate restTemplate) {
		
		return restTemplate.getMessageConverters().stream()
			.filter(MappingJackson2HttpMessageConverter.class::isInstance)
			.map(AbstractJackson2HttpMessageConverter.class::cast)
			.map(AbstractHttpMessageConverter::getSupportedMediaTypes)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
	}

	static class BaseConfig {

		@Bean
		RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class HalConfig extends BaseConfig {
	}

	@Configuration
	@EnableHypermediaSupport(type = {HypermediaType.HAL, HypermediaType.COLLECTION_JSON})
	static class HalAndCollectionJsonConfig extends BaseConfig {
	}

	@Configuration
	@EnableHypermediaSupport(type = {HypermediaType.HAL, HypermediaType.HAL_FORMS, HypermediaType.COLLECTION_JSON, HypermediaType.UBER})
	static class AllHypermediaConfig extends BaseConfig {
	}
}