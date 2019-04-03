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
package org.springframework.hateoas.mediatype;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.support.MappingUtils.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.client.LinkDiscoverer;

/**
 * Standard collection of {@link LinkDiscoverer} tests.
 *
 * TODO: Merge with {@link org.springframework.hateoas.client.LinkDiscovererUnitTest}.
 * 
 * @author Greg Turnquist
 */
public interface LinkDiscovererStandardUnitTest {

	String RELATION = "http://www.foo.com/bar";

	LinkRelation LINK_RELATION = LinkRelation.of(RELATION);

	/**
	 * Media type provider must provider an instance of their {@link LinkDiscoverer}.
	 */
	LinkDiscoverer getLinkDiscoverer();

	/**
	 * Media type provider must provide a JSON-based collection of links.
	 */
	default String getInputString() {

		try {
			return read(new ClassPathResource("standard-link-discoverer-unit-test.json", getClass()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	default void findLinkWithRelByString() {

		Optional<Link> optionalLink = getLinkDiscoverer().findLinkWithRel(RELATION, getInputString());

		assertThat(optionalLink).isNotEmpty();
		assertLink(optionalLink.orElseThrow(RuntimeException::new));
	}

	@Test
	default void findLinkWithRelByLinkRelation() {

		Optional<Link> optionalLink = getLinkDiscoverer().findLinkWithRel(LINK_RELATION, getInputString());

		assertThat(optionalLink).isNotEmpty();
		assertLink(optionalLink.orElseThrow(RuntimeException::new));
	}

	@Test
	default void findRequiredLinkWithRelByLinkRelation() {

		Link link = getLinkDiscoverer().findRequiredLinkWithRel(LINK_RELATION, getInputString());

		assertLink(link);
	}

	@Test
	default void findLinkWithRelByLinkRelationAndInputStream() {

		Optional<Link> optionalLink = getLinkDiscoverer().findLinkWithRel(LINK_RELATION,
				new ByteArrayInputStream(getInputString().getBytes()));

		assertThat(optionalLink).isNotEmpty();
		assertLink(optionalLink.orElseThrow(RuntimeException::new));
	}

	@Test
	default void findLinkWithRelByStringAndInputStream() {

		Optional<Link> optionalLink = getLinkDiscoverer().findLinkWithRel(RELATION,
				new ByteArrayInputStream(getInputString().getBytes()));

		assertThat(optionalLink).isNotEmpty();
		assertLink(optionalLink.orElseThrow(RuntimeException::new));
	}

	@Test
	default void findRequiredLinkWithRelByLinkRelationAndInputStream() {

		Link link = getLinkDiscoverer().findRequiredLinkWithRel(LINK_RELATION,
				new ByteArrayInputStream(getInputString().getBytes()));

		assertLink(link);
	}

	@Test
	default void findLinksWithRelByString() {

		Links links = getLinkDiscoverer().findLinksWithRel(RELATION, getInputString());

		assertLinks(links);
	}

	@Test
	default void findLinksWithRelByLinkRelation() {

		Links links = getLinkDiscoverer().findLinksWithRel(LINK_RELATION, getInputString());

		assertLinks(links);
	}

	@Test
	default void findLinksWithRelByStringAndInputStream() {

		Links links = getLinkDiscoverer().findLinksWithRel(RELATION, new ByteArrayInputStream(getInputString().getBytes()));

		assertLinks(links);
	}

	@Test
	default void findLinksWithRelByLinkRelationAndInputStream() {

		Links links = getLinkDiscoverer().findLinksWithRel(LINK_RELATION, new ByteArrayInputStream(getInputString().getBytes()));

		assertLinks(links);
	}

	/**
	 * Verify the details of a standard set of {@link Link}s.
	 *
	 * @param link
	 */
	static void assertLink(Link link) {

		assertThat(link.getRel()).isEqualTo(LINK_RELATION);
		assertThat(link.getHref()).isEqualTo("https://www.foo.com/bar/whatever");
	}

	/**
	 * Verify the details of a standard combined set of {@link Links}.
	 *
	 * @param links
	 */
	static void assertLinks(Links links) {

		assertThat(links).hasSize(1);
		assertThat(links).extracting(Link::getRel).containsExactly(LINK_RELATION);
		assertThat(links).extracting(Link::getHref).containsExactly("https://www.foo.com/bar/whatever");
	}

}
