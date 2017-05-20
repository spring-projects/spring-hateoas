/*
 * Copyright 2014-2016 the original author or authors.
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
package org.springframework.hateoas.alps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import org.springframework.util.Assert;

/**
 * A value object for an ALPS doc element.
 * 
 * @author Oliver Gierke
 * @since 0.15
 * @see http://alps.io/spec/#prop-doc
 */
@Value
@Builder(builderMethodName = "doc")
@AllArgsConstructor
public class Doc {

	private final String href, value;
	private final Format format;

	/**
	 * Creates a new {@link Doc} instance with the given value and {@link Format}.
	 * 
	 * @param value must not be {@literal null} or empty.
	 * @param format must not be {@literal null}.
	 */
	public Doc(String value, Format format) {

		Assert.hasText(value, "Value must not be null or empty!");
		Assert.notNull(format, "Format must not be null!");

		this.href = null;
		this.value = value;
		this.format = format;
	}
}
