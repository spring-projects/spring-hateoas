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
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Links.MergeMode;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of the "collection" part of a Collection+JSON document.
 *
 * @author Greg Turnquist
 */
@Value
@Getter(onMethod = @__(@JsonProperty))
@Wither(AccessLevel.PACKAGE)
class CollectionJson<T> {

	private String version;
	private @Nullable String href;

	private @JsonInclude(Include.NON_EMPTY) Links links;
	private @JsonInclude(Include.NON_EMPTY) List<CollectionJsonItem<T>> items;
	private @JsonInclude(Include.NON_EMPTY) List<CollectionJsonQuery> queries;
	private @JsonInclude(Include.NON_NULL) @Nullable CollectionJsonTemplate template;
	private @JsonInclude(Include.NON_NULL) @Nullable CollectionJsonError error;

	@JsonCreator
	CollectionJson(@JsonProperty("version") String version, //
			@JsonProperty("href") @Nullable String href, //
			@JsonProperty("links") @Nullable Links links, //
			@JsonProperty("items") @Nullable List<CollectionJsonItem<T>> items, //
			@JsonProperty("queries") @Nullable List<CollectionJsonQuery> queries, //
			@JsonProperty("template") @Nullable CollectionJsonTemplate template, //
			@JsonProperty("error") @Nullable CollectionJsonError error) {

		this.version = version;
		this.href = href;
		this.links = links == null ? Links.NONE : links;
		this.items = items == null ? Collections.emptyList() : items;
		this.queries = queries == null ? Collections.emptyList() : queries;
		this.template = template;
		this.error = error;
	}

	CollectionJson() {
		this("1.0", null, Links.NONE, Collections.emptyList(), null, null, null);
	}

	@SafeVarargs
	final CollectionJson<T> withItems(CollectionJsonItem<T>... items) {
		return withItems(Arrays.asList(items));
	}

	CollectionJson<T> withItems(List<CollectionJsonItem<T>> items) {
		return new CollectionJson<>(version, href, links, items, queries, template, error);
	}

	CollectionJson<T> withLinks(Link... links) {
		return withLinks(Links.of(links));
	}

	CollectionJson<T> withLinks(Links links) {
		return new CollectionJson<>(version, href, links, items, queries, template, error);
	}

	CollectionJson<T> withOwnSelfLink() {

		String href = this.href;

		if (href == null) {
			return this;
		}

		return withLinks(Links.of(Link.of(href)).merge(MergeMode.SKIP_BY_REL, links));
	}

	boolean hasItems() {
		return !items.isEmpty();
	}
}
