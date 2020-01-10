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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;

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
@Value
@Getter(onMethod = @__(@JsonProperty))
@Wither(AccessLevel.PACKAGE)
@JsonInclude(Include.NON_NULL)
class Uber {

	private String version;
	private List<UberData> data;
	private UberError error;

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
}
