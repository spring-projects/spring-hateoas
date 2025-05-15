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
package org.springframework.hateoas.config;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;

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
	 * @deprecated since 3.0
	 */
	@Deprecated
	default ObjectMapper configureObjectMapper(ObjectMapper mapper) {
		return configureObjectMapper(mapper.rebuild()).build();
	}

	default MapperBuilder<ObjectMapper, ?> configureObjectMapper(MapperBuilder<ObjectMapper, ?> builder) {

		var configured = builder
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		var module = getJacksonModule();

		return module == null ? configured : configured.addModule(module);
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
	default JacksonModule getJacksonModule() {
		return null;
	}
}
