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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
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
@EnablePluginRegistries({ LinkDiscoverer.class })
public class HateoasConfiguration {

	static String I18N_BASE_NAME = "rest-messages";
	static String I18N_DEFAULTS_BASE_NAME = "rest-default-messages";

	private @Autowired ApplicationContext context;

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
	private final AbstractMessageSource lookupMessageSource() {

		List<Resource> candidates = loadResourceBundleResources(I18N_DEFAULTS_BASE_NAME, false);

		if (candidates.isEmpty() && loadResourceBundleResources(I18N_BASE_NAME, true).isEmpty()) {
			return null;
		}

		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setResourceLoader(context);
		messageSource.setBasename("classpath:".concat(I18N_BASE_NAME));
		messageSource.setDefaultEncoding(StandardCharsets.UTF_8.toString());

		if (!candidates.isEmpty()) {
			messageSource.setCommonMessages(loadProperties(candidates));
		}

		return messageSource;
	}

	@Nullable
	private final Properties loadProperties(List<Resource> sources) {

		PropertiesFactoryBean factory = new PropertiesFactoryBean();
		factory.setLocations(sources.toArray(new Resource[sources.size()]));

		try {

			factory.afterPropertiesSet();
			return factory.getObject();

		} catch (IOException o_O) {
			throw new IllegalStateException("Could not load default properties from resources!", o_O);
		}
	}

	private final List<Resource> loadResourceBundleResources(String baseName, boolean withWildcard) {

		try {
			return Arrays //
					.stream(context.getResources(String.format("classpath:%s%s.properties", baseName, withWildcard ? "*" : ""))) //
					.filter(Resource::exists) //
					.collect(Collectors.toList());

		} catch (IOException e) {
			return Collections.emptyList();
		}
	}
}
