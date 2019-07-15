/*
 * Copyright 2017-2019 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;

/**
 * HAL-FORMS specific configuration extension of {@link HalConfiguration}.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@RequiredArgsConstructor
public class HalFormsConfiguration {

	private final @Getter HalConfiguration halConfiguration;
	private final Map<Class<?>, String> patterns = new HashMap<>();

	/**
	 * Creates a new {@link HalFormsConfiguration} backed by a default {@link HalConfiguration}.
	 */
	public HalFormsConfiguration() {
		this.halConfiguration = new HalConfiguration();
	}

	public HalFormsConfiguration registerPattern(Class<?> type, String pattern) {

		patterns.put(type, pattern);

		return this;
	}

	/**
	 * Returns the regular expression pattern that is registered for the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @return
	 */
	Optional<String> getTypePatternFor(ResolvableType type) {
		return Optional.ofNullable(patterns.get(type.resolve(Object.class)));
	}
}
