/*
 * Copyright 2018 the original author or authors.
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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.core.ControllerEntityLinksFactoryBean;
import org.springframework.hateoas.core.DelegatingEntityLinks;
import org.springframework.hateoas.mvc.ControllerLinkBuilderFactory;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.support.PluginRegistryFactoryBean;
import org.springframework.stereotype.Controller;

/**
 * Spring configuration to register a {@link PluginRegistry} for {@link EntityLinks}.
 * 
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
@Configuration
class EntityLinksConfiguration {

	@Bean
	PluginRegistryFactoryBean<EntityLinks, Class<?>> entityLinksPluginRegistry() {

		PluginRegistryFactoryBean<EntityLinks, Class<?>> registry = new PluginRegistryFactoryBean<EntityLinks, Class<?>>();
		registry.setType(EntityLinks.class);
		registry.setExclusions(new Class[] { DelegatingEntityLinks.class });

		return registry;
	}

	@Primary
	@Bean
	@DependsOn("controllerEntityLinks")
	DelegatingEntityLinks delegatingEntityLinks(PluginRegistry<EntityLinks, Class<?>> entityLinksPluginRegistry) {
		return new DelegatingEntityLinks(entityLinksPluginRegistry);
	}

	@Bean
	ControllerEntityLinksFactoryBean controllerEntityLinks(ControllerLinkBuilderFactory controllerLinkBuilderFactory) {

		ControllerEntityLinksFactoryBean factory = new ControllerEntityLinksFactoryBean();
		factory.setAnnotation(Controller.class);
		factory.setLinkBuilderFactory(controllerLinkBuilderFactory);

		return factory;
	}

	@Bean
	ControllerLinkBuilderFactory controllerLinkBuilderFactoryBean() {
		return new ControllerLinkBuilderFactory();
	}
}
