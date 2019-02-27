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
package org.springframework.hateoas.config;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.hateoas.server.mvc.UriComponentsContributor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring MVC HATEOAS Configuration
 *
 * @author Greg Turnquist
 */
@Configuration
class WebMvcHateoasConfiguration {

	@Bean
	HypermediaWebMvcConfigurer hypermediaWebMvcConfigurer(ObjectProvider<ObjectMapper> mapper,
			Collection<HypermediaMappingInformation> hypermediaTypes) {

		return new HypermediaWebMvcConfigurer(mapper.getIfAvailable(ObjectMapper::new), hypermediaTypes);
	}

	@Bean
	HypermediaRestTemplateBeanPostProcessor restTemplateBeanPostProcessor(HypermediaWebMvcConfigurer configurer) {
		return new HypermediaRestTemplateBeanPostProcessor(configurer);
	}

	@Bean
	WebMvcLinkBuilderFactory webMvcLinkBuilderFactory(ObjectProvider<UriComponentsContributor> contributors) {

		WebMvcLinkBuilderFactory factory = new WebMvcLinkBuilderFactory();
		factory.setUriComponentsContributors(contributors.stream().collect(Collectors.toList()));

		return factory;
	}

	/**
	 * @author Oliver Gierke
	 * @author Greg Turnquist
	 */
	@RequiredArgsConstructor
	static class HypermediaWebMvcConfigurer implements WebMvcConfigurer {

		private final ObjectMapper mapper;
		private final Collection<HypermediaMappingInformation> hypermediaTypes;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#extendMessageConverters(java.util.List)
		 */
		@Override
		public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

			this.hypermediaTypes.forEach(hypermedia -> {

				converters.add(0, new TypeConstrainedMappingJackson2HttpMessageConverter(RepresentationModel.class,
						hypermedia.getMediaTypes(), hypermedia.configureObjectMapper(mapper.copy())));
			});
		}
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

		private final HypermediaWebMvcConfigurer configurer;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

			if (!RestTemplate.class.isInstance(bean)) {
				return bean;
			}

			configurer.extendMessageConverters(((RestTemplate) bean).getMessageConverters());

			return bean;
		}
	}
}
