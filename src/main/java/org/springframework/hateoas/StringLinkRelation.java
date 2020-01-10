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

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Simple value type for a {@link String} based {@link LinkRelation}.
 *
 * @author Oliver Drotbohm
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class StringLinkRelation implements LinkRelation, Serializable {

	private static final long serialVersionUID = -3904935345545567957L;
	private static final Map<String, StringLinkRelation> CACHE = new ConcurrentHashMap<>(256);

	@NonNull String relation;

	/**
	 * Returns a (potentially cached) {@link LinkRelation} for the given value.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @return
	 */
	@JsonCreator
	public static StringLinkRelation of(String relation) {

		Assert.hasText(relation, "Relation must not be null or empty!");

		return CACHE.computeIfAbsent(relation, StringLinkRelation::new);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkRelation#value()
	 */
	@JsonValue
	@Override
	public String value() {
		return relation;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return relation.toLowerCase(Locale.US).hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object o) {

		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		StringLinkRelation that = (StringLinkRelation) o;

		return this.relation.equalsIgnoreCase(that.relation);
	}
}
