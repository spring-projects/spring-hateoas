/*
 * Copyright 2018 the original author or authors.
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

import static com.fasterxml.jackson.annotation.JsonInclude.*;

import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Greg Turnquist
 */
@Value
@Wither
class CollectionJsonQuery {

	@JsonInclude(Include.NON_NULL) private String rel;

	@JsonInclude(Include.NON_NULL) private String href;

	@JsonInclude(Include.NON_NULL) private String prompt;

	@JsonInclude(Include.NON_EMPTY) private List<CollectionJsonData> data;

	@JsonCreator
	CollectionJsonQuery(@JsonProperty("rel") @Nullable String rel, @JsonProperty("href") @Nullable String href,
			@JsonProperty("prompt") @Nullable String prompt, @JsonProperty("data") @Nullable List<CollectionJsonData> data) {

		this.rel = rel;
		this.href = href;
		this.prompt = prompt;
		this.data = data;
	}

	CollectionJsonQuery() {
		this(null, null, null, null);
	}
}
