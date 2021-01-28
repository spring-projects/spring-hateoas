/*
 * Copyright 2019-2021 the original author or authors.
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
package org.springframework.hateoas.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ClassUtils;

/**
 * Utility to glean what web stack is currently available.
 * 
 * @author Greg Turnquist
 */
public enum WebStack {

	WEBMVC("org.springframework.web.servlet.DispatcherServlet", //
			"org.springframework.hateoas.config.WebMvcHateoasConfiguration", //
			"org.springframework.web.client.RestTemplate", //
			"org.springframework.hateoas.config.RestTemplateHateoasConfiguration"),

	WEBFLUX("org.springframework.web.reactive.DispatcherHandler", //
			"org.springframework.hateoas.config.WebFluxHateoasConfiguration", //
			"org.springframework.web.reactive.function.client.WebClient", //
			"org.springframework.hateoas.config.WebClientHateoasConfiguration");

	private final boolean isServerAvailable;
	private final String serverConfiguration;

	private final boolean isClientAvailable;
	private final String clientConfiguration;

	/**
	 * Initialize the {@link #isAvailable} based upon a defined signature class.
	 */
	WebStack(String serverAvailableClazz, String serverConfigurationClazz, String clientAvailableClazz,
			String clientConfigurationClazz) {

		this.isServerAvailable = ClassUtils.isPresent(serverAvailableClazz, null);
		this.serverConfiguration = serverConfigurationClazz;

		this.isClientAvailable = ClassUtils.isPresent(clientAvailableClazz, null);
		this.clientConfiguration = clientConfigurationClazz;
	}

	/**
	 * Based on what client/server components are on the classpath, return what configuration classes should be
	 * registered.
	 */
	public List<String> getAvailableConfigurations() {

		List<String> configurations = new ArrayList<>();

		if (this.isServerAvailable) {
			configurations.add(this.serverConfiguration);
		}

		if (this.isClientAvailable) {
			configurations.add(this.clientConfiguration);
		}

		return configurations;
	}

	/**
	 * Is this web stack on the classpath?
	 *
	 * @deprecated Will be removed in 1.2 in light of {@link #getAvailableConfigurations()}.
	 */
	@Deprecated
	public boolean isAvailable() {
		return this.isServerAvailable;
	}
}
