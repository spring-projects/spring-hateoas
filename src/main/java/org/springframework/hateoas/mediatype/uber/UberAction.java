/*
 * Copyright 2014-2019 the original author or authors.
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

import java.util.Arrays;

import org.springframework.hateoas.mediatype.uber.Jackson2UberModule.UberActionDeserializer;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Embodies possible actions for an {@literal UBER+JSON} representation, mapped onto {@link HttpMethod}s.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @since 1.0
 */
@JsonDeserialize(using = UberActionDeserializer.class)
enum UberAction {

	/**
	 * POST
	 */
	APPEND(HttpMethod.POST),

	/**
	 * PATCH
	 */
	PARTIAL(HttpMethod.PATCH),

	/**
	 * GET
	 */
	READ(HttpMethod.GET),

	/**
	 * DELETE
	 */
	REMOVE(HttpMethod.DELETE),

	/**
	 * PUT
	 */
	REPLACE(HttpMethod.PUT);

	private final HttpMethod httpMethod;

	UberAction(HttpMethod method) {
		this.httpMethod = method;
	}

	/**
	 * Look up the related Spring Web {@link HttpMethod}.
	 *
	 * @return
	 */
	HttpMethod getMethod() {
		return this.httpMethod;
	}

	@JsonValue
	@Override
	public String toString() {
		return this.name().toLowerCase();
	}

	/**
	 * Convert an {@link HttpMethod} into an {@link UberAction}.
	 *
	 * @param method
	 * @return
	 */
	static UberAction fromMethod(HttpMethod method) {

		return Arrays.stream(UberAction.values()) //
				.filter(action -> action.httpMethod == method) //
				.findFirst() //
				.orElseThrow(() -> new IllegalArgumentException("Unsupported method: " + method));
	}

	/**
	 * Maps given request method to uber action. GET will be mapped as {@literal null} since it is the default.
	 *
	 * @param method to map
	 * @return action, or null for GET
	 */
	@Nullable
	static UberAction forRequestMethod(HttpMethod method) {
		return HttpMethod.GET == method ? null : fromMethod(method);
	}
}
