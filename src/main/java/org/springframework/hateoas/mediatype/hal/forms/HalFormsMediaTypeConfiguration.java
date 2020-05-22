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
package org.springframework.hateoas.mediatype.hal.forms;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring configuration for HAL Forms support.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@Configuration
class HalFormsMediaTypeConfiguration implements HypermediaMappingInformation {

	private final DelegatingLinkRelationProvider relProvider;
	private final ObjectProvider<CurieProvider> curieProvider;
	private final ObjectProvider<HalFormsConfiguration> halFormsConfiguration;
	private final ObjectProvider<HalConfiguration> halConfiguration;
	private final MessageResolver resolver;
	private final AbstractAutowireCapableBeanFactory beanFactory;

	public HalFormsMediaTypeConfiguration(DelegatingLinkRelationProvider relProvider,
			ObjectProvider<CurieProvider> curieProvider, ObjectProvider<HalFormsConfiguration> halFormsConfiguration,
			ObjectProvider<HalConfiguration> halConfiguration, MessageResolver resolver,
			AbstractAutowireCapableBeanFactory beanFactory) {

		this.relProvider = relProvider;
		this.curieProvider = curieProvider;
		this.halFormsConfiguration = halFormsConfiguration;
		this.halConfiguration = halConfiguration;
		this.resolver = resolver;
		this.beanFactory = beanFactory;
	}

	@Bean
	LinkDiscoverer halFormsLinkDiscoverer() {
		return new HalFormsLinkDiscoverer();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#configureObjectMapper(com.fasterxml.jackson.databind.ObjectMapper)
	 */
	@Override
	public ObjectMapper configureObjectMapper(ObjectMapper mapper) {

		HalFormsConfiguration configuration = halFormsConfiguration
				.getIfAvailable(() -> new HalFormsConfiguration(halConfiguration.getIfAvailable(HalConfiguration::new)));

		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new Jackson2HalFormsModule.HalFormsHandlerInstantiator(relProvider,
				curieProvider.getIfAvailable(() -> CurieProvider.NONE), resolver, configuration, beanFactory));

		return mapper;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#getMediaTypes()
	 */
	@Override
	public List<MediaType> getMediaTypes() {
		return HypermediaType.HAL_FORMS.getMediaTypes();
	}
}
