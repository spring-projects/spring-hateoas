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
package org.springframework.hateoas.mediatype.hal.forms;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper.Builder;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.hateoas.mediatype.MediaTypeConfigurationCustomizer;
import org.springframework.hateoas.mediatype.MediaTypeConfigurationFactory;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.HalJacksonModule;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.http.MediaType;

/**
 * Spring configuration for HAL Forms support.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@Configuration(proxyBeanMethods = false)
class HalFormsMediaTypeConfiguration implements HypermediaMappingInformation {

	private final DelegatingLinkRelationProvider relProvider;
	private final ObjectProvider<CurieProvider> curieProvider;
	private final MediaTypeConfigurationFactory<HalFormsConfiguration, ? extends MediaTypeConfigurationCustomizer<HalFormsConfiguration>> configurationFactory;
	private final MessageResolver resolver;
	private final AbstractAutowireCapableBeanFactory beanFactory;

	public HalFormsMediaTypeConfiguration(DelegatingLinkRelationProvider relProvider,
			ObjectProvider<CurieProvider> curieProvider,
			ObjectProvider<HalConfiguration> halConfiguration,
			ObjectProvider<MediaTypeConfigurationCustomizer<HalConfiguration>> halCustomizers,
			ObjectProvider<HalFormsConfiguration> halFormsConfiguration,
			ObjectProvider<MediaTypeConfigurationCustomizer<HalFormsConfiguration>> halFormsCustomizers,
			MessageResolver resolver, AbstractAutowireCapableBeanFactory beanFactory) {

		this.relProvider = relProvider;
		this.curieProvider = curieProvider;

		Supplier<HalFormsConfiguration> defaultConfig = () -> {

			MediaTypeConfigurationFactory<HalConfiguration, ?> customizedHalConfiguration = new MediaTypeConfigurationFactory<>(
					() -> halConfiguration.getIfAvailable(HalConfiguration::new), halCustomizers);

			return new HalFormsConfiguration(
					customizedHalConfiguration.getConfiguration());
		};

		this.configurationFactory = new MediaTypeConfigurationFactory<>(
				() -> halFormsConfiguration.getIfAvailable(defaultConfig), halFormsCustomizers);
		this.resolver = resolver;
		this.beanFactory = beanFactory;
	}

	@Bean
	LinkDiscoverer halFormsLinkDiscoverer() {
		return new HalFormsLinkDiscoverer();
	}

	@Bean
	HalFormsTemplateBuilder halFormsTemplateBuilder() {

		HalFormsConfiguration configuration = configurationFactory.getConfiguration();
		return new HalFormsTemplateBuilder(configuration, resolver);
	}

	@Bean
	HalFormsTemplatePropertyWriter halFormsTemplatePropertyWriter(HalFormsTemplateBuilder builder) {
		return new HalFormsTemplatePropertyWriter(builder);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#configureJsonMapper(tools.jackson.databind.json.JsonMapper.Builder)
	 */
	@Override
	public Builder configureJsonMapper(Builder builder) {

		var halFormsConfig = configurationFactory.getConfiguration();
		var provider = curieProvider.getIfAvailable(() -> CurieProvider.NONE);

		builder = builder
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.addModule(new HalFormsJacksonModule())
				.handlerInstantiator(new HalJacksonModule.HalHandlerInstantiator(relProvider, provider,
						resolver, halFormsConfig.getHalConfiguration(), beanFactory));

		return halFormsConfig.customize(builder);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#getMediaTypes()
	 */
	@Override
	public List<MediaType> getMediaTypes() {
		return configurationFactory.getConfiguration().getMediaTypes();
	}

	/**
	 * For testing purposes.
	 *
	 * @return
	 */
	HalFormsConfiguration getResolvedConfiguration() {
		return configurationFactory.getConfiguration();
	}
}
