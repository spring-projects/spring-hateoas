/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.hateoas;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Interface for defining link relations. Can be used for implementing spec-based link relations as well as custom ones.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 * @since 1.0
 */
public interface LinkRelation {

	/**
	 * Return the link relation's value.
	 */
	@JsonValue
	String value();

	/**
	 * Creates a new {@link LinkRelation}.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @return
	 */
	@JsonCreator
	static LinkRelation of(String relation) {
		return StringLinkRelation.of(relation);
	}

	/**
	 * Creates a new {@link Iterable} of {@link LinkRelation} for each of the given {@link String}s.
	 *
	 * @param others must not be {@literal null}.
	 * @return
	 */
	static Iterable<LinkRelation> manyOf(String... others) {

		return Arrays.stream(others) //
				.map(LinkRelation::of) //
				.collect(Collectors.toList());
	}

	/**
	 * Returns whether the given {@link LinkRelation} is logically the same as the current one, independent of
	 * implementation, i.e. whether the plain {@link String} values match.
	 *
	 * @param relation must not be {@literal null}.
	 * @return
	 */
	default boolean isSameAs(LinkRelation relation) {

		Assert.notNull(relation, "LinkRelation must not be null!");

		return this.value().equalsIgnoreCase(relation.value());
	}
}
