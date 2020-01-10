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

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring WebFlux HATEOAS configuration.
 *
 * @author Greg Turnquist
 * @since 1.0 TODO: Inspect ApplicationContext -> WebApplicationContext -> WebMVC
 */
@Configuration
class WebClientHateoasConfiguration {

	@Bean
	WebClientConfigurer webClientConfigurer(ObjectProvider<ObjectMapper> mapper,
			List<HypermediaMappingInformation> hypermediaTypes) {
		return new WebClientConfigurer(mapper.getIfAvailable(ObjectMapper::new), hypermediaTypes);
	}

	@Bean
	static HypermediaWebClientBeanPostProcessor webClientBeanPostProcessor(
			ObjectProvider<WebClientConfigurer> configurer) {
		return new HypermediaWebClientBeanPostProcessor(configurer);
	}

	/**
	 * {@link BeanPostProcessor} to register the proper handlers in {@link WebClient} instances found in the application
	 * context.
	 *
	 * @author Greg Turnquist
	 * @since 1.0
	 */
	@RequiredArgsConstructor
	static class HypermediaWebClientBeanPostProcessor implements BeanPostProcessor {

		private final ObjectProvider<WebClientConfigurer> configurer;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
		 */
		@NonNull
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

			if (bean instanceof WebClient) {
				return this.configurer.getObject().registerHypermediaTypes((WebClient) bean);
			}

			return bean;
		}
	}
}
