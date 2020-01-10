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
package org.springframework.hateoas.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

/**
 * Container for customizations to a single traverson "hop"
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 * @author Manish Misra
 * @since 0.18
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter(AccessLevel.PACKAGE)
public class Hop {

	/**
	 * Name of this hop.
	 */
	private final String rel;

	/**
	 * Collection of URI Template parameters.
	 */
	private final @Wither Map<String, Object> parameters;

	/**
	 * Extra {@link HttpHeaders} to apply to this hop.
	 */
	private final @Wither HttpHeaders headers;

	/**
	 * Creates a new {@link Hop} for the given relation name.
	 * 
	 * @param rel must not be {@literal null} or empty.
	 * @return
	 */
	public static Hop rel(String rel) {

		Assert.hasText(rel, "Relation must not be null or empty!");

		return new Hop(rel, Collections.emptyMap(), HttpHeaders.EMPTY);
	}

	/**
	 * Add one parameter to the map of parameters.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @param value can be {@literal null}.
	 * @return
	 */
	public Hop withParameter(String name, Object value) {

		Assert.hasText(name, "Name must not be null or empty!");

		HashMap<String, Object> parameters = new HashMap<>(this.parameters);
		parameters.put(name, value);

		return new Hop(this.rel, parameters, this.headers);
	}

	/**
	 * Add one header to the HttpHeaders collection.
	 *
	 * @param headerName must not be {@literal null} or empty.
	 * @param headerValue can be {@literal null}.
	 * @return
	 */
	public Hop header(String headerName, String headerValue) {

		Assert.hasText(headerName, "headerName must not be null or empty!");

		if (this.headers == HttpHeaders.EMPTY) {

			HttpHeaders newHeaders = new HttpHeaders();
			newHeaders.add(headerName, headerValue);

			return new Hop(this.rel, this.parameters, newHeaders);
		}

		this.headers.add(headerName, headerValue);
		return this;
	}

	/**
	 * Returns whether the {@link Hop} has parameters declared.
	 * 
	 * @return
	 */
	boolean hasParameters() {
		return !this.parameters.isEmpty();
	}

	/**
	 * Create a new {@link Map} starting with the supplied template parameters. Then add the ones for this hop. This
	 * allows a local hop to override global parameters.
	 *
	 * @param globalParameters must not be {@literal null}.
	 * @return a merged map of URI Template parameters, will never be {@literal null}.
	 */
	Map<String, Object> getMergedParameters(Map<String, Object> globalParameters) {

		Assert.notNull(globalParameters, "Global parameters must not be null!");

		Map<String, Object> mergedParameters = new HashMap<>();

		mergedParameters.putAll(globalParameters);
		mergedParameters.putAll(this.parameters);

		return mergedParameters;
	}
}
