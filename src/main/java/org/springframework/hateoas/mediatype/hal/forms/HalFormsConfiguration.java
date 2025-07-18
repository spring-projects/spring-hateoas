/*
 * Copyright 2017-2024 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.http.MediaType;
import org.springframework.lang.Contract;
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
	private final HalFormsOptionsFactory options;
	private final List<MediaType> mediaTypes;
	private final boolean defaultSingleTemplate;

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
		this(halConfiguration, new HashMap<>(), new HalFormsOptionsFactory(), __ -> {},
				Collections.singletonList(MediaTypes.HAL_FORMS_JSON), false);
	}

	private HalFormsConfiguration(HalConfiguration halConfiguration, Map<Class<?>, String> patterns,
			HalFormsOptionsFactory options, @Nullable Consumer<ObjectMapper> objectMapperCustomizer,
			List<MediaType> mediaTypes, boolean defaultSingleTemplate) {

		Assert.notNull(halConfiguration, "HalConfiguration must not be null!");
		Assert.notNull(patterns, "Patterns must not be null!");
		Assert.notNull(objectMapperCustomizer, "ObjectMapper customizer must not be null!");
		Assert.notNull(options, "HalFormsSuggests must not be null!");
		Assert.notNull(mediaTypes, "Media types must not be null!");

		this.halConfiguration = halConfiguration;
		this.patterns = patterns;
		this.objectMapperCustomizer = objectMapperCustomizer;
		this.options = options;
		this.mediaTypes = new ArrayList<>(mediaTypes);
		this.defaultSingleTemplate = defaultSingleTemplate;
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

		return new HalFormsConfiguration(halConfiguration, newPatterns, options, objectMapperCustomizer, mediaTypes,
				defaultSingleTemplate);
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
				: new HalFormsConfiguration(halConfiguration, patterns, options, objectMapperCustomizer, mediaTypes,
						defaultSingleTemplate);
	}

	/**
	 * Registers additional media types that are supposed to be aliases to {@link MediaTypes#HAL_FORMS_JSON}. Registered
	 * {@link MediaType}s will be preferred over the default one, i.e. they'll be listed first in client's accept headers
	 * etc.
	 *
	 * @param mediaType must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.4
	 */
	public HalFormsConfiguration withMediaType(MediaType mediaType) {

		Assert.notNull(mediaType, "MediaType must not be null!");

		if (mediaTypes.contains(mediaType)) {
			return this;
		}

		List<MediaType> newMediaTypes = new ArrayList<>(mediaTypes);
		newMediaTypes.add(mediaTypes.size() - 1, mediaType);

		return new HalFormsConfiguration(halConfiguration, patterns, options, objectMapperCustomizer, newMediaTypes,
				defaultSingleTemplate);
	}

	/**
	 * Customizes the given {@link ObjectMapper} with the registered callback.
	 *
	 * @param mapper must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @see #withObjectMapperCustomizer(Consumer)
	 */
	@Contract("_ -> this")
	public HalFormsConfiguration customize(ObjectMapper mapper) {

		Assert.notNull(mapper, "ObjectMapper must not be null!");

		objectMapperCustomizer.accept(mapper);

		return this;
	}

	/**
	 * Returns a new {@link HalFormsConfiguration} with the given
	 *
	 * @param <T>
	 * @param type the
	 * @param property
	 * @param creator
	 * @return
	 */
	public <T> HalFormsConfiguration withOptions(Class<T> type, String property,
			Function<PropertyMetadata, HalFormsOptions> creator) {

		return new HalFormsConfiguration(halConfiguration, patterns, options.withOptions(type, property, creator),
				objectMapperCustomizer, mediaTypes, defaultSingleTemplate);
	}

	/**
	 * Configures whether to use the name {@code default} in case only a single template appears. Defaults to
	 * {@literal false}. Set this to {@literal true} in case you need the legacy behavior.
	 *
	 * @param defaultSingleTemplate
	 * @return will never be {@literal null}.
	 * @since 3.0
	 */
	public HalFormsConfiguration withDefaultSingleTemplate(boolean defaultSingleTemplate) {

		return new HalFormsConfiguration(halConfiguration, patterns, options, objectMapperCustomizer, mediaTypes,
				defaultSingleTemplate);
	}

	/**
	 * Returns the underlying {@link HalConfiguration}.
	 *
	 * @return will never be {@literal null}.
	 */
	public HalConfiguration getHalConfiguration() {
		return halConfiguration;
	}

	/**
	 * Returns the {@link HalFormsOptionsFactory} to look up {@link HalFormsOptions} from payload and property metadata.
	 *
	 * @return will never be {@literal null}.
	 */
	HalFormsOptionsFactory getOptionsFactory() {
		return options;
	}

	/**
	 * Returns the regular expression pattern that is registered for the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	Optional<String> getTypePatternFor(ResolvableType type) {
		return Optional.ofNullable(patterns.get(type.resolve(Object.class)));
	}

	/**
	 * The {@link MediaType}s that we want to register this configuration for.
	 *
	 * @return will never be {@literal null}.
	 */
	List<MediaType> getMediaTypes() {
		return Collections.unmodifiableList(mediaTypes);
	}

	/**
	 * Returns whether to default the name of the first template added to a form. Available for backwards-compatibility
	 * reasons.
	 *
	 * @see https://github.com/mamund/hal-forms/issues/82
	 * @since 3.0
	 */
	boolean isDefaultSingleTemplate() {
		return defaultSingleTemplate;
	}
}
