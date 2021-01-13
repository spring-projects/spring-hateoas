/*
 * Copyright 2017-2021 the original author or authors.
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

import java.util.Objects;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Greg Turnquist
 */
@JsonInclude(Include.NON_NULL)
final class CollectionJsonData {

	private @Nullable final String name;
	private @Nullable final Object value;
	private @Nullable final String prompt;

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

	public CollectionJsonData withName(@Nullable String name) {
		return this.name == name ? this : new CollectionJsonData(name, this.value, this.prompt);
	}

	public CollectionJsonData withValue(@Nullable Object value) {
		return this.value == value ? this : new CollectionJsonData(this.name, value, this.prompt);
	}

	public CollectionJsonData withPrompt(@Nullable String prompt) {
		return this.prompt == prompt ? this : new CollectionJsonData(this.name, this.value, prompt);
	}

	@JsonProperty
	@Nullable
	public String getName() {
		return this.name;
	}

	@JsonProperty
	@Nullable
	public Object getValue() {
		return this.value;
	}

	@JsonProperty
	@Nullable
	public String getPrompt() {
		return this.prompt;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CollectionJsonData that = (CollectionJsonData) o;
		return Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value)
				&& Objects.equals(this.prompt, that.prompt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.value, this.prompt);
	}

	@Override
	public String toString() {
		return "CollectionJsonData(name=" + this.name + ", value=" + this.value + ", prompt=" + this.prompt + ")";
	}
}
