/*
 * Copyright 2013 the original author or authors.
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

import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.*;

import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DefaultLinkDiscoverer;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.hateoas.hal.Jackson1HalModule;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link ImportBeanDefinitionRegistrar} implementation to activate hypermedia support based on the configured
 * hypermedia type. Activates {@link EntityLinks} support as well (essentially as if {@link EnableEntityLinks} was
 * activated as well).
 * 
 * @author Oliver Gierke
 */
class HypermediaSupportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	private static final String LINK_DISCOVERER_BEAN_NAME = "_linkDiscoverer";

	private static final boolean JACKSON1_PRESENT = ClassUtils.isPresent("org.codehaus.jackson.map.ObjectMapper", null);
	private static final boolean JACKSON2_PRESENT = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper",
			null);
	private static final boolean JSONPATH_PRESENT = ClassUtils.isPresent("com.jayway.jsonpath.JsonPath", null);

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

		new LinkBuilderBeanDefinitionRegistrar().registerBeanDefinitions(importingClassMetadata, registry);

		Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableHypermediaSupport.class
				.getName());
		HypermediaType type = (HypermediaType) attributes.get("type");

		if (JSONPATH_PRESENT) {
			registerBeanDefinition(
					new BeanDefinitionHolder(getLinkDiscovererBeanDefinition(type), LINK_DISCOVERER_BEAN_NAME), registry);
		}

		if (type == HypermediaType.HAL) {

			if (JACKSON2_PRESENT) {
				registerWithGeneratedName(new RootBeanDefinition(Jackson2ModuleRegisteringBeanPostProcessor.class), registry);
			}

			if (JACKSON1_PRESENT) {
				registerWithGeneratedName(new RootBeanDefinition(Jackson1ModuleRegisteringBeanPostProcessor.class), registry);
			}
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
			case DEFAULT:
			default:
				definition = new RootBeanDefinition(DefaultLinkDiscoverer.class);
		}

		definition.setSource(this);
		return definition;
	}

	/**
	 * {@link BeanPostProcessor} to register {@link Jackson2HalModule} with {@link ObjectMapper} instances registered in
	 * the {@link ApplicationContext}.
	 * 
	 * @author Oliver Gierke
	 */
	private static class Jackson2ModuleRegisteringBeanPostProcessor implements BeanPostProcessor {

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			return bean;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

			if (bean instanceof RequestMappingHandlerAdapter) {
				registerModule(((RequestMappingHandlerAdapter) bean).getMessageConverters());
			}

			if (bean instanceof AnnotationMethodHandlerAdapter) {
				registerModule(((AnnotationMethodHandlerAdapter) bean).getMessageConverters());
			}

			if (bean instanceof ObjectMapper) {
				registerModule(bean);
			}

			return bean;
		}

		private void registerModule(List<HttpMessageConverter<?>> converters) {

			for (HttpMessageConverter<?> converter : converters) {
				if (converter instanceof MappingJackson2HttpMessageConverter) {
					registerModule(((MappingJackson2HttpMessageConverter) converter).getObjectMapper());
				}
			}
		}

		private void registerModule(Object objectMapper) {
			((ObjectMapper) objectMapper).registerModule(new Jackson2HalModule(null));
		}
	}

	/**
	 * {@link BeanPostProcessor} to register the {@link Jackson1HalModule} with
	 * {@link org.codehaus.jackson.map.ObjectMapper} beans registered in the {@link ApplicationContext}.
	 * 
	 * @author Oliver Gierke
	 */
	private static class Jackson1ModuleRegisteringBeanPostProcessor implements BeanPostProcessor {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			return bean;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

			if (bean instanceof AnnotationMethodHandlerAdapter) {
				registerModule(((AnnotationMethodHandlerAdapter) bean).getMessageConverters());
			}

			if (bean instanceof RequestMappingHandlerAdapter) {
				registerModule(((RequestMappingHandlerAdapter) bean).getMessageConverters());
			}

			if (bean instanceof org.codehaus.jackson.map.ObjectMapper) {
				registerModule(bean);
			}

			return bean;
		}

		private void registerModule(List<HttpMessageConverter<?>> converters) {

			for (HttpMessageConverter<?> converter : converters) {
				if (converter instanceof MappingJacksonHttpMessageConverter) {
					registerModule(((MappingJacksonHttpMessageConverter) converter).getObjectMapper());
				}
			}
		}

		private void registerModule(Object mapper) {
			((org.codehaus.jackson.map.ObjectMapper) mapper).registerModule(new Jackson1HalModule(null));
		}
	}
}
