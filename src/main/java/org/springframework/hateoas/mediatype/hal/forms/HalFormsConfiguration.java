/*
 * Copyright 2017-2021 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HAL-FORMS specific configuration extension of {@link HalConfiguration}.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class HalFormsConfiguration {

	private final HalConfiguration halConfiguration;
	private final Map<Class<?>, String> patterns;
	private final Consumer<ObjectMapper> objectMapperCustomizer;

	/**
	 * Creates a new {@link HalFormsConfiguration} backed by a default {@link HalConfiguration}.
	 */
	public HalFormsConfiguration() {
		this(new HalConfiguration());
	}

	/**
	 * Creates a new {@link HalFormsConfiguration} for the given {@link HalConfiguration}.
	 *
	 * @param halConfiguration must not be {@literal null}.
	 */
	public HalFormsConfiguration(HalConfiguration halConfiguration) {
		this(halConfiguration, new HashMap<>(), __ -> {});
	}

	private HalFormsConfiguration(HalConfiguration halConfiguration, Map<Class<?>, String> patterns,
			@Nullable Consumer<ObjectMapper> objectMapperCustomizer) {

		Assert.notNull(halConfiguration, "HalConfiguration must not be null!");
		Assert.notNull(patterns, "Patterns must not be null!");
		Assert.notNull(objectMapperCustomizer, "ObjectMapper customizer must not be null!");

		this.halConfiguration = halConfiguration;
		this.patterns = patterns;
		this.objectMapperCustomizer = objectMapperCustomizer;
	}

	/**
	 * Registers a regular expression pattern to be used for form descriptions of the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @param pattern must not be {@literal null} or empty.
	 * @return will never be {@literal null}.
	 * @deprecated prefer {@link #withPattern(Class, String)} that returns a fresh instance, to be removed with 1.3.
	 */
	@Deprecated
	public HalFormsConfiguration registerPattern(Class<?> type, String pattern) {

		Assert.notNull(type, "Type must not be null!");
		Assert.hasText(pattern, "Pattern must not be null or empty!");

		patterns.put(type, pattern);

		return this;
	}

	/**
	 * Registers a regular expression pattern to be used for form descriptions of the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @param pattern must not be {@literal null} or empty.
	 * @return will never be {@literal null}.
	 */
	public HalFormsConfiguration withPattern(Class<?> type, String pattern) {

		Assert.notNull(type, "Type must not be null!");
		Assert.hasText(pattern, "Pattern must not be null or empty!");

		Map<Class<?>, String> newPatterns = new HashMap<>(patterns);
		newPatterns.put(type, pattern);

		return new HalFormsConfiguration(halConfiguration, newPatterns, objectMapperCustomizer);
	}

	/**
	 * Register the given {@link Consumer} to apply additional customizations on the {@link ObjectMapper} used to render
	 * HAL documents.
	 *
	 * @param objectMapperCustomizer must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalFormsConfiguration withObjectMapperCustomizer(Consumer<ObjectMapper> objectMapperCustomizer) {

		Assert.notNull(objectMapperCustomizer, "ObjectMapper customizer must not be null!");

		return this.objectMapperCustomizer == objectMapperCustomizer //
				? this //
				: new HalFormsConfiguration(halConfiguration, patterns, objectMapperCustomizer);
	}

	/**
	 * Customizes the given {@link ObjectMapper} with the registered callback.
	 *
	 * @param mapper must not be {@literal null}.
	 * @return
	 * @see #withObjectMapperCustomizer(Consumer)
	 */
	public HalFormsConfiguration customize(ObjectMapper mapper) {

		Assert.notNull(mapper, "ObjectMapper must not be null!");

		objectMapperCustomizer.accept(mapper);

		return this;
	}

	public HalConfiguration getHalConfiguration() {
		return halConfiguration;
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
