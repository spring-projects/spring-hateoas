/*
 * Copyright 2021 the original author or authors.
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

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Oliver Drotbohm
 */
class WebfluxCodecCustomizer implements Consumer<Object> {

	private static final MediaType ANY_JSON = MediaType.parseMediaType("application/*+json");

	private final List<HypermediaMappingInformation> mappingInformations;
	private final ObjectMapper mapper;
	private final boolean withGenericJsonTypes;

	/**
	 * @param mappingInformations
	 * @param mapper
	 */
	public WebfluxCodecCustomizer(List<HypermediaMappingInformation> mappingInformations, ObjectMapper mapper) {
		this(mappingInformations, mapper, false);
	}

	private WebfluxCodecCustomizer(List<HypermediaMappingInformation> mappingInformations, ObjectMapper mapper,
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
	public void accept(@Nullable Object it) {

		if (it == null || !Jackson2CodecSupport.class.isInstance(it)) {
			return;
		}

		Jackson2CodecSupport codec = (Jackson2CodecSupport) it;
		ObjectMapper firstMapper = null;

		for (HypermediaMappingInformation information : mappingInformations) {

			ObjectMapper objectMapper = information.configureObjectMapper(mapper.copy());

			if (firstMapper == null) {
				firstMapper = objectMapper;
			}

			for (MediaType mediaType : information.getMediaTypes()) {
				codec.registerObjectMappersForType(information.getRootType(), map -> {
					map.put(mediaType, objectMapper);
				});
			}
		}

		if (!withGenericJsonTypes) {
			return;
		}

		Class<?> type = mappingInformations.get(0).getRootType();
		ObjectMapper mapper = firstMapper;

		codec.registerObjectMappersForType(type, map -> {
			Stream.of(MediaType.APPLICATION_JSON, ANY_JSON).forEach(mediaType -> map.put(mediaType, mapper));
		});
	}
}
