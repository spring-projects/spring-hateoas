/*
 * Copyright 2015-2018 the original author or authors.
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

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.plugin.core.support.PluginRegistryFactoryBean;
import org.springframework.util.ClassUtils;

/**
 * Common HATEOAS specific configuration.
 *
 * @author Oliver Gierke
 * @soundtrack Elephants Crossing - Wait (Live at Stadtfest Dresden)
 * @since 0.19
 */
@Configuration
@EnablePluginRegistries({ LinkDiscoverer.class })
class HateoasConfiguration {

	/**
	 * The {@link MessageSourceAccessor} to provide messages for {@link ResourceDescription}s being rendered.
	 * 
	 * @return
	 */
	@Bean
	public MessageSourceAccessor linkRelationMessageSource() {

		try {

			ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
			messageSource.setBasename("classpath:rest-messages");

			return new MessageSourceAccessor(messageSource);

		} catch (Exception o_O) {
			throw new BeanCreationException("resourceDescriptionMessageSourceAccessor", "", o_O);
		}
	}

	@Bean
	ConverterRegisteringBeanPostProcessor jackson2ModuleRegisteringBeanPostProcessor(
			ObjectFactory<ConverterRegisteringWebMvcConfigurer> configurer) {
		return new ConverterRegisteringBeanPostProcessor(configurer);
	}

	// RelProvider

	@Bean
	RelProvider defaultRelProvider() {

		return ClassUtils.isPresent("org.atteo.evo.inflector.English", null) //
				? new EvoInflectorRelProvider()
				: new DefaultRelProvider();
	}

	@Bean
	AnnotationRelProvider annotationRelProvider() {
		return new AnnotationRelProvider();
	}

	@Primary
	@Bean
	DelegatingRelProvider _relProvider(PluginRegistry<RelProvider, Class<?>> relProviderPluginRegistry) {
		return new DelegatingRelProvider(relProviderPluginRegistry);
	}

	@Bean
	PluginRegistryFactoryBean<RelProvider, Class<?>> relProviderPluginRegistry() {

		PluginRegistryFactoryBean<RelProvider, Class<?>> factory = new PluginRegistryFactoryBean<RelProvider, Class<?>>();

		factory.setType(RelProvider.class);
		factory.setExclusions(new Class[] { DelegatingRelProvider.class });

		return factory;
	}

	// LinkDiscoverers

	@Bean
	LinkDiscoverers linkDiscoverers(PluginRegistry<LinkDiscoverer, MediaType> linkDiscovererRegistry) {
		return new LinkDiscoverers(linkDiscovererRegistry);
	}
}
