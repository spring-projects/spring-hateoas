/*
 * Copyright 2020-2021 the original author or authors.
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

import org.springframework.web.client.RestTemplate;

/**
 * Assembles hypermedia-based message converters and applies them to an existing {@link RestTemplate}.
 *
 * @author Greg Turnquist
 * @since 1.1
 */
public class HypermediaRestTemplateConfigurer {

	private final WebConverters converters;

	/**
	 * Creates a new {@link HypermediaRestTemplateConfigurer} using the {@link WebConverters}.
	 *
	 * @param converters
	 */
	HypermediaRestTemplateConfigurer(WebConverters converters) {
		this.converters = converters;
	}

	/**
	 * Insert hypermedia-aware message converters in front of any other existing message converters.
	 *
	 * @param template
	 * @return {@link RestTemplate} capable of speaking hypermedia.
	 */
	public RestTemplate registerHypermediaTypes(RestTemplate template) {

		converters.augmentClient(template.getMessageConverters());

		return template;
	}
}
