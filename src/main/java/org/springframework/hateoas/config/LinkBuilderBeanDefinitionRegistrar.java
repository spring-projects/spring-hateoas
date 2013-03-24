/*
 * Copyright 2012-2013 the original author or authors.
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

import java.lang.annotation.Annotation;

import javax.ws.rs.Path;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkBuilderFactory;
import org.springframework.hateoas.core.ControllerEntityLinksFactoryBean;
import org.springframework.hateoas.core.DelegatingEntityLinks;
import org.springframework.hateoas.jaxrs.JaxRsLinkBuilderFactory;
import org.springframework.hateoas.mvc.ControllerLinkBuilderFactory;
import org.springframework.plugin.core.support.PluginRegistryFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;

/**
 * {@link ImportBeanDefinitionRegistrar} to register a {@link DelegatingEntityLinks} instance as well as a
 * {@link ControllerEntityLinksFactoryBean} for Spring MVC controllers and JAX-RS resources if present.
 * 
 * @author Oliver Gierke
 */
class LinkBuilderBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	private static final boolean IS_JAX_RS_PRESENT = ClassUtils.isPresent("javax.ws.rs.Path",
			ClassUtils.getDefaultClassLoader());

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

		BeanDefinitionBuilder registryFactoryBeanBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(PluginRegistryFactoryBean.class);
		registryFactoryBeanBuilder.addPropertyValue("type", EntityLinks.class);
		registryFactoryBeanBuilder.addPropertyValue("exclusions", DelegatingEntityLinks.class);

		AbstractBeanDefinition registryBeanDefinition = registryFactoryBeanBuilder.getBeanDefinition();
		registry.registerBeanDefinition("entityLinksPluginRegistry", registryBeanDefinition);

		BeanDefinitionBuilder delegateBuilder = BeanDefinitionBuilder.rootBeanDefinition(DelegatingEntityLinks.class);
		delegateBuilder.addConstructorArgValue(registryBeanDefinition);

		BeanDefinitionBuilder builder = getEntityControllerLinksFor(Controller.class, ControllerLinkBuilderFactory.class);
		registry.registerBeanDefinition("controllerEntityLinks", builder.getBeanDefinition());
		delegateBuilder.addDependsOn("controllerEntityLinks");

		if (IS_JAX_RS_PRESENT) {
			JaxRsEntityControllerBuilderDefinitionBuilder definitionBuilder = new JaxRsEntityControllerBuilderDefinitionBuilder();
			registry.registerBeanDefinition("jaxRsEntityLinks", definitionBuilder.getBeanDefinition());
			delegateBuilder.addDependsOn("jaxRsEntityLinks");
		}

		AbstractBeanDefinition beanDefinition = delegateBuilder.getBeanDefinition();
		beanDefinition.setPrimary(true);
		registry.registerBeanDefinition("delegatingEntityLinks", beanDefinition);
	}

	private static BeanDefinitionBuilder getEntityControllerLinksFor(Class<? extends Annotation> type,
			Class<? extends LinkBuilderFactory<?>> linkBuilderFactoryType) {

		RootBeanDefinition definition = new RootBeanDefinition(linkBuilderFactoryType);
		definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ControllerEntityLinksFactoryBean.class);
		builder.addPropertyValue("annotation", type);
		builder.addPropertyValue("linkBuilderFactory", definition);

		return builder;
	}

	static class JaxRsEntityControllerBuilderDefinitionBuilder {

		public BeanDefinition getBeanDefinition() {
			BeanDefinitionBuilder builder = getEntityControllerLinksFor(Path.class, JaxRsLinkBuilderFactory.class);
			return builder.getBeanDefinition();
		}
	}
}
