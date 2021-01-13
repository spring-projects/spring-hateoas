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
package org.springframework.hateoas.mediatype.uber;

import java.util.List;
import java.util.Objects;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Top-level element in an {@literal UBER+JSON} representation.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
final class UberDocument {

	private final Uber uber;

	@JsonCreator
	UberDocument(@JsonProperty("version") String version, @JsonProperty("data") @Nullable List<UberData> data,
			@JsonProperty("error") @Nullable UberError error) {
		this.uber = new Uber(version, data, error);
	}

	UberDocument() {
		this("1.0", null, null);
	}

	UberDocument(Uber uber) {
		this.uber = uber;
	}

	UberDocument withUber(Uber uber) {
		return this.uber == uber ? this : new UberDocument(uber);
	}

	@JsonProperty
	public Uber getUber() {
		return this.uber;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (!(o instanceof UberDocument))
			return false;
		UberDocument that = (UberDocument) o;
		return Objects.equals(this.uber, that.uber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uber);
	}

	public String toString() {
		return "UberDocument(uber=" + this.uber + ")";
	}
}
