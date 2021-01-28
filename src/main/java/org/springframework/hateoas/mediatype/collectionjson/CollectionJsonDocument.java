/*
 * Copyright 2015-2021 the original author or authors.
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

import java.util.List;
import java.util.Objects;

import org.springframework.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an entire Collection+JSON document.
 *
 * @author Greg Turnquist
 */
final class CollectionJsonDocument<T> {

	private final CollectionJson<T> collection;

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

	CollectionJsonDocument(CollectionJson<T> collection) {
		this.collection = collection;
	}

	/**
	 * Create a new {@link CollectionJsonDocument} by copying the attributes and replacing the {@literal collection}.
	 *
	 * @param collection
	 * @return
	 */
	CollectionJsonDocument<T> withCollection(CollectionJson<T> collection) {
		return this.collection == collection ? this : new CollectionJsonDocument<T>(this.collection);
	}

	@JsonProperty
	public CollectionJson<T> getCollection() {
		return this.collection;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CollectionJsonDocument<?> that = (CollectionJsonDocument<?>) o;
		return Objects.equals(this.collection, that.collection);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.collection);
	}

	public String toString() {
		return "CollectionJsonDocument(collection=" + this.collection + ")";
	}

}
