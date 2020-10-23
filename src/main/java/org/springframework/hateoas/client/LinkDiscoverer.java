/*
 * Copyright 2012-2020 the original author or authors.
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

import java.io.InputStream;
import java.util.Optional;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.Plugin;

/**
 * Interface to allow discovering links by relation type from some source.
 *
 * @author Oliver Gierke
 */
public interface LinkDiscoverer extends Plugin<MediaType> {

	/**
	 * Finds a single link with the given {@link LinkRelation} in the given {@link String} representation.
	 *
	 * @param rel must not be {@literal null}.
	 * @param representation must not be {@literal null}.
	 * @return the first link with the given relation type found, or {@link Optional#empty()} if none was found.
	 */
	Optional<Link> findLinkWithRel(LinkRelation rel, String representation);

	/**
	 * Finds a single link with the given relation type in the given {@link String} representation.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null}.
	 * @return the first {@link Link} with the given link relation found, or {@link Optional#empty()} if none was found.
	 */
	default Optional<Link> findLinkWithRel(String rel, String representation) {
		return findLinkWithRel(LinkRelation.of(rel), representation);
	}

	/**
	 * Finds a single link with the given relation in the given {@link String} representation.
	 *
	 * @param relation must not be {@literal null}.
	 * @param representation must not be {@literal null}.
	 * @return the first link with the given relation type found.
	 * @throws IllegalArgumentException if no {@link Link} for the given {@link LinkRelation} can be found.
	 */
	default Link findRequiredLinkWithRel(LinkRelation relation, String representation) {

		return findLinkWithRel(relation, representation).orElseThrow(
				() -> new IllegalArgumentException(String.format("Did not find link with relation '%s'!", relation.value())));
	}

	/**
	 * Finds a single link with the given {@link LinkRelation} in the given {@link InputStream} representation.
	 *
	 * @param relation must not be {@literal null}.
	 * @param representation must not be {@literal null}.
	 * @return the first {@link Link} with the given {@link LinkRelation} found, or {@link Optional#empty()} if none was
	 *         found.
	 */
	Optional<Link> findLinkWithRel(LinkRelation relation, InputStream representation);

	/**
	 * Finds a single link with the given relation type in the given {@link InputStream} representation.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @param representation must not be {@literal null}.
	 * @return the first link with the given relation type found, or {@link Optional#empty()} if none was found.
	 */
	default Optional<Link> findLinkWithRel(String relation, InputStream representation) {
		return findLinkWithRel(LinkRelation.of(relation), representation);
	}

	/**
	 * Finds a single link with the given relation type in the given {@link InputStream} representation.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @param representation must not be {@literal null}.
	 * @return the first link with the given relation type found.
	 * @throws IllegalArgumentException if no {@link Link} for the given {@link LinkRelation} can be found.
	 */
	default Link findRequiredLinkWithRel(LinkRelation relation, InputStream representation) {

		return findLinkWithRel(relation, representation).orElseThrow(
				() -> new IllegalArgumentException(String.format("Did not find link with relation '%s'!", relation.value())));
	}

	/**
	 * Returns all links with the given link relation found in the given {@link String} representation.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @param representation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	default Links findLinksWithRel(String relation, String representation) {
		return findLinksWithRel(LinkRelation.of(relation), representation);
	}

	/**
	 * Returns all links with the given {@link LinkRelation} found in the given {@link String} representation.
	 *
	 * @param relation must not be {@literal null}.
	 * @param representation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	Links findLinksWithRel(LinkRelation relation, String representation);

	/**
	 * Returns all links with the given link relation found in the given {@link InputStream} representation.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @param representation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	default Links findLinksWithRel(String relation, InputStream representation) {
		return findLinksWithRel(LinkRelation.of(relation), representation);
	}

	/**
	 * Returns all links with the given {@link LinkRelation} found in the given {@link InputStream} representation.
	 *
	 * @param relation must not be {@literal null}.
	 * @param representation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	Links findLinksWithRel(LinkRelation relation, InputStream representation);
}
