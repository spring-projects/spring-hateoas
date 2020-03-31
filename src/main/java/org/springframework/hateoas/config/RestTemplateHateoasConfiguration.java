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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

/**
 * Spring MVC HATEOAS Configuration
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@Configuration
class RestTemplateHateoasConfiguration {

	@Bean
	@Deprecated
	static HypermediaRestTemplateBeanPostProcessor hypermediaRestTemplateBeanPostProcessor(
			HypermediaRestTemplateConfigurer configurer) {
		return new HypermediaRestTemplateBeanPostProcessor(configurer);
	}

	@Bean
	HypermediaRestTemplateConfigurer hypermediaRestTemplateConfigurer(WebConverters converters) {
		return new HypermediaRestTemplateConfigurer(converters);
	}

	/**
	 * {@link BeanPostProcessor} to register hypermedia support with {@link RestTemplate} instances found in the
	 * application context.
	 *
	 * @author Oliver Gierke
	 * @author Greg Turnquist
	 * @deprecated Create your own @{@link Bean} definition that applies {@link HypermediaRestTemplateConfigurer}. See ref
	 *             docs.
	 */
	@Deprecated
	@RequiredArgsConstructor
	static class HypermediaRestTemplateBeanPostProcessor implements BeanPostProcessor {

		private final HypermediaRestTemplateConfigurer configurer;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
		 */
		@NonNull
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

			if (!RestTemplate.class.isInstance(bean)) {
				return bean;
			}

			return this.configurer.registerHypermediaTypes((RestTemplate) bean);
		}
	}
}
