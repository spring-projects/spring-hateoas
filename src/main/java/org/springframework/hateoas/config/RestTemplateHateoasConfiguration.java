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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

/**
 * Spring MVC HATEOAS Configuration
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@Configuration(proxyBeanMethods = false)
class RestTemplateHateoasConfiguration {

	@Bean
	static HypermediaRestTemplateBeanPostProcessor hypermediaRestTemplateBeanPostProcessor(
			ObjectFactory<HypermediaRestTemplateConfigurer> configurer) {
		return new HypermediaRestTemplateBeanPostProcessor(configurer);
	}

	@Bean
	@Lazy
	HypermediaRestTemplateConfigurer hypermediaRestTemplateConfigurer(WebConverters converters) {
		return new HypermediaRestTemplateConfigurer(converters);
	}

	/**
	 * {@link BeanPostProcessor} to register hypermedia support with {@link RestTemplate} instances found in the
	 * application context.
	 *
	 * @author Oliver Gierke
	 * @author Greg Turnquist
	 */
	static class HypermediaRestTemplateBeanPostProcessor implements BeanPostProcessor {

		private final ObjectFactory<HypermediaRestTemplateConfigurer> configurer;

		public HypermediaRestTemplateBeanPostProcessor(ObjectFactory<HypermediaRestTemplateConfigurer> configurer) {
			this.configurer = configurer;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
		 */
		@NonNull
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

			return !RestTemplate.class.isInstance(bean) //
					? bean
					: this.configurer.getObject().registerHypermediaTypes((RestTemplate) bean);
		}
	}
}
