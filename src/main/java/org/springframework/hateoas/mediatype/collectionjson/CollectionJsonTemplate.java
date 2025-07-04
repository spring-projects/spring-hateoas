/*
 * Copyright 2018-2024 the original author or authors.
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

import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Greg Turnquist
 */
@NullUnmarked
final class CollectionJsonTemplate {

	private final List<CollectionJsonData> data;

	@JsonCreator
	CollectionJsonTemplate(@JsonProperty("data") @Nullable List<CollectionJsonData> data) {
		this.data = data;
	}

	CollectionJsonTemplate() {
		this(null);
	}

	public CollectionJsonTemplate withData(List<CollectionJsonData> data) {
		return this.data == data ? this : new CollectionJsonTemplate(data);
	}

	public List<CollectionJsonData> getData() {
		return this.data;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CollectionJsonTemplate that = (CollectionJsonTemplate) obj;
		return Objects.equals(this.data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.data);
	}

	@Override
	public String toString() {
		return "CollectionJsonTemplate(data=" + this.data + ")";
	}
}
