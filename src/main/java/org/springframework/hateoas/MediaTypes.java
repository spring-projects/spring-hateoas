/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas;

import org.springframework.http.MediaType;

/**
 * Constants for well-known hypermedia types.
 * 
 * @author Oliver Gierke
 * @author Przemek Nowak
 * @author Drummond Dawson
 * @author Greg Turnquist
 */
public class MediaTypes {

	/**
	 * A String equivalent of {@link MediaTypes#HAL_JSON}.
	 */
	public static final String HAL_JSON_VALUE = "application/hal+json";

	/**
	 * Public constant media type for {@code application/hal+json}.
	 */
	public static final MediaType HAL_JSON = MediaType.valueOf(HAL_JSON_VALUE);

	/**
	 * A String equivalent of {@link MediaTypes#HAL_JSON_UTF8}.
	 */
	public static final String HAL_JSON_UTF8_VALUE = HAL_JSON_VALUE + ";charset=UTF-8";

	/**
	 * Public constant media type for {@code application/hal+json;charset=UTF-8}.
	 */
	public static final MediaType HAL_JSON_UTF8 = MediaType.valueOf(HAL_JSON_UTF8_VALUE);

	/**
	 * A String equivalent of {@link MediaTypes#ALPS_JSON}.
	 */
	public static final String ALPS_JSON_VALUE = "application/alps+json";

	/**
	 * Public constant media type for {@code application/alps+json}.
	 */
	public static final MediaType ALPS_JSON = MediaType.parseMediaType(ALPS_JSON_VALUE);
}
