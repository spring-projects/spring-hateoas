/*
 * Copyright 2018-2020 the original author or authors.
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

import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.Wither;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Greg Turnquist
 */
@Value
@Wither(AccessLevel.PACKAGE)
class CollectionJsonError {

	private String title;
	private String code;
	private String message;

	@JsonCreator
	CollectionJsonError(@JsonProperty("title") @Nullable String title, @JsonProperty("code") @Nullable String code,
						@JsonProperty("message") @Nullable String message) {

		this.title = title;
		this.code = code;
		this.message = message;
	}

	CollectionJsonError() {
		this(null, null, null);
	}

}
