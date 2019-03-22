/*
 * Copyright 2019 the original author or authors.
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

import java.util.Collection;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.hateoas.config.MediaTypeConfigurationProvider;
import org.springframework.http.MediaType;

/**
 * {@link MediaTypeConfigurationProvider} for Collection/JSON.
 *
 * @author Oliver Drotbohm
 */
class CollectionJsonMediaTypeConfigurationProvider implements MediaTypeConfigurationProvider {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.MediaTypeConfigurationProvider#getConfiguration()
	 */
	@Override
	public Class<? extends HypermediaMappingInformation> getConfiguration() {
		return CollectionJsonMediaTypeConfiguration.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.MediaTypeConfigurationProvider#supportsAny(java.util.Collection)
	 */
	@Override
	public boolean supportsAny(Collection<MediaType> mediaTypes) {
		return mediaTypes.contains(MediaTypes.COLLECTION_JSON);
	}
}
