/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.Wither;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Greg Turnquist
 */
@Value
@Wither(AccessLevel.PACKAGE)
@JsonIgnoreProperties()
class CollectionJsonData {

	@JsonInclude(Include.NON_NULL) //
	private String name;

	@JsonInclude(Include.NON_NULL) //
	private Object value;

	@JsonInclude(Include.NON_NULL) //
	private String prompt;

	@JsonCreator
	CollectionJsonData(@JsonProperty("name") String name, @JsonProperty("value") Object value,
					   @JsonProperty("prompt") String prompt) {
		
		this.name = name;
		this.value = value;
		this.prompt = prompt;
	}

	CollectionJsonData() {
		this(null, null, null);
	}
}
