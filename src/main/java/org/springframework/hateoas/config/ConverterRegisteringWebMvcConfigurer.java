/*
 * Copyright 2018 the original author or authors.
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

import static org.springframework.hateoas.MediaTypes.*;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Oliver Gierke
 */
@Configuration
@RequiredArgsConstructor
public class ConverterRegisteringWebMvcConfigurer extends WebMvcConfigurerAdapter implements BeanFactoryAware {

	private static final String MESSAGE_SOURCE_BEAN_NAME = "linkRelationMessageSource";

	private final ObjectProvider<ObjectMapper> mapper;
	private final ObjectProvider<DelegatingRelProvider> relProvider;
	private final ObjectProvider<CurieProvider> curieProvider;
	private final ObjectProvider<HalConfiguration> halConfiguration;

	private BeanFactory beanFactory;
	private Collection<HypermediaType> hypermediaTypes;

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * @param hyperMediaTypes the hyperMediaTypes to set
	 */
	public void setHypermediaTypes(Collection<HypermediaType> hyperMediaTypes) {
		this.hypermediaTypes = hyperMediaTypes;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#extendMessageConverters(java.util.List)
	 */
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

		for (HttpMessageConverter<?> converter : converters) {
			if (converter instanceof MappingJackson2HttpMessageConverter) {
				MappingJackson2HttpMessageConverter halConverterCandidate = (MappingJackson2HttpMessageConverter) converter;
				ObjectMapper objectMapper = halConverterCandidate.getObjectMapper();
				if (Jackson2HalModule.isAlreadyRegisteredIn(objectMapper)) {
					return;
				}
			}
		}

		ObjectMapper objectMapper = mapper.getIfAvailable();
		objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;

		CurieProvider curieProvider = this.curieProvider.getIfAvailable();
		RelProvider relProvider = this.relProvider.getObject();
		MessageSourceAccessor linkRelationMessageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME,
				MessageSourceAccessor.class);

		if (hypermediaTypes.contains(HypermediaType.HAL)) {
			converters.add(0, createHalConverter(objectMapper, curieProvider, relProvider, linkRelationMessageSource));
		}
	}

	/**
	 * @param objectMapper
	 * @param curieProvider
	 * @param relProvider
	 * @param linkRelationMessageSource
	 * @return
	 */
	private MappingJackson2HttpMessageConverter createHalConverter(ObjectMapper objectMapper, CurieProvider curieProvider,
			RelProvider relProvider, MessageSourceAccessor linkRelationMessageSource) {

		HalConfiguration halConfiguration = this.halConfiguration.getIfAvailable();
		halConfiguration = halConfiguration == null ? new HalConfiguration() : halConfiguration;

		HalHandlerInstantiator instantiator = new Jackson2HalModule.HalHandlerInstantiator(relProvider, curieProvider,
				linkRelationMessageSource, halConfiguration);

		ObjectMapper mapper = objectMapper.copy();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.registerModule(new Jackson2HalModule());
		mapper.setHandlerInstantiator(instantiator);

		MappingJackson2HttpMessageConverter converter = new TypeConstrainedMappingJackson2HttpMessageConverter(
				ResourceSupport.class);
		converter.setSupportedMediaTypes(Arrays.asList(HAL_JSON, HAL_JSON_UTF8));
		converter.setObjectMapper(mapper);

		return converter;
	}
}
