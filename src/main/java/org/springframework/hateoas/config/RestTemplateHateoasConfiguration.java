/*
 * Copyright 2019 the original author or authors.
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
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring MVC HATEOAS Configuration
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@Configuration
class RestTemplateHateoasConfiguration {

	@Bean
	static HypermediaRestTemplateBeanPostProcessor hypermediaRestTemplateBeanPostProcessor(
			ObjectProvider<ObjectMapper> mapper, List<HypermediaMappingInformation> hypermediaTypes) {
		return new HypermediaRestTemplateBeanPostProcessor(mapper.getIfAvailable(ObjectMapper::new), hypermediaTypes);
	}

	/**
	 * {@link BeanPostProcessor} to register hypermedia support with {@link RestTemplate} instances found in the
	 * application context.
	 *
	 * @author Oliver Gierke
	 * @author Greg Turnquist
	 */
	@RequiredArgsConstructor
	static class HypermediaRestTemplateBeanPostProcessor implements BeanPostProcessor {

		private final ObjectMapper mapper;
		private final List<HypermediaMappingInformation> hypermediaTypes;

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

			this.hypermediaTypes.forEach(hypermedia -> {

				((RestTemplate) bean).getMessageConverters().add(0, new TypeConstrainedMappingJackson2HttpMessageConverter(
						RepresentationModel.class, hypermedia.getMediaTypes(), hypermedia.configureObjectMapper(mapper.copy())));
			});

			return bean;
		}
	}
}
