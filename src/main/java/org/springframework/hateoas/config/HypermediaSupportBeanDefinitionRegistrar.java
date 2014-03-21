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

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.*;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.*;
import static org.springframework.hateoas.MediaTypes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.support.PluginRegistryFactoryBean;
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
@SuppressWarnings("deprecation")
class HypermediaSupportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	private static final String DELEGATING_REL_PROVIDER_BEAN_NAME = "_relProvider";
	private static final String LINK_DISCOVERER_REGISTRY_BEAN_NAME = "_linkDiscovererRegistry";
	private static final String HAL_OBJECT_MAPPER_BEAN_NAME = "_halObjectMapper";

	private static final boolean JACKSON2_PRESENT = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper",
			null);
	private static final boolean JSONPATH_PRESENT = ClassUtils.isPresent("com.jayway.jsonpath.JsonPath", null);
	private static final boolean EVO_PRESENT = ClassUtils.isPresent("org.atteo.evo.inflector.English", null);

	private final ImportBeanDefinitionRegistrar linkBuilderBeanDefinitionRegistrar = new LinkBuilderBeanDefinitionRegistrar();

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

		linkBuilderBeanDefinitionRegistrar.registerBeanDefinitions(metadata, registry);

		Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableHypermediaSupport.class.getName());
		Collection<HypermediaType> types = Arrays.asList((HypermediaType[]) attributes.get("type"));

		for (HypermediaType type : types) {

			if (JSONPATH_PRESENT) {

				AbstractBeanDefinition linkDiscovererBeanDefinition = getLinkDiscovererBeanDefinition(type);
				registerBeanDefinition(
						new BeanDefinitionHolder(linkDiscovererBeanDefinition, BeanDefinitionReaderUtils.generateBeanName(
								linkDiscovererBeanDefinition, registry)), registry);
			}
		}

		if (types.contains(HypermediaType.HAL)) {

			if (JACKSON2_PRESENT) {

				BeanDefinitionBuilder halQueryMapperBuilder = rootBeanDefinition(ObjectMapper.class);
				registerSourcedBeanDefinition(halQueryMapperBuilder, metadata, registry, HAL_OBJECT_MAPPER_BEAN_NAME);

				BeanDefinitionBuilder builder = rootBeanDefinition(Jackson2ModuleRegisteringBeanPostProcessor.class);
				registerSourcedBeanDefinition(builder, metadata, registry);
			}
		}

		if (!types.isEmpty()) {

			BeanDefinitionBuilder linkDiscoverersRegistryBuilder = BeanDefinitionBuilder
					.rootBeanDefinition(PluginRegistryFactoryBean.class);
			linkDiscoverersRegistryBuilder.addPropertyValue("type", LinkDiscoverer.class);
			registerSourcedBeanDefinition(linkDiscoverersRegistryBuilder, metadata, registry,
					LINK_DISCOVERER_REGISTRY_BEAN_NAME);

			BeanDefinitionBuilder linkDiscoverersBuilder = BeanDefinitionBuilder.rootBeanDefinition(LinkDiscoverers.class);
			linkDiscoverersBuilder.addConstructorArgReference(LINK_DISCOVERER_REGISTRY_BEAN_NAME);
			registerSourcedBeanDefinition(linkDiscoverersBuilder, metadata, registry);
		}

		registerRelProviderPluginRegistryAndDelegate(registry);
	}

	/**
	 * Registers bean definitions for a {@link PluginRegistry} to capture {@link RelProvider} instances. Wraps the
	 * registry into a {@link DelegatingRelProvider} bean definition backed by the registry.
	 * 
	 * @param registry
	 */
	private static void registerRelProviderPluginRegistryAndDelegate(BeanDefinitionRegistry registry) {

		Class<?> defaultRelProviderType = EVO_PRESENT ? EvoInflectorRelProvider.class : DefaultRelProvider.class;
		RootBeanDefinition defaultRelProviderBeanDefinition = new RootBeanDefinition(defaultRelProviderType);
		registry.registerBeanDefinition("defaultRelProvider", defaultRelProviderBeanDefinition);

		RootBeanDefinition annotationRelProviderBeanDefinition = new RootBeanDefinition(AnnotationRelProvider.class);
		registry.registerBeanDefinition("annotationRelProvider", annotationRelProviderBeanDefinition);

		BeanDefinitionBuilder registryFactoryBeanBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(PluginRegistryFactoryBean.class);
		registryFactoryBeanBuilder.addPropertyValue("type", RelProvider.class);
		registryFactoryBeanBuilder.addPropertyValue("exclusions", DelegatingRelProvider.class);

		AbstractBeanDefinition registryBeanDefinition = registryFactoryBeanBuilder.getBeanDefinition();
		registry.registerBeanDefinition("relProviderPluginRegistry", registryBeanDefinition);

		BeanDefinitionBuilder delegateBuilder = BeanDefinitionBuilder.rootBeanDefinition(DelegatingRelProvider.class);
		delegateBuilder.addConstructorArgValue(registryBeanDefinition);

		AbstractBeanDefinition beanDefinition = delegateBuilder.getBeanDefinition();
		beanDefinition.setPrimary(true);
		registry.registerBeanDefinition(DELEGATING_REL_PROVIDER_BEAN_NAME, beanDefinition);
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

	/**
	 * {@link BeanPostProcessor} to register {@link Jackson2HalModule} with {@link ObjectMapper} instances registered in
	 * the {@link ApplicationContext}.
	 * 
	 * @author Oliver Gierke
	 */
	static class Jackson2ModuleRegisteringBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

		private CurieProvider curieProvider;
		private RelProvider relProvider;
		private ObjectMapper halObjectMapper;

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
		 */
		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

			this.curieProvider = getCurieProvider(beanFactory);
			this.relProvider = beanFactory.getBean(DELEGATING_REL_PROVIDER_BEAN_NAME, RelProvider.class);
			this.halObjectMapper = beanFactory.getBean(HAL_OBJECT_MAPPER_BEAN_NAME, ObjectMapper.class);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

			if (bean instanceof RequestMappingHandlerAdapter) {

				RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) bean;
				adapter.setMessageConverters(potentiallyRegisterModule(adapter.getMessageConverters()));
			}

			if (bean instanceof AnnotationMethodHandlerAdapter) {

				AnnotationMethodHandlerAdapter adapter = (AnnotationMethodHandlerAdapter) bean;
				List<HttpMessageConverter<?>> augmentedConverters = potentiallyRegisterModule(Arrays.asList(adapter
						.getMessageConverters()));
				adapter
						.setMessageConverters(augmentedConverters.toArray(new HttpMessageConverter<?>[augmentedConverters.size()]));
			}

			return bean;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
		 */
		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			return bean;
		}

		private List<HttpMessageConverter<?>> potentiallyRegisterModule(List<HttpMessageConverter<?>> converters) {

			for (HttpMessageConverter<?> converter : converters) {
				if (converter instanceof MappingJackson2HttpMessageConverter) {
					MappingJackson2HttpMessageConverter halConverterCandidate = (MappingJackson2HttpMessageConverter) converter;
					ObjectMapper objectMapper = halConverterCandidate.getObjectMapper();
					if (Jackson2HalModule.isAlreadyRegisteredIn(objectMapper)) {
						return converters;
					}
				}
			}

			halObjectMapper.registerModule(new Jackson2HalModule());
			halObjectMapper.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(relProvider, curieProvider));

			MappingJackson2HttpMessageConverter halConverter = new MappingJackson2HttpMessageConverter();
			halConverter.setSupportedMediaTypes(Arrays.asList(HAL_JSON));
			halConverter.setObjectMapper(halObjectMapper);

			List<HttpMessageConverter<?>> result = new ArrayList<HttpMessageConverter<?>>(converters.size());
			result.add(halConverter);
			result.addAll(converters);
			return result;
		}

		private static CurieProvider getCurieProvider(BeanFactory factory) {

			try {
				return factory.getBean(CurieProvider.class);
			} catch (NoSuchBeanDefinitionException e) {
				return null;
			}
		}
	}
}
