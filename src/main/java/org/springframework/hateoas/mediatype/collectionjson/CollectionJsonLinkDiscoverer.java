/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.mediatype.collectionjson;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.mediatype.MediaTypes;
import org.springframework.hateoas.core.JsonPathLinkDiscoverer;

/**
 * {@link LinkDiscoverer} implementation based on JSON Collection link structure.
 *
 * NOTE: Since links can appear in two different places in a Collection+JSON document, this discoverer
 * uses two.
 *
 * @author Greg Turnquist
 */
public class CollectionJsonLinkDiscoverer extends JsonPathLinkDiscoverer {

	private final CollectionJsonSelfLinkDiscoverer selfLinkDiscoverer;

	public CollectionJsonLinkDiscoverer() {
		super("$.collection..links..[?(@.rel == '%s')].href", MediaTypes.COLLECTION_JSON);
		this.selfLinkDiscoverer = new CollectionJsonSelfLinkDiscoverer();
	}

	@Override
	public Link findLinkWithRel(String rel, String representation) {

		if (rel.equals(Link.REL_SELF)) {
			return findSelfLink(representation);
		} else {
			return super.findLinkWithRel(rel, representation);
		}
	}

	@Override
	public Link findLinkWithRel(String rel, InputStream representation) {

		if (rel.equals(Link.REL_SELF)) {
			return findSelfLink(representation);
		} else {
			return super.findLinkWithRel(rel, representation);
		}
	}

	@Override
	public List<Link> findLinksWithRel(String rel, String representation) {

		if (rel.equals(Link.REL_SELF)) {
			return addSelfLink(super.findLinksWithRel(rel, representation), representation);
		} else {
			return super.findLinksWithRel(rel, representation);
		}
	}

	@Override
	public List<Link> findLinksWithRel(String rel, InputStream representation) {

		if (rel.equals(Link.REL_SELF)) {
			return addSelfLink(super.findLinksWithRel(rel, representation), representation);
		} else {
			return super.findLinksWithRel(rel, representation);
		}
	}

	//
	// Internal methods to support discovering the "self" link found at "$.collection.href".
	//

	private Link findSelfLink(String representation) {
		return this.selfLinkDiscoverer.findLinkWithRel(Link.REL_SELF, representation);
	}

	private Link findSelfLink(InputStream representation) {
		return this.selfLinkDiscoverer.findLinkWithRel(Link.REL_SELF, representation);
	}

	private List<Link> addSelfLink(List<Link> links, String representation) {

		return Stream.concat(
			Stream.of(findSelfLink(representation)),
			links.stream()
		)
		.collect(Collectors.toList());
	}

	private List<Link> addSelfLink(List<Link> links, InputStream representation) {

		return Stream.concat(
			Stream.of(findSelfLink(representation)),
			links.stream()
		)
		.collect(Collectors.toList());
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
