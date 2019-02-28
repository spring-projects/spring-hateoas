/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.client;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.PluginRegistry;

/**
 * @author Oliver Gierke
 */
@RequiredArgsConstructor
public class LinkDiscovery {

	private final PluginRegistry<LinkDiscoverer, MediaType> discoverers;

	Discoverer forMediaType(MediaType mediaType) {
		return new Discoverer(discoverers.getPluginFor(mediaType));
	}

	@RequiredArgsConstructor
	static class Discoverer {

		private final Optional<LinkDiscoverer> discoverer;

		public WithRelation findRelation(LinkRelation relation) {
			return new WithRelation(discoverer, relation);
		}
	}

	@RequiredArgsConstructor
	static class WithRelation {

		private final Optional<LinkDiscoverer> discoverer;
		private final LinkRelation relation;

		public WithRepresentation in(String representation) {
			return new WithRepresentation(this, representation);
		}
	}

	interface MultiLookup {
		Links all();
	}

	interface SingleLookup {
		Optional<Link> first();
	}

	interface RequiredLookup {
		Link first();

		Link any();
	}

	@RequiredArgsConstructor
	static class WithRepresentation {

		private final WithRelation withRelation;
		private final String representation;

		private Links lookup() {
			return withRelation.discoverer.map(it -> it.findLinksWithRel(withRelation.relation, representation))
					.orElse(Links.NONE);
		}

		public Optional<Link> any() {
			return Optional.empty();
		}

		public Optional<Link> first() {
			return Optional.empty();
		}

		public Links all() {
			return Links.NONE;
		}
	}
}
