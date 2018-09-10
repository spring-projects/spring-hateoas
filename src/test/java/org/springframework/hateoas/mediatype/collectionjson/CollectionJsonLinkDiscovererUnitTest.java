/*
 * Copyright 2018 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.support.MappingUtils;

/**
 * Unit tests for {@link CollectionJsonLinkDiscoverer}.
 *
 * @author Greg Turnquist
 */
public class CollectionJsonLinkDiscovererUnitTest {

	LinkDiscoverer discoverer;

	@Before
	public void setUp() {
		this.discoverer = new CollectionJsonLinkDiscoverer();
	}

	@Test
	public void spec1Links() throws IOException {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part1.json", getClass()));

		Link link = this.discoverer.findLinkWithRel("self", specBasedJson);

		assertThat(link).isNotNull();
		assertThat(link.getHref()).isEqualTo("http://example.org/friends/");
	}

	@Test
	public void spec2Links() throws IOException {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part2.json", getClass()));

		Link selfLink = this.discoverer.findLinkWithRel("self", specBasedJson);

		assertThat(selfLink).isNotNull();
		assertThat(selfLink.getHref()).isEqualTo("http://example.org/friends/");

		Link feedLink = this.discoverer.findLinkWithRel("feed", specBasedJson);

		assertThat(feedLink).isNotNull();
		assertThat(feedLink.getHref()).isEqualTo("http://example.org/friends/rss");

		List<Link> links = this.discoverer.findLinksWithRel("blog", specBasedJson);

		assertThat(links)
			.extracting("href")
			.containsExactlyInAnyOrder(
				"http://examples.org/blogs/jdoe",
				"http://examples.org/blogs/msmith",
				"http://examples.org/blogs/rwilliams");

		links = this.discoverer.findLinksWithRel("avatar", specBasedJson);

		assertThat(links)
			.extracting("href")
			.containsExactlyInAnyOrder(
				"http://examples.org/images/jdoe",
				"http://examples.org/images/msmith",
				"http://examples.org/images/rwilliams");
	}
}
