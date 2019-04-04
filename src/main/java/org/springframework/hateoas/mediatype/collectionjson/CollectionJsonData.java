/*
 * Copyright 2017 the original author or authors.
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

import lombok.Value;
import lombok.experimental.Wither;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Greg Turnquist
 */
@Value
@Wither
@JsonIgnoreProperties()
class CollectionJsonData {

	@JsonInclude(Include.NON_NULL) //
	private @Nullable String name;

	@JsonInclude(Include.NON_NULL) //
	private @Nullable Object value;

	@JsonInclude(Include.NON_NULL) //
	private @Nullable String prompt;

	@JsonCreator
	CollectionJsonData(@JsonProperty("name") @Nullable String name, //
			@JsonProperty("value") @Nullable Object value, //
			@JsonProperty("prompt") @Nullable String prompt) {

		this.name = name;
		this.value = value;
		this.prompt = prompt;
	}

	CollectionJsonData() {
		this(null, null, null);
	}
}
