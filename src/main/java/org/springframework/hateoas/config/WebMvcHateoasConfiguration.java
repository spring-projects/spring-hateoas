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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.RepresentationModelProcessorHandlerMethodReturnValueHandler;
import org.springframework.hateoas.server.mvc.RepresentationModelProcessorInvoker;
import org.springframework.hateoas.server.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.hateoas.server.mvc.UriComponentsContributor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

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
	HypermediaRepresentationModelBeanProcessorPostProcessor hypermediaRepresentionModelProcessorConfigurator(
			List<RepresentationModelProcessor<?>> processors) {

		return new HypermediaRepresentationModelBeanProcessorPostProcessor(processors);
	}

	@Bean
	static HypermediaRestTemplateBeanPostProcessor restTemplateBeanPostProcessor(
			ObjectProvider<HypermediaWebMvcConfigurer> configurer) {
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
	 * @author Greg Turnquist
	 */
	@RequiredArgsConstructor
	static class HypermediaRepresentationModelBeanProcessorPostProcessor implements BeanPostProcessor {

		private final List<RepresentationModelProcessor<?>> processors;

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

			if (RequestMappingHandlerAdapter.class.isInstance(bean)) {

				RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) bean;

				HandlerMethodReturnValueHandlerComposite delegate = new HandlerMethodReturnValueHandlerComposite();
				delegate.addHandlers(adapter.getReturnValueHandlers());

				RepresentationModelProcessorHandlerMethodReturnValueHandler handler = new RepresentationModelProcessorHandlerMethodReturnValueHandler(
						delegate, new RepresentationModelProcessorInvoker(processors));

				adapter.setReturnValueHandlers(Collections.singletonList(handler));
			}

			return bean;
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

		private final ObjectProvider<HypermediaWebMvcConfigurer> configurer;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

			if (!RestTemplate.class.isInstance(bean)) {
				return bean;
			}

			configurer.getObject().extendMessageConverters(((RestTemplate) bean).getMessageConverters());

			return bean;
		}
	}
}
