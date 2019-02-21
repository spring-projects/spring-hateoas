/*
 * Copyright 2013-2019 the original author or authors.
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

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.*;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link ImportBeanDefinitionRegistrar} implementation to activate hypermedia support based on the configured
 * hypermedia type. Activates {@link EntityLinks} support as well (essentially as if {@link EnableEntityLinks} was
 * activated as well).
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class HypermediaSupportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	private static final boolean JSONPATH_PRESENT = ClassUtils.isPresent("com.jayway.jsonpath.JsonPath", null);

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

		Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableHypermediaSupport.class.getName());
		Collection<HypermediaType> types = Arrays.asList((HypermediaType[]) attributes.get("type"));

		/*
		 * Register all the {@link HypermediaType}s as individual beans, so they can be gathered as needed into a
		 * collection using the application context.
		 */
		for (HypermediaType type : types) {

			type.configurer().ifPresent(clazz -> {

				Assert.isTrue(clazz.isAnnotationPresent(Configuration.class),
						clazz + " must have Spring's @Configuration annotation!");

				BeanDefinitionBuilder hypermediaTypeBeanDefinition = genericBeanDefinition(clazz);
				registerSourcedBeanDefinition(hypermediaTypeBeanDefinition, metadata, registry);
			});
		}

	}
	
	private static String registerSourcedBeanDefinition(BeanDefinitionBuilder builder, AnnotationMetadata metadata,
			BeanDefinitionRegistry registry) {

		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		String generateBeanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry);
		return registerSourcedBeanDefinition(builder, metadata, registry, generateBeanName);
	}

	private static String registerSourcedBeanDefinition(BeanDefinitionBuilder builder, AnnotationMetadata metadata,
			BeanDefinitionRegistry registry, String name) {

		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		beanDefinition.setSource(metadata);

		BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, name);
		registerBeanDefinition(holder, registry);
		return name;
	}
}
