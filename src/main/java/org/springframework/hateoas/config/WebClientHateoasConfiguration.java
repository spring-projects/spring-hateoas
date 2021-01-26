/*
 * Copyright 2019-2021 the original author or authors.
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

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring WebFlux HATEOAS configuration.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
class WebClientHateoasConfiguration {

	@Bean
	@Lazy
	HypermediaWebClientConfigurer webClientConfigurer(ObjectProvider<ObjectMapper> mapper,
			List<HypermediaMappingInformation> hypermediaTypes) {

		WebfluxCodecCustomizer withGenericJsonTypes = new WebfluxCodecCustomizer(hypermediaTypes,
				mapper.getIfAvailable(ObjectMapper::new)).withGenericJsonTypes();

		return new HypermediaWebClientConfigurer(withGenericJsonTypes);
	}

	@Bean
	static HypermediaWebClientBeanPostProcessor webClientBeanPostProcessor(
			ObjectFactory<HypermediaWebClientConfigurer> configurer) {
		return new HypermediaWebClientBeanPostProcessor(configurer);
	}

	/**
	 * {@link BeanPostProcessor} to register the proper handlers in {@link WebClient} instances found in the application
	 * context.
	 *
	 * @author Greg Turnquist
	 * @since 1.0
	 */
	static class HypermediaWebClientBeanPostProcessor implements BeanPostProcessor {

		private final ObjectFactory<HypermediaWebClientConfigurer> configurer;

		public HypermediaWebClientBeanPostProcessor(ObjectFactory<HypermediaWebClientConfigurer> configurer) {
			this.configurer = configurer;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
		 */
		@NonNull
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

			return !WebClient.class.isInstance(bean) //
					? bean //
					: this.configurer.getObject().registerHypermediaTypes(((WebClient) bean).mutate()).build();
		}
	}
}
