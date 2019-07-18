/*
 * Copyright 2015-2019 the original author or authors.
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

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.LinkRelationProvider.LookupContext;
import org.springframework.hateoas.server.core.AnnotationLinkRelationProvider;
import org.springframework.hateoas.server.core.DefaultLinkRelationProvider;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.plugin.core.support.PluginRegistryFactoryBean;
import org.springframework.util.ClassUtils;

/**
 * Common HATEOAS specific configuration.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @soundtrack Elephants Crossing - Wait (Live at Stadtfest Dresden)
 * @since 0.19
 */
@Configuration
@Import(EntityLinksConfiguration.class)
@EnablePluginRegistries({ LinkDiscoverer.class })
class HateoasConfiguration {

	@Bean
	public MessageResolver messageResolver() {
		return MessageResolver.of(lookupMessageSource());
	}

	// RelProvider

	@Bean
	LinkRelationProvider defaultRelProvider() {

		return ClassUtils.isPresent("org.atteo.evo.inflector.English", null) //
				? new EvoInflectorLinkRelationProvider()
				: new DefaultLinkRelationProvider();
	}

	@Bean
	AnnotationLinkRelationProvider annotationRelProvider() {
		return new AnnotationLinkRelationProvider();
	}

	@Primary
	@Bean
	DelegatingLinkRelationProvider _relProvider(
			PluginRegistry<LinkRelationProvider, LookupContext> relProviderPluginRegistry) {
		return new DelegatingLinkRelationProvider(relProviderPluginRegistry);
	}

	@Bean
	PluginRegistryFactoryBean<LinkRelationProvider, LookupContext> relProviderPluginRegistry() {

		PluginRegistryFactoryBean<LinkRelationProvider, LookupContext> factory = new PluginRegistryFactoryBean<>();

		factory.setType(LinkRelationProvider.class);
		factory.setExclusions(new Class[] { DelegatingLinkRelationProvider.class });

		return factory;
	}

	// LinkDiscoverers

	@Bean
	LinkDiscoverers linkDiscoverers(PluginRegistry<LinkDiscoverer, MediaType> discoverers) {
		return new LinkDiscoverers(discoverers);
	}

	/**
	 * Creates a message source for the {@code rest-messages} resource bundle if the file exists or a
	 * {@link NoOpMessageSource} otherwise.
	 *
	 * @return will never be {@literal null}.
	 */
	@Nullable
	private static final MessageSource lookupMessageSource() {

		ClassPathResource resource = new ClassPathResource("rest-messages");

		if (!resource.exists()) {
			return null;
		}

		AbstractResourceBasedMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:rest-messages");

		return messageSource;
	}
}
