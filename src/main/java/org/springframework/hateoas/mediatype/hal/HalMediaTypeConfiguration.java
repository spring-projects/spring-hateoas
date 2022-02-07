/*
 * Copyright 2019-2022 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring configuration to set up HAL support.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@Configuration(proxyBeanMethods = false)
public class HalMediaTypeConfiguration implements HypermediaMappingInformation {

	private final LinkRelationProvider relProvider;
	private final ObjectProvider<CurieProvider> curieProvider;
	private final ObjectProvider<HalConfiguration> halConfiguration;
	private final @Qualifier("messageResolver") MessageResolver resolver;
	private final AutowireCapableBeanFactory beanFactory;

	private HalConfiguration resolvedConfiguration;

	public HalMediaTypeConfiguration(LinkRelationProvider relProvider, ObjectProvider<CurieProvider> curieProvider,
			ObjectProvider<HalConfiguration> halConfiguration, MessageResolver resolver,
			AutowireCapableBeanFactory beanFactory) {

		this.relProvider = relProvider;
		this.curieProvider = curieProvider;
		this.halConfiguration = halConfiguration;
		this.resolver = resolver;
		this.beanFactory = beanFactory;
	}

	@Bean
	LinkDiscoverer halLinkDisocoverer() {
		return new HalLinkDiscoverer();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#getMediaTypes()
	 */
	@Override
	public List<MediaType> getMediaTypes() {
		return getResolvedConfiguration().getMediaTypes();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#configureObjectMapper(com.fasterxml.jackson.databind.ObjectMapper)
	 */
	@Override
	public ObjectMapper configureObjectMapper(ObjectMapper mapper) {

		HalConfiguration halConfiguration = getResolvedConfiguration();

		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.registerModule(new Jackson2HalModule());
		mapper.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(relProvider,
				curieProvider.getIfAvailable(() -> CurieProvider.NONE), resolver, halConfiguration, beanFactory));

		halConfiguration.customize(mapper);

		return mapper;
	}

	/**
	 * Lookup and cache the {@link HalConfiguration} instance to be used.
	 *
	 * @return will never be {@literal null}.
	 */
	private HalConfiguration getResolvedConfiguration() {

		if (resolvedConfiguration == null) {
			this.resolvedConfiguration = halConfiguration.getIfAvailable(HalConfiguration::new);
		}

		return resolvedConfiguration;
	}
}
