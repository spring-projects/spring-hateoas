/*
 * Copyright 2021-2024 the original author or authors.
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

import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.JacksonCodecSupport;

/**
 * @author Oliver Drotbohm
 */
class WebfluxCodecCustomizer implements Consumer<Object> {

	private static final MediaType ANY_JSON = MediaType.parseMediaType("application/*+json");

	private final List<HypermediaMappingInformation> mappingInformations;
	private final JsonMapper mapper;
	private final boolean withGenericJsonTypes;

	/**
	 * @param mappingInformations
	 * @param mapper
	 */
	public WebfluxCodecCustomizer(List<HypermediaMappingInformation> mappingInformations, JsonMapper mapper) {
		this(mappingInformations, mapper, false);
	}

	private WebfluxCodecCustomizer(List<HypermediaMappingInformation> mappingInformations, JsonMapper mapper,
			boolean withGenericJsonTypes) {

		this.mappingInformations = mappingInformations;
		this.mapper = mapper;
		this.withGenericJsonTypes = withGenericJsonTypes;
	}

	WebfluxCodecCustomizer withGenericJsonTypes() {
		return new WebfluxCodecCustomizer(mappingInformations, mapper, true);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void accept(@Nullable Object it) {

		if (it == null || !JacksonCodecSupport.class.isInstance(it)) {
			return;
		}

		var codec = (JacksonCodecSupport<JsonMapper>) it;
		JsonMapper firstMapper = null;

		for (HypermediaMappingInformation information : mappingInformations) {

			var configured = information.configureJsonMapper(mapper.rebuild()).build();

			if (firstMapper == null) {
				firstMapper = configured;
			}

			for (MediaType mediaType : information.getMediaTypes()) {
				codec.registerMappersForType(information.getRootType(), map -> {
					map.put(mediaType, configured);
				});
			}
		}

		if (!withGenericJsonTypes) {
			return;
		}

		Class<?> type = mappingInformations.get(0).getRootType();
		JsonMapper mapper = firstMapper;

		codec.registerMappersForType(type, map -> {
			Stream.of(MediaType.APPLICATION_JSON, ANY_JSON).forEach(mediaType -> map.put(mediaType, mapper));
		});
	}
}
