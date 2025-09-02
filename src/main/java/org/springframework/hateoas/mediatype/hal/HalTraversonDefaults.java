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
package org.springframework.hateoas.mediatype.hal;

import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.TraversonDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

/**
 * Traverson defaults to support HAL.
 *
 * @author Oliver Drotbohm
 */
class HalTraversonDefaults implements TraversonDefaults {

	private static final List<MediaType> HAL_FLAVORS = List.of(MediaTypes.HAL_JSON, MediaTypes.VND_HAL_JSON,
			MediaTypes.HAL_FORMS_JSON);

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.TraversonDefaults#getHttpMessageConverters(java.util.Collection)
	 */
	@Override
	public List<HttpMessageConverter<?>> getHttpMessageConverters(Collection<MediaType> mediaTypes) {

		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));

		List<MediaType> halFlavors = mediaTypes.stream() //
				.filter(HAL_FLAVORS::contains) //
				.collect(Collectors.toList());

		if (!halFlavors.isEmpty()) {
			converters.add(getHalConverter(halFlavors));
		}

		return converters;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.TraversonDefaults#getLinkDiscoverers(java.util.Collection)
	 */
	@Override
	public List<LinkDiscoverer> getLinkDiscoverers(Collection<MediaType> mediaTypes) {

		return mediaTypes.stream().anyMatch(it -> HAL_FLAVORS.stream().anyMatch(it::isCompatibleWith)) //
				? List.of(new HalLinkDiscoverer()) //
				: Collections.emptyList();
	}

	/**
	 * Creates a new {@link HttpMessageConverter} to support HAL.
	 *
	 * @return
	 */
	private static HttpMessageConverter<?> getHalConverter(List<MediaType> halFlavours) {

		var mapper = JsonMapper.builder()
				.addModule(new HalJacksonModule())
				.build();

		var converter = new JacksonJsonHttpMessageConverter(mapper);
		converter.setSupportedMediaTypes(halFlavours);

		return converter;
	}
}
