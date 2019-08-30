/*
 * Copyright 2013-2014 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal;

import java.util.Collection;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;

/**
 * API to provide HAL curie information for links.
 *
 * @see {@link https://tools.ietf.org/html/draft-kelly-json-hal#section-8.2}
 * @author Oliver Gierke
 * @author Jeff Stano
 * @since 0.9
 */
public interface CurieProvider {

	CurieProvider NONE = new CurieProvider() {

		@Override
		public HalLinkRelation getNamespacedRelFrom(Link link) {
			throw new UnsupportedOperationException();
		}

		@Override
		public HalLinkRelation getNamespacedRelFor(LinkRelation rel) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Collection<?> getCurieInformation(Links links) {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * Returns the rel to be rendered for the given {@link Link}. Will potentially prefix the rel but also might decide
	 * not to, depending on the actual rel.
	 *
	 * @param link
	 * @return
	 */
	HalLinkRelation getNamespacedRelFrom(Link link);

	/**
	 * Returns the rel to be rendered for the given rel. Will potentially prefix the rel but also might decide not to,
	 * depending on the actual rel.
	 *
	 * @param rel
	 * @return
	 * @since 0.17
	 */
	HalLinkRelation getNamespacedRelFor(LinkRelation rel);

	/**
	 * Returns an object to render as the base curie information. Implementations have to make sure, the returned
	 * instances renders as defined in the spec.
	 *
	 * @param links the {@link Links} that have been added to the response so far.
	 * @return must not be {@literal null}.
	 */
	Collection<?> getCurieInformation(Links links);
}
