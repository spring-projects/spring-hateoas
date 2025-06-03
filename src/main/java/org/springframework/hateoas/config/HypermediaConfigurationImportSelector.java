/*
 * Copyright 2019-2024 the original author or authors.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.MediaType;
import org.springframework.util.ClassUtils;

/**
 * {@link ImportSelector} that looks up configuration classes from all {@link MediaTypeConfigurationProvider}
 * implementations listed in {@code META-INF/spring.factories}.
 *
 * @author Oliver Drotbohm
 * @author Greg Turnquist
 */
class HypermediaConfigurationImportSelector implements ImportSelector, ResourceLoaderAware, BeanFactoryAware {

	public static final String SPRING_TEST = "org.springframework.test.web.reactive.server.WebTestClient";

	private @Nullable ResourceLoader resourceLoader;
	private @Nullable ConfigurableBeanFactory beanFactory;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ResourceLoaderAware#setResourceLoader(org.springframework.core.io.ResourceLoader)
	 */
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

	/*
	* (non-Javadoc)
	* @see org.springframework.context.annotation.ImportSelector#selectImports(org.springframework.core.type.AnnotationMetadata)
	*/
	@Override
	public String[] selectImports(AnnotationMetadata metadata) {

		if (beanFactory == null || resourceLoader == null) {
			return new String[0];
		}

		Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableHypermediaSupport.class.getName());

		List<MediaType> types = attributes == null //
				? Collections.emptyList() //
				: Arrays.stream((HypermediaType[]) attributes.get("type")) //
						.flatMap(it -> it.getMediaTypes().stream()) //
						.collect(Collectors.toList());

		if (!beanFactory.containsBean("hateoasMediaTypeConfigurer")) {
			beanFactory.registerSingleton("hateoasMediaTypeConfigurer", new HypermediaMappingInformationComparator(types));
		}

		List<MediaTypeConfigurationProvider> configurationProviders = SpringFactoriesLoader.loadFactories(
				MediaTypeConfigurationProvider.class, HypermediaConfigurationImportSelector.class.getClassLoader());

		// Filter the ones supporting the given media types
		Stream<String> imports = configurationProviders.stream() //
				.filter(it -> it.supportsAny(types)) //
				.map(MediaTypeConfigurationProvider::getConfiguration) //
				.map(Class::getName);

		// Conditionally apply other configurations
		if (ClassUtils.isPresent(SPRING_TEST, resourceLoader.getClassLoader())) {
			imports = Stream.concat(imports, Stream.of(WebTestHateoasConfiguration.class.getName()));
		}

		return imports.toArray(String[]::new);
	}

	public String[] selectImports(List<MediaType> mediaType) {
		return new String[] {};
	}
}
