/*
 * Copyright 2017-2024 the original author or authors.
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

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

/**
 * Hold the {@link AffordanceModel}s for all supported media types.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public final class Affordance implements Iterable<AffordanceModel> {

	/**
	 * Collection of {@link AffordanceModel}s related to this affordance.
	 */
	private final Map<MediaType, AffordanceModel> models;

	public Affordance(Map<MediaType, AffordanceModel> models) {
		this.models = models;
	}

	/**
	 * Look up the {@link AffordanceModel} for the requested {@link MediaType}.
	 *
	 * @param mediaType must not be {@literal null}.
	 * @return can be {@literal null}.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends AffordanceModel> T getAffordanceModel(MediaType mediaType) {
		return (T) this.models.get(mediaType);
	}

	/**
	 * Look up the {@link AffordanceModel} for the requested {@link MediaType}.
	 *
	 * @param mediaType must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 3.0
	 */
	@SuppressWarnings("unchecked")
	public <T extends AffordanceModel> T getRequiredAffordanceModel(MediaType mediaType) {

		var result = getAffordanceModel(mediaType);

		Assert.notNull(result, () -> "No affordance model found for %s!".formatted(mediaType));

		return (T) result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<AffordanceModel> iterator() {
		return this.models.values().iterator();
	}

	Map<MediaType, AffordanceModel> getModels() {
		return this.models;
	}

	@Override
	public boolean equals(@Nullable Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Affordance that = (Affordance) o;
		return Objects.equals(this.models, that.models);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.models);
	}

	@Override
	public String toString() {
		return "Affordance(models=" + this.models + ")";
	}
}
