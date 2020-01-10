/*
 * Copyright 2015-2020 the original author or authors.
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;

import org.springframework.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an entire Collection+JSON document.
 *
 * @author Greg Turnquist
 */
@Value
@Getter(onMethod = @__(@JsonProperty))
@Wither(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class CollectionJsonDocument<T> {

	private CollectionJson<T> collection;

	@JsonCreator
	CollectionJsonDocument(@JsonProperty("version") String version, //
			@JsonProperty("href") String href, //
			@JsonProperty("links") Links links, //
			@JsonProperty("items") List<CollectionJsonItem<T>> items, //
			@JsonProperty("queries") List<CollectionJsonQuery> queries, //
			@JsonProperty("template") CollectionJsonTemplate template, //
			@JsonProperty("error") CollectionJsonError error) {
		this.collection = new CollectionJson<>(version, href, links, items, queries, template, error);
	}
}
