/*
 * Copyright 2018-2024 the original author or authors.
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

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.core.DelegatingEntityLinks;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.support.PluginRegistryFactoryBean;

/**
 * Spring configuration to register a {@link PluginRegistry} for {@link EntityLinks}.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
@Configuration(proxyBeanMethods = false)
class EntityLinksConfiguration {

	@Bean
	OrderAwarePluginRegistry<EntityLinks, Class<?>> entityLinksPluginRegistry(ListableBeanFactory beanFactory) {

		PluginRegistryFactoryBean<EntityLinks, Class<?>> registry = new PluginRegistryFactoryBean<>();
		registry.setBeanFactory(beanFactory);
		registry.setType(EntityLinks.class);
		registry.setExclusions(new Class[] { DelegatingEntityLinks.class });

		return registry.getObject();
	}

	@Primary
	@Bean
	DelegatingEntityLinks delegatingEntityLinks(PluginRegistry<EntityLinks, Class<?>> entityLinksPluginRegistry) {
		return new DelegatingEntityLinks(entityLinksPluginRegistry);
	}
}
