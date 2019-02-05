/*
 * Copyright 2018-2019 the original author or authors.
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
package org.springframework.hateoas.config.mvc;

import static org.springframework.hateoas.config.HypermediaObjectMapperCreator.*;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.Hypermedia;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.hateoas.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
@Configuration
@RequiredArgsConstructor
public class HypermediaWebMvcConfigurer implements WebMvcConfigurer, BeanFactoryAware {

	private static final String MESSAGE_SOURCE_BEAN_NAME = "linkRelationMessageSource";

	private final ObjectMapper mapper;
	private final DelegatingRelProvider relProvider;
	private final CurieProvider curieProvider;
	private final HalConfiguration halConfiguration;
	private final HalFormsConfiguration halFormsConfiguration;
	private final Collection<Hypermedia> hypermediaTypes;

	private BeanFactory beanFactory;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#extendMessageConverters(java.util.List)
	 */
	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

		if (converters.stream().filter(MappingJackson2HttpMessageConverter.class::isInstance)
				.filter(AbstractJackson2HttpMessageConverter.class::isInstance)
				.map(AbstractJackson2HttpMessageConverter.class::cast)
				.map(AbstractJackson2HttpMessageConverter::getObjectMapper)
				.anyMatch(Jackson2HalModule::isAlreadyRegisteredIn)) {

			return;
		}

		MessageSourceAccessor linkRelationMessageSource = this.beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME,
				MessageSourceAccessor.class);

		this.hypermediaTypes.forEach(hypermedia -> {

			ObjectMapper objectMapper;

			if (hypermedia == HypermediaType.HAL) {

				objectMapper = createHalObjectMapper(this.mapper, this.curieProvider, this.relProvider,
					linkRelationMessageSource, this.halConfiguration);

			} else if (hypermedia == HypermediaType.HAL_FORMS) {

				objectMapper = createHalFormsObjectMapper(this.mapper, this.curieProvider, this.relProvider,
					linkRelationMessageSource, this.halFormsConfiguration);

			} else if (hypermedia == HypermediaType.COLLECTION_JSON) {

				objectMapper = createCollectionJsonObjectMapper(this.mapper);

			} else if (hypermedia == HypermediaType.UBER) {

				objectMapper = createUberObjectMapper(this.mapper);

			} else {

				objectMapper = hypermedia.createObjectMapper(this.mapper);
			}

			converters.add(0, new TypeConstrainedMappingJackson2HttpMessageConverter(
				ResourceSupport.class, hypermedia.getMediaTypes(), objectMapper));

		});
	}
}
