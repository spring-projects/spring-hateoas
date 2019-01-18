/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.config.reactive;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@link BeanPostProcessor} to register the proper handlers in {@link WebClient} instances found in the
 * application context.
 * 
 * @author Greg Turnquist
 * @since 1.0
 */
@RequiredArgsConstructor
public class HypermediaWebClientBeanPostProcessor implements BeanPostProcessor {

	private final WebClientConfigurer configurer;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		if (bean instanceof WebClient) {
			return this.configurer.registerHypermediaTypes((WebClient) bean);
		}

		return bean;
	}
}
