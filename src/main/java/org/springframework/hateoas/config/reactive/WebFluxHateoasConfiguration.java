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

import java.util.Collection;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.Hypermedia;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.hateoas.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.reactive.HypermediaWebFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring WebFlux HATEOAS configuration.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
@Configuration
public class WebFluxHateoasConfiguration {

	@Bean
	WebClientConfigurer webClientConfigurer(ObjectProvider<ObjectMapper> mapper, DelegatingRelProvider relProvider,
			ObjectProvider<CurieProvider> curieProvider, ObjectProvider<HalConfiguration> halConfiguration,
			ObjectProvider<HalFormsConfiguration> halFormsConfiguration, Collection<Hypermedia> hypermediaTypes) {

		return new WebClientConfigurer(mapper.getIfAvailable(ObjectMapper::new), relProvider,
				curieProvider.getIfAvailable(), halConfiguration.getIfAvailable(HalConfiguration::new),
				halFormsConfiguration.getIfAvailable(HalFormsConfiguration::new), hypermediaTypes);
	}

	@Bean
	HypermediaWebClientBeanPostProcessor webClientBeanPostProcessor(WebClientConfigurer configurer) {
		return new HypermediaWebClientBeanPostProcessor(configurer);
	}

	@Bean
	HypermediaWebFluxConfigurer hypermediaWebFluxConfigurer(ObjectProvider<ObjectMapper> mapper,
			DelegatingRelProvider relProvider, ObjectProvider<CurieProvider> curieProvider,
			ObjectProvider<HalConfiguration> halConfiguration, ObjectProvider<HalFormsConfiguration> halFormsConfiguration,
			Collection<Hypermedia> hypermediaTypes) {

		return new HypermediaWebFluxConfigurer(mapper.getIfAvailable(ObjectMapper::new), relProvider,
				curieProvider.getIfAvailable(), halConfiguration.getIfAvailable(HalConfiguration::new),
				halFormsConfiguration.getIfAvailable(HalFormsConfiguration::new), hypermediaTypes);
	}

	/**
	 * TODO: Replace with Spring Framework filter when https://github.com/spring-projects/spring-framework/issues/21746 is
	 * completed.
	 */
	@Bean
	HypermediaWebFilter hypermediaWebFilter() {
		return new HypermediaWebFilter();
	}
}
