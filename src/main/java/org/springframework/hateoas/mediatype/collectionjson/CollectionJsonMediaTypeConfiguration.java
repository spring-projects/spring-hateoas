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
package org.springframework.hateoas.mediatype.collectionjson;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.Module;

/**
 * Configuration setup for Collection/JSON.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@Configuration(proxyBeanMethods = false)
class CollectionJsonMediaTypeConfiguration implements HypermediaMappingInformation {

	@Bean
	LinkDiscoverer collectionJsonLinkDiscoverer() {
		return new CollectionJsonLinkDiscoverer();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#getMediaTypes()
	 */
	@Override
	public List<MediaType> getMediaTypes() {
		return HypermediaType.COLLECTION_JSON.getMediaTypes();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#getJacksonModule()
	 */
	@NonNull
	@Override
	public Module getJacksonModule() {
		return new Jackson2CollectionJsonModule();
	}
}
