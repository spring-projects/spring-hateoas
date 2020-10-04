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

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Class to statically provide the {@link Environment} of the current application for the property resolving of the
 * {@link PropertyResolvingMappingDiscoverer}.
 *
 * @author Lars Michele
 */
@Component
class StaticEnvironmentProvider implements EnvironmentAware {

	private static final EnvironmentHolder HOLDER = new EnvironmentHolder();

	@Override
	public void setEnvironment(Environment environment) {
		HOLDER.environment = environment;
	}

	static Environment get() {
		return HOLDER.environment;
	}

	private static class EnvironmentHolder {

		private Environment environment;
	}
}
