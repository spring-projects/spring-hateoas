/*
 * Copyright 2014-2016 the original author or authors.
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
package org.springframework.hateoas.mediatype.alps;

import lombok.Builder;
import lombok.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A value object for an ALPS ext element.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @since 0.15
 * @see http://alps.io/spec/#prop-ext
 */
@Value
@Builder
@JsonPropertyOrder({ "id", "href", "value" })
public class Ext {

	private final String id;
	private final String href;
	private final String value;

	@JsonCreator
	private Ext(@JsonProperty("id") String id, @JsonProperty("href") String href, @JsonProperty("value") String value) {

		this.id = id;
		this.href = href;
		this.value = value;
	}
}
