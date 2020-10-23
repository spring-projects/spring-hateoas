/*
 * Copyright 2013-2020 the original author or authors.
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

import java.util.Optional;

import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.jayway.jsonpath.JsonPath;

/**
 * Helper class to find {@link Link} instances in representations.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @since 0.11
 */
class Rels {

	/**
	 * Creates a new {@link Rel} for the given relation name and {@link LinkDiscoverers}.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @param discoverers must not be {@literal null}.
	 * @return
	 */
	public static Rel getRelFor(String rel, LinkDiscoverers discoverers) {

		Assert.hasText(rel, "Relation name must not be null!");
		Assert.notNull(discoverers, "LinkDiscoverers must not be null!");

		if (rel.startsWith("$")) {
			return new JsonPathRel(rel);
		}

		return new LinkDiscovererRel(rel, discoverers);
	}

	public interface Rel {

		/**
		 * Returns the link contained in the given representation of the given {@link MediaType}.
		 *
		 * @param representation will never be {@literal null}.
		 * @param mediaType will never be {@literal null}.
		 * @return
		 */
		Optional<Link> findInResponse(String representation, MediaType mediaType);
	}

	/**
	 * {@link Rel} to using a {@link LinkDiscoverer} based on the given {@link MediaType}.
	 *
	 * @author Oliver Gierke
	 */
	private static class LinkDiscovererRel implements Rel {

		private final String rel;
		private final LinkDiscoverers discoverers;

		/**
		 * Creates a new {@link LinkDiscovererRel} for the given relation name and {@link LinkDiscoverers}.
		 *
		 * @param rel must not be {@literal null} or empty.
		 * @param discoverers must not be {@literal null}.
		 */
		private LinkDiscovererRel(String rel, LinkDiscoverers discoverers) {

			Assert.hasText(rel, "Rel must not be null or empty!");
			Assert.notNull(discoverers, "LinkDiscoverers must not be null!");

			this.rel = rel;
			this.discoverers = discoverers;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.client.Rels.Rel#findInResponse(java.lang.String, org.springframework.http.MediaType)
		 */
		@Override
		public Optional<Link> findInResponse(String response, MediaType mediaType) {

			return discoverers //
					.getRequiredLinkDiscovererFor(mediaType) //
					.findLinkWithRel(rel, response);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.rel;
		}

	}

	/**
	 * A relation that's being looked up by a JSONPath expression.
	 *
	 * @author Oliver Gierke
	 */
	private static class JsonPathRel implements Rel {

		private final String jsonPath;
		private final String rel;

		/**
		 * Creates a new {@link JsonPathRel} for the given JSON path.
		 *
		 * @param jsonPath must not be {@literal null} or empty.
		 */
		private JsonPathRel(String jsonPath) {

			Assert.hasText(jsonPath, "JSON path must not be null or empty!");

			this.jsonPath = jsonPath;

			String lastSegment = jsonPath.substring(jsonPath.lastIndexOf('.'));
			this.rel = lastSegment.contains("[") ? lastSegment.substring(0, lastSegment.indexOf("[")) : lastSegment;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.client.Rels.Rel#findInResponse(java.lang.String, org.springframework.http.MediaType)
		 */
		@Override
		public Optional<Link> findInResponse(@Nullable String representation, @Nullable MediaType mediaType) {
			return Optional.of(Link.of(JsonPath.read(representation, jsonPath).toString(), rel));
		}
	}
}
