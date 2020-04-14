/*
 * Copyright 2019-2020 the original author or authors.
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

import java.util.Collections;
import java.util.List;

import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
	 * Copy the incoming {@link ObjectMapper} and change it's output format along with disabling failure on unknown
	 * properties.
	 */
	@Override
	public ObjectMapper configureObjectMapper(ObjectMapper mapper) {

		mapper.registerModule(new Jackson2HalModule());
		mapper.setHandlerInstantiator(new HalHandlerInstantiator(new EvoInflectorLinkRelationProvider(),
				CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY));
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		return mapper;
	}
}
