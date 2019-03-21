/*
 * Copyright 2012-2019 the original author or authors.
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

import java.io.InputStream;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.plugin.core.Plugin;

/**
 * Interface to allow discovering links by relation type from some source.
 *
 * @author Oliver Gierke
 */
public interface LinkDiscoverer extends Plugin<MediaType> {

	/**
	 * Finds a single link with the given relation type in the given {@link String} representation.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null}.
	 * @return the first link with the given relation type found, or {@literal null} if none was found.
	 */
	default Optional<Link> findLinkWithRel(String rel, String representation) {
		return findLinkWithRel(LinkRelation.of(rel), representation);
	}

	Optional<Link> findLinkWithRel(LinkRelation rel, String representation);

	/**
	 * Finds a single link with the given relation type in the given {@link InputStream} representation.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null}.
	 * @return the first link with the given relation type found, or {@literal null} if none was found.
	 */
	default Optional<Link> findLinkWithRel(String rel, InputStream representation) {
		return findLinkWithRel(LinkRelation.of(rel), representation);
	}

	/**
	 * Finds a single link with the given {@link LinkRelation} in the given {@link InputStream} representation.
	 *
	 * @param rel must not be {@literal null}.
	 * @param representation must not be {@literal null}.
	 * @return the first link with the given relation type found, or {@literal null} if none was found.
	 */
	Optional<Link> findLinkWithRel(LinkRelation rel, InputStream representation);

	/**
	 * Returns all links with the given link relation found in the given {@link String} representation.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	default Links findLinksWithRel(String rel, String representation) {
		return findLinksWithRel(LinkRelation.of(rel), representation);
	}

	/**
	 * Returns all links with the given {@link LinkRelation} found in the given {@link String} representation.
	 *
	 * @param rel must not be {@literal null}.
	 * @param representation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	Links findLinksWithRel(LinkRelation rel, String representation);

	/**
	 * Returns all links with the given link relation found in the given {@link InputStream} representation.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	default Links findLinksWithRel(String rel, InputStream representation) {
		return findLinksWithRel(LinkRelation.of(rel), representation);
	}

	/**
	 * Returns all links with the given {@link LinkRelation} found in the given {@link InputStream} representation.
	 *
	 * @param rel must not be {@literal null}.
	 * @param representation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	Links findLinksWithRel(LinkRelation rel, InputStream representation);
}
