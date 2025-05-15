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

import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.util.Assert;

/**
 * Value type to handle registration of hypermedia related {@link HttpMessageConverter}s.
 *
 * @author Oliver Drotbohm
 */
public class WebConverters {

	private static MediaType ANY_JSON = MediaType.parseMediaType("application/*+json");

	private final List<HypermediaMappingInformation> infos;
	private final JsonMapper mapper;

	/**
	 * Creates a new {@link WebConverters} from the given {@link JsonMapper} and {@link HypermediaMappingInformation}s.
	 *
	 * @param mapper must not be {@literal null}.
	 * @param mappingInformation must not be {@literal null}.
	 */
	private WebConverters(JsonMapper mapper, List<HypermediaMappingInformation> mappingInformation) {

		this.mapper = mapper;
		this.infos = mappingInformation;
	}

	/**
	 * Creates a new {@link WebConverters} from the given {@link JsonMapper} and {@link HypermediaMappingInformation}s.
	 *
	 * @param mapper must not be {@literal null}.
	 * @param mappingInformations must not be {@literal null}.
	 * @return
	 */
	public static WebConverters of(JsonMapper mapper, List<HypermediaMappingInformation> mappingInformations) {

		Assert.notNull(mapper, "JsonMapper must not be null!");
		Assert.notNull(mappingInformations, "Mapping information must not be null!");

		return new WebConverters(mapper, mappingInformations);
	}

	List<MediaType> getSupportedMediaTypes() {

		return infos.stream() //
				.flatMap(it -> it.getMediaTypes().stream())
				.collect(Collectors.toList());
	}

	/**
	 * Augments the given {@link List} of {@link HttpMessageConverter}s with the hypermedia enabled ones.
	 *
	 * @param converters must not be {@literal null}.
	 */
	public void augmentServer(List<HttpMessageConverter<?>> converters) {
		augment(converters, false);
	}

	public void augmentClient(List<HttpMessageConverter<?>> converters) {
		augment(converters, true);
	}

	private void augment(List<HttpMessageConverter<?>> converters, boolean includeGenericJsonTypes) {

		Assert.notNull(converters, "HttpMessageConverters must not be null!");

		var converter = converters.stream()
				.filter(it -> JacksonJsonHttpMessageConverter.class.equals(it.getClass()))
				.map(JacksonJsonHttpMessageConverter.class::cast)
				.findFirst()
				.orElseGet(() -> new JacksonJsonHttpMessageConverter(mapper));

		JsonMapper first = null;

		for (var info : infos) {

			var rootType = info.getRootType();
			var configured = info.configureJsonMapper(mapper.rebuild()).build();

			if (first == null) {
				first = configured;
			}

			var mappers = info.getMediaTypes().stream().distinct()
					.collect(Collectors.toMap(Function.identity(), __ -> configured));

			converter.registerMappersForType(rootType, map -> map.putAll(mappers));
		}

		if (!includeGenericJsonTypes || infos.isEmpty()) {
			return;
		}

		var rootType = infos.get(0).getRootType();
		var mapper = first;

		converter.registerMappersForType(rootType, map -> {
			Stream.of(MediaType.APPLICATION_JSON, ANY_JSON).forEach(it -> map.put(it, mapper));
		});
	}
}
