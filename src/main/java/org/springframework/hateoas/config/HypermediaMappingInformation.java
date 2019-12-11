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
package org.springframework.hateoas.config;

import java.util.List;
import java.util.Optional;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface for registering custom hypermedia handlers.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public interface HypermediaMappingInformation {

	/**
	 * All {@link MediaType}s this hypermedia can handle.
	 *
	 * @return
	 */
	List<MediaType> getMediaTypes();

	/**
	 * Return the type that this hypermedia type is represented by. Default implementation returns
	 * {@link RepresentationModel} as it's the base class most media type serializations work with.
	 *
	 * @return the type that this hypermedia type is represented by.
	 * @since 1.1
	 */
	default Class<?> getRootType() {
		return RepresentationModel.class;
	}

	/**
	 * Configure an {@link ObjectMapper} and register custom serializers and deserializers for the supported media types.
	 * If all you want to do is register a Jackson {@link Module}, prefer implementing {@link #getJacksonModule()}.
	 *
	 * @return
	 * @see #getJacksonModule()
	 */
	default ObjectMapper configureObjectMapper(ObjectMapper mapper) {

		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		Optional.ofNullable(getJacksonModule()).ifPresent(mapper::registerModule);

		return mapper;
	}

	/**
	 * Optionally return the Jackson {@link Module} to be used to customize the serialization of representation models.
	 * Override this if there's nothing but the module to be done to setup the {@link ObjectMapper}. For more advanced
	 * needs, see {@link #configureObjectMapper(ObjectMapper)}.
	 *
	 * @return
	 * @see #configureObjectMapper(ObjectMapper)
	 */
	@Nullable
	default Module getJacksonModule() {
		return null;
	}
}
