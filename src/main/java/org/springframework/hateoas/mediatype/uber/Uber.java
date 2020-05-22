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
package org.springframework.hateoas.mediatype.uber;

import java.util.List;
import java.util.Objects;

import org.springframework.hateoas.Links;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enclosing collection in an {@literal UBER+JSON} representation.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
@JsonInclude(Include.NON_NULL)
final class Uber {

	private final String version;
	private final List<UberData> data;
	private final UberError error;

	@JsonCreator
	Uber(@JsonProperty("version") String version, @JsonProperty("data") @Nullable List<UberData> data,
			@JsonProperty("error") @Nullable UberError error) {

		this.version = version;
		this.data = data;
		this.error = error;
	}

	Uber() {
		this("1.0", null, null);
	}

	/**
	 * Create a new {@link Uber} by copying attributes and replacing the {@literal version}.
	 *
	 * @param version
	 * @return
	 */
	Uber withVersion(String version) {
		return this.version == version ? this : new Uber(version, this.data, this.error);
	}

	/**
	 * Create a new {@link Uber} by copying attributes and replacing the {@literal data}.
	 *
	 * @param data
	 * @return
	 */
	Uber withData(List<UberData> data) {
		return this.data == data ? this : new Uber(this.version, data, this.error);
	}

	/**
	 * Create a new {@link Uber} by copying attributes and replacing the {@literal error}.
	 *
	 * @param error
	 * @return
	 */
	Uber withError(UberError error) {
		return this.error == error ? this : new Uber(this.version, this.data, error);
	}

	/**
	 * Extract rel and url from every {@link UberData} entry.
	 *
	 * @return
	 */
	@JsonIgnore
	Links getLinks() {

		if (data == null) {
			return Links.NONE;
		}

		return data.stream() //
				.flatMap(uberData -> uberData.getLinks().stream()) //
				.collect(Links.collector());
	}

	@JsonProperty
	public String getVersion() {
		return this.version;
	}

	@JsonProperty
	public List<UberData> getData() {
		return this.data;
	}

	@JsonProperty
	public UberError getError() {
		return this.error;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (!(o instanceof Uber))
			return false;
		Uber uber = (Uber) o;
		return Objects.equals(this.version, uber.version) && Objects.equals(this.data, uber.data)
				&& Objects.equals(this.error, uber.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.version, this.data, this.error);
	}
}
