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
package org.springframework.hateoas.config.mvc;

import java.util.Collection;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.Hypermedia;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.hateoas.hal.forms.HalFormsConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring MVC HATEOAS Configuration
 *
 * @author Greg Turnquist
 */
@Configuration
public class WebMvcHateoasConfiguration {

	@Bean
	HypermediaWebMvcConfigurer hypermediaWebMvcConfigurer(ObjectProvider<ObjectMapper> mapper,
			DelegatingRelProvider relProvider, ObjectProvider<CurieProvider> curieProvider,
			ObjectProvider<HalConfiguration> halConfiguration, ObjectProvider<HalFormsConfiguration> halFormsConfiguration,
			Collection<Hypermedia> hypermediaTypes) {

		return new HypermediaWebMvcConfigurer(mapper.getIfAvailable(ObjectMapper::new), relProvider,
				curieProvider.getIfAvailable(), halConfiguration.getIfAvailable(HalConfiguration::new),
				halFormsConfiguration.getIfAvailable(HalFormsConfiguration::new), hypermediaTypes);
	}

	@Bean
	HypermediaRestTemplateBeanPostProcessor restTemplateBeanPostProcessor(HypermediaWebMvcConfigurer configurer) {
		return new HypermediaRestTemplateBeanPostProcessor(configurer);
	}
}
