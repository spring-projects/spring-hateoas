/*
 * Copyright 2015-2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import java.io.InputStream;
import java.util.Optional;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.JsonPathLinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.util.Assert;

/**
 * {@link LinkDiscoverer} implementation based on JSON Collection link structure. NOTE: Since links can appear in two
 * different places in a Collection+JSON document, this discoverer uses two.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class CollectionJsonLinkDiscoverer extends JsonPathLinkDiscoverer {

	private final CollectionJsonSelfLinkDiscoverer selfLinkDiscoverer;

	public CollectionJsonLinkDiscoverer() {

		super("$.collection..links..[?(@.rel == '%s')].href", MediaTypes.COLLECTION_JSON);

		this.selfLinkDiscoverer = new CollectionJsonSelfLinkDiscoverer();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.JsonPathLinkDiscoverer#findLinkWithRel(org.springframework.hateoas.LinkRelation, java.lang.String)
	 */
	@Override
	public Optional<Link> findLinkWithRel(LinkRelation relation, String representation) {

		Assert.notNull(relation, "LinkRelation must not be null!");
		Assert.notNull(representation, "Representation must not be null!");

		return relation.isSameAs(IanaLinkRelations.SELF) //
				? findSelfLink(representation) //
				: super.findLinkWithRel(relation, representation);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkDiscoverer#findLinkWithRel(java.lang.String, java.io.InputStream)
	 */
	@Override
	public Optional<Link> findLinkWithRel(LinkRelation relation, InputStream representation) {

		Assert.notNull(relation, "LinkRelation must not be null!");
		Assert.notNull(representation, "InputStream must not be null!");

		return relation.isSameAs(IanaLinkRelations.SELF) //
				? findSelfLink(representation) //
				: super.findLinkWithRel(relation, representation);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.JsonPathLinkDiscoverer#findLinksWithRel(org.springframework.hateoas.LinkRelation, java.lang.String)
	 */
	@Override
	public Links findLinksWithRel(LinkRelation relation, String representation) {

		Assert.notNull(relation, "LinkRelation must not be null!");
		Assert.notNull(representation, "Representation must not be null!");

		return relation.isSameAs(IanaLinkRelations.SELF) //
				? addSelfLink(super.findLinksWithRel(relation, representation), representation) //
				: super.findLinksWithRel(relation, representation);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.JsonPathLinkDiscoverer#findLinksWithRel(org.springframework.hateoas.LinkRelation, java.io.InputStream)
	 */
	@Override
	public Links findLinksWithRel(LinkRelation relation, InputStream representation) {

		return relation.isSameAs(IanaLinkRelations.SELF) //
				? addSelfLink(super.findLinksWithRel(relation, representation), representation) //
				: super.findLinksWithRel(relation, representation);

	}

	//
	// Internal methods to support discovering the "self" link found at "$.collection.href".
	//

	private Optional<Link> findSelfLink(String representation) {
		return this.selfLinkDiscoverer.findLinkWithRel(IanaLinkRelations.SELF, representation);
	}

	private Optional<Link> findSelfLink(InputStream representation) {
		return this.selfLinkDiscoverer.findLinkWithRel(IanaLinkRelations.SELF, representation);
	}

	private Links addSelfLink(Links links, String representation) {

		return findSelfLink(representation) //
				.map(Links::of) //
				.map(it -> it.and(links)) //
				.orElse(links);
	}

	private Links addSelfLink(Links links, InputStream representation) {

		return findSelfLink(representation) //
				.map(Links::of) //
				.map(it -> it.and(links)) //
				.orElse(links);
	}

	/**
	 * {@link JsonPathLinkDiscoverer} that looks for the non-parameterized {@literal collection.href} link.
	 */
	private static class CollectionJsonSelfLinkDiscoverer extends JsonPathLinkDiscoverer {

		CollectionJsonSelfLinkDiscoverer() {
			super("$.collection.href", MediaTypes.COLLECTION_JSON);
		}
	}
}
