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

import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.client.RestTemplate;

/**
 * {@link BeanPostProcessor} to register hypermedia support with {@link RestTemplate} instances found in the
 * application context.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
@RequiredArgsConstructor
public class HypermediaRestTemplateBeanPostProcessor implements BeanPostProcessor {

	private final HypermediaWebMvcConfigurer configurer;

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		if (bean instanceof RestTemplate) {
			this.configurer.extendMessageConverters(((RestTemplate) bean).getMessageConverters());
		}

		return bean;
	}
}
