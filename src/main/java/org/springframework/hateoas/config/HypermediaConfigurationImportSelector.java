/*
 * Copyright 2019-2020 the original author or authors.
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

import org.springframework.context.annotation.ImportSelector;
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
 */
class HypermediaConfigurationImportSelector implements ImportSelector {

	public static final String SPRING_TEST = "org.springframework.test.web.reactive.server.WebTestClientConfigurer";

	/*
	* (non-Javadoc)
	* @see org.springframework.context.annotation.ImportSelector#selectImports(org.springframework.core.type.AnnotationMetadata)
	*/
	@Override
	public String[] selectImports(AnnotationMetadata metadata) {

		Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableHypermediaSupport.class.getName());

		List<MediaType> types = attributes == null //
				? Collections.emptyList() //
				: Arrays.stream((HypermediaType[]) attributes.get("type")) //
						.flatMap(it -> it.getMediaTypes().stream()) //
						.collect(Collectors.toList());

		List<MediaTypeConfigurationProvider> configurationProviders = SpringFactoriesLoader.loadFactories(
				MediaTypeConfigurationProvider.class, HypermediaConfigurationImportSelector.class.getClassLoader());

		// Filter the ones supporting the given media types

		List<String> hypermediaImportClasses = configurationProviders.stream() //
				.filter(it -> it.supportsAny(types)) //
				.map(MediaTypeConfigurationProvider::getConfiguration) //
				.map(Class::getName).collect(Collectors.toList());

		// Conditionally apply other configurations

		if (ClassUtils.isPresent(SPRING_TEST, getClass().getClassLoader())) {
			hypermediaImportClasses.add("org.springframework.hateoas.config.WebTestHateoasConfiguration");
		}

		return hypermediaImportClasses.toArray(new String[0]);
	}

	public String[] selectImports(List<MediaType> mediaType) {
		return new String[] {};
	}
}
