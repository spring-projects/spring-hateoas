/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.hateoas.server.core;

import org.springframework.core.env.Environment;

/**
 * {@code EnvironmentContext} holds the {@link Environment} of the current servlet request provided by the
 * {@link EnvironmentContextFilter}. The {@link Environment} is needed by the {@link PropertyResolvingMappingDiscoverer}
 * to be able to resolve properties.
 *
 * @author Lars Michele
 */
class EnvironmentContext {

	private static final ThreadLocal<Environment> ENVIRONMENT = new ThreadLocal<>();

	static Environment get() {
		return ENVIRONMENT.get();
	}

	static void set(Environment environment) {
		ENVIRONMENT.set(environment);
	}

	static void clear() {
		ENVIRONMENT.remove();
	}
}
