/*
 * Copyright 2018 the original author or authors.
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

import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link BeanPostProcessor} to register {@link Jackson2HalModule} with {@link ObjectMapper} instances registered in the
 * {@link ApplicationContext}.
 *
 * @author Oliver Gierke
 */
@RequiredArgsConstructor
class ConverterRegisteringBeanPostProcessor implements BeanPostProcessor {

	private final ObjectFactory<ConverterRegisteringWebMvcConfigurer> configurer;

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		if (bean instanceof RestTemplate) {

			ConverterRegisteringWebMvcConfigurer object = configurer.getObject();
			object.extendMessageConverters(((RestTemplate) bean).getMessageConverters());
		}

		return bean;
	}
}
