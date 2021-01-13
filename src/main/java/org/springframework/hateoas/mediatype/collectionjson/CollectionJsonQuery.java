/*
 * Copyright 2018-2021 the original author or authors.
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

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Greg Turnquist
 */
final class CollectionJsonQuery {

	private @JsonInclude(Include.NON_NULL) final String rel;
	private @JsonInclude(Include.NON_NULL) final String href;
	private @JsonInclude(Include.NON_NULL) final String prompt;
	private @JsonInclude(Include.NON_EMPTY) final List<CollectionJsonData> data;

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

	/**
	 * Create a new {@link CollectionJsonQuery} by copying attributes and replacing the {@literal rel}.
	 *
	 * @param rel
	 * @return
	 */
	public CollectionJsonQuery withRel(String rel) {
		return this.rel == rel ? this : new CollectionJsonQuery(rel, this.href, this.prompt, this.data);
	}

	/**
	 * Create a new {@link CollectionJsonQuery} by copying attributes and replacing the {@literal href}.
	 *
	 * @param href
	 * @return
	 */
	public CollectionJsonQuery withHref(String href) {
		return this.href == href ? this : new CollectionJsonQuery(this.rel, href, this.prompt, this.data);
	}

	/**
	 * Create a new {@link CollectionJsonQuery} by copying attributes and replacing the {@literal prompt}.
	 *
	 * @param prompt
	 * @return
	 */
	public CollectionJsonQuery withPrompt(String prompt) {
		return this.prompt == prompt ? this : new CollectionJsonQuery(this.rel, this.href, prompt, this.data);
	}

	/**
	 * Create a new {@link CollectionJsonQuery} by copying attributes and replacing the {@literal data}.
	 *
	 * @param data
	 * @return
	 */
	public CollectionJsonQuery withData(List<CollectionJsonData> data) {
		return this.data == data ? this : new CollectionJsonQuery(this.rel, this.href, this.prompt, data);
	}

	public String getRel() {
		return this.rel;
	}

	public String getHref() {
		return this.href;
	}

	public String getPrompt() {
		return this.prompt;
	}

	public List<CollectionJsonData> getData() {
		return this.data;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CollectionJsonQuery that = (CollectionJsonQuery) o;
		return Objects.equals(this.rel, that.rel) && Objects.equals(this.href, that.href)
				&& Objects.equals(this.prompt, that.prompt) && Objects.equals(this.data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.rel, this.href, this.prompt, this.data);
	}

	public String toString() {

		return "CollectionJsonQuery(rel=" + this.rel + ", href=" + this.href + ", prompt=" + this.prompt + ", data="
				+ this.data + ")";
	}

}
