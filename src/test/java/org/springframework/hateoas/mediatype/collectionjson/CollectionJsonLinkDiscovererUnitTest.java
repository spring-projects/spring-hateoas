/*
 * Copyright 2018-2019 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.support.MappingUtils;

/**
 * Unit tests for {@link CollectionJsonLinkDiscoverer}.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
class CollectionJsonLinkDiscovererUnitTest {

	LinkDiscoverer discoverer;

	@BeforeEach
	void setUp() {
		this.discoverer = new CollectionJsonLinkDiscoverer();
	}

	@Test
	void spec1Links() throws IOException {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part1.json", getClass()));

		Optional<Link> link = this.discoverer.findLinkWithRel("self", specBasedJson);

		assertThat(link) //
				.map(Link::getHref) //
				.hasValue("https://example.org/friends/");
	}

	@Test
	void spec2Links() throws IOException {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part2.json", getClass()));

		assertThat(this.discoverer.findLinkWithRel("self", specBasedJson)) //
				.map(Link::getHref) //
				.hasValue("https://example.org/friends/");

		assertThat(this.discoverer.findLinkWithRel("feed", specBasedJson)) //
				.map(Link::getHref) //
				.hasValue("https://example.org/friends/rss");

		assertThat(this.discoverer.findLinksWithRel("blog", specBasedJson)) //
				.extracting("href") //
				.containsExactlyInAnyOrder("https://examples.org/blogs/jdoe", "https://examples.org/blogs/msmith",
						"https://examples.org/blogs/rwilliams");

		assertThat(this.discoverer.findLinksWithRel("avatar", specBasedJson)) //
				.extracting("href") //
				.containsExactlyInAnyOrder("https://examples.org/images/jdoe", "https://examples.org/images/msmith",
						"https://examples.org/images/rwilliams");
	}
}
