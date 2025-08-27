/*
 * Copyright 2019-2024 the original author or authors.
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

import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper.Builder;

import java.util.Collections;
import java.util.List;

import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.http.MediaType;

/**
 * @author Greg Turnquist
 */
public class CustomHypermediaType implements HypermediaMappingInformation {

	public static final MediaType FRODO_MEDIATYPE = MediaType.parseMediaType("application/frodo+json");

	/**
	 * {@link MediaType}s this hypermedia can handle.
	 */
	@Override
	public List<MediaType> getMediaTypes() {
		return Collections.singletonList(FRODO_MEDIATYPE);
	}

	/**
	 * Change mappers output format to indenting.
	 */
	@Override
	public Builder configureJsonMapper(Builder builder) {
		return builder.enable(SerializationFeature.INDENT_OUTPUT);
	}
}
