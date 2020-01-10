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

import java.util.Collection;

import org.springframework.http.MediaType;

/**
 * SPI to register a media type configuration provider.
 *
 * @author Oliver Drotbohm
 * @see HypermediaMappingInformation
 */
public interface MediaTypeConfigurationProvider {

	/**
	 * Returns the primary Spring configuration class to be bootstrapped for the given media type.
	 *
	 * @return
	 */
	Class<? extends HypermediaMappingInformation> getConfiguration();

	/**
	 * Returns whether the provider supports any of the given {@link MediaType}s. Used to select the providers to be
	 * included into a configuration setup in case the media types to be enabled are explicitly defined.
	 *
	 * @param mediaTypes will never be {@literal null}.
	 * @return
	 */
	boolean supportsAny(Collection<MediaType> mediaTypes);
}
