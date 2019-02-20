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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.mvc.WebMvcHateoasConfiguration;
import org.springframework.hateoas.config.reactive.WebFluxHateoasConfiguration;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.hateoas.hal.forms.HalFormsLinkDiscoverer;
import org.springframework.hateoas.support.WebStack;
import org.springframework.hateoas.uber.UberLinkDiscoverer;
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
			
			BeanDefinitionBuilder hypermediaTypeBeanDefinition = genericBeanDefinition(HypermediaType.class, () -> type);
			registerSourcedBeanDefinition(hypermediaTypeBeanDefinition, metadata, registry);
		}

		/*
		 * Only register JSONPath-based {@link LinkDiscoverer}s based on the registered {@link HypermediaType}s.
		 */
		if (JSONPATH_PRESENT) {

			for (HypermediaType type : types) {

				AbstractBeanDefinition linkDiscovererBeanDefinition = getLinkDiscovererBeanDefinition(type);
				registerBeanDefinition(new BeanDefinitionHolder(linkDiscovererBeanDefinition,
						BeanDefinitionReaderUtils.generateBeanName(linkDiscovererBeanDefinition, registry)), registry);
			}
		}

		/*
		 * Register a Spring MVC-specific HATEOAS configuration.
		 */
		if (WebStack.WEBMVC.isAvailable()) {
			
			BeanDefinitionBuilder webMvcHateosConfiguration = rootBeanDefinition(WebMvcHateoasConfiguration.class);
			registerSourcedBeanDefinition(webMvcHateosConfiguration, metadata, registry);
		}

		/*
		 * Register a Spring WebFlux-specific HATEOAS configuration.
		 */
		if (WebStack.WEBFLUX.isAvailable()) {

			BeanDefinitionBuilder webFluxHateoasConfiguration = rootBeanDefinition(WebFluxHateoasConfiguration.class);
			registerSourcedBeanDefinition(webFluxHateoasConfiguration, metadata, registry);
		}

	}

	/**
	 * Returns a {@link LinkDiscoverer} {@link BeanDefinition} suitable for the given {@link HypermediaType}.
	 * 
	 * @param type
	 * @return
	 */
	private AbstractBeanDefinition getLinkDiscovererBeanDefinition(HypermediaType type) {

		AbstractBeanDefinition definition;

		switch (type) {
			case HAL:
				definition = new RootBeanDefinition(HalLinkDiscoverer.class);
				break;
			case HAL_FORMS:
				definition = new RootBeanDefinition(HalFormsLinkDiscoverer.class);
				break;
			case COLLECTION_JSON:
				definition = new RootBeanDefinition(CollectionJsonLinkDiscoverer.class);
				break;
			case UBER:
				definition = new RootBeanDefinition(UberLinkDiscoverer.class);
				break;
			default:
				throw new IllegalStateException(String.format("Unsupported hypermedia type %s!", type));
		}

		definition.setSource(this);
		return definition;
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
