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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
final class CollectionJson<T> {

	private final String version;
	private @Nullable final String href;

	private @JsonInclude(Include.NON_EMPTY) final Links links;
	private @JsonInclude(Include.NON_EMPTY) final List<CollectionJsonItem<T>> items;
	private @JsonInclude(Include.NON_EMPTY) final List<CollectionJsonQuery> queries;
	private @JsonInclude(Include.NON_NULL) @Nullable final CollectionJsonTemplate template;
	private @JsonInclude(Include.NON_NULL) @Nullable final CollectionJsonError error;

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

	/**
	 * Create a new {@link CollectionJson} by copying attributes and replacing the {@literal version}.
	 *
	 * @param version
	 * @return
	 */
	CollectionJson<T> withVersion(String version) {

		return this.version == version ? this
				: new CollectionJson<T>(version, this.href, this.links, this.items, this.queries, this.template, this.error);
	}

	/**
	 * Create a new {@link CollectionJson} by copying attributes and replacing the {@literal href}.
	 *
	 * @param href
	 * @return
	 */
	CollectionJson<T> withHref(@Nullable String href) {

		return this.href == href ? this
				: new CollectionJson<T>(this.version, href, this.links, this.items, this.queries, this.template, this.error);
	}

	/**
	 * Create a new {@link CollectionJson} by copying attributes and replacing the {@link Link}s .
	 * 
	 * @param links
	 * @return
	 */
	CollectionJson<T> withLinks(Link... links) {
		return withLinks(Links.of(links));
	}

	/**
	 * Create a new {@link CollectionJson} by copying attributes and replacing the {@link Links}.
	 *
	 * @param links
	 * @return
	 */
	CollectionJson<T> withLinks(Links links) {

		return this.links == links ? this
				: new CollectionJson<T>(this.version, this.href, links, this.items, this.queries, this.template, this.error);
	}

	/**
	 * Create a new {@link CollectionJson} by copying attributes and replacing the {@link Links} with a {@literal self}
	 * {@link Link}.
	 *
	 * @return
	 */
	CollectionJson<T> withOwnSelfLink() {

		String href = this.href;

		if (href == null) {
			return this;
		}

		return withLinks(Links.of(Link.of(href)).merge(MergeMode.SKIP_BY_REL, links));
	}

	/**
	 * Create a new {@link CollectionJson} by copying attributes and replacing the {@literal items}.
	 *
	 * @param items
	 * @return
	 */
	@SafeVarargs
	final CollectionJson<T> withItems(CollectionJsonItem<T>... items) {
		return withItems(Arrays.asList(items));
	}

	/**
	 * Create a new {@link CollectionJson} by copying attributes and replacing the {@literal items}.
	 *
	 * @param items
	 * @return
	 */
	CollectionJson<T> withItems(List<CollectionJsonItem<T>> items) {

		return this.items == items ? this
				: new CollectionJson<T>(this.version, this.href, this.links, items, this.queries, this.template, this.error);
	}

	/**
	 * Create a new {@link CollectionJson} by copying attributes and replacing the {@literal queries}.
	 *
	 * @param queries
	 * @return
	 */
	CollectionJson<T> withQueries(List<CollectionJsonQuery> queries) {

		return this.queries == queries ? this
				: new CollectionJson<T>(this.version, this.href, this.links, this.items, queries, this.template, this.error);
	}

	/**
	 * Create a new {@link CollectionJson} by copying attributes and replacing the {@literal template}.
	 *
	 * @param template
	 * @return
	 */
	CollectionJson<T> withTemplate(@Nullable CollectionJsonTemplate template) {

		return this.template == template ? this
				: new CollectionJson<T>(this.version, this.href, this.links, this.items, this.queries, template, this.error);
	}

	/**
	 * Create a new {@link CollectionJson} by copying attributes and replacing the {@literal error}.
	 *
	 * @param error
	 * @return
	 */
	CollectionJson<T> withError(@Nullable CollectionJsonError error) {

		return this.error == error ? this
				: new CollectionJson<T>(this.version, this.href, this.links, this.items, this.queries, this.template, error);
	}

	/**
	 * Check if there are any {@literal items}.
	 * 
	 * @return
	 */
	boolean hasItems() {
		return !items.isEmpty();
	}

	@JsonProperty
	public String getVersion() {
		return this.version;
	}

	@JsonProperty
	@Nullable
	public String getHref() {
		return this.href;
	}

	@JsonProperty
	public Links getLinks() {
		return this.links;
	}

	@JsonProperty
	public List<CollectionJsonItem<T>> getItems() {
		return this.items;
	}

	@JsonProperty
	public List<CollectionJsonQuery> getQueries() {
		return this.queries;
	}

	@JsonProperty
	@Nullable
	public CollectionJsonTemplate getTemplate() {
		return this.template;
	}

	@JsonProperty
	@Nullable
	public CollectionJsonError getError() {
		return this.error;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CollectionJson<?> that = (CollectionJson<?>) o;
		return Objects.equals(this.version, that.version) && Objects.equals(this.href, that.href)
				&& Objects.equals(this.links, that.links) && Objects.equals(this.items, that.items)
				&& Objects.equals(this.queries, that.queries) && Objects.equals(this.template, that.template)
				&& Objects.equals(this.error, that.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.version, this.href, this.links, this.items, this.queries, this.template, this.error);
	}

	@Override
	public String toString() {

		return "CollectionJson{" + "version='" + this.version + '\'' + ", href='" + this.href + '\'' + ", links="
				+ this.links + ", items=" + this.items + ", queries=" + this.queries + ", template=" + this.template
				+ ", error=" + this.error + '}';
	}
}
