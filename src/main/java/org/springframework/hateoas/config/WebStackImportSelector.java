/*
 * Copyright 2019 the original author or authors.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.hateoas.support.WebStack;

/**
 * {@link ImportSelector} to include web stack specific configuration.
 *
 * @author Oliver Drotbohm
 */
class WebStackImportSelector implements ImportSelector {

	private static final String WEB_STACK_MISSING = "At least one web stack has to be selected in @EnableHypermediaSupport on %s!";

	static Map<WebStack, String> CONFIGS;

	static {

		Map<WebStack, String> configs = new HashMap<>();

		configs.put(WebStack.WEBMVC, "org.springframework.hateoas.config.WebMvcHateoasConfiguration");
		configs.put(WebStack.WEBFLUX, "org.springframework.hateoas.config.WebFluxHateoasConfiguration");
		configs.put(WebStack.REST_TEMPLATE, "org.springframework.hateoas.config.RestTemplateHateoasConfiguration");
		configs.put(WebStack.WEB_CLIENT, "org.springframework.hateoas.config.WebClientHateoasConfiguration");

		CONFIGS = Collections.unmodifiableMap(configs);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.annotation.ImportSelector#selectImports(org.springframework.core.type.AnnotationMetadata)
	 */
	@Override
	public String[] selectImports(AnnotationMetadata metadata) {

		Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableHypermediaSupport.class.getName());

		// Configuration class imported but not through @EnableHypermediaSupport

		if (attributes == null) {
			return new String[0];
		}

		WebStack[] stacks = (WebStack[]) attributes.get("stacks");

		if (stacks.length == 0) {
			throw new IllegalStateException(String.format(WEB_STACK_MISSING, metadata.getClassName()));
		}

		return Arrays.stream(stacks) //
				.filter(WebStack::isAvailable) //
				.map(CONFIGS::get) //
				.toArray(String[]::new);
	}
}
