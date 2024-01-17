/*
 * Copyright 2017-2024 the original author or authors.
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
package org.springframework.hateoas.mediatype;

import org.springframework.hateoas.AffordanceModel;
import org.springframework.http.MediaType;

/**
 * SPI for media type implementations to create a specific {@link AffordanceModel} for a {@link ConfiguredAffordance}.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public interface AffordanceModelFactory {

	/**
	 * Declare the {@link MediaType} this factory supports.
	 *
	 * @return
	 */
	MediaType getMediaType();

	/**
	 * Return the {@link AffordanceModel} for the given {@link ConfiguredAffordance}.
	 *
	 * @param configured will never be {@literal null}.
	 * @return must not be {@literal null}.
	 * @since 1.3
	 */
	AffordanceModel getAffordanceModel(ConfiguredAffordance configured);
}
