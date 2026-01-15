/*
 * Copyright 2013-2026 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.client.LinkDiscovererUnitTest;

/**
 * Unit tests for {@link HalLinkDiscoverer}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class HalLinkDiscovererUnitTest extends LinkDiscovererUnitTest {

	HalLinkDiscoverer discoverer = new HalLinkDiscoverer();
	ContextualMapper $ = MappingTestUtils.createMapper();

	/**
	 * @see #314
	 */
	@Test
	void discoversFullyQualifiedRel() {

		assertThat(getDiscoverer().findLinkWithRel("http://www.foo.com/bar", getInputString())) //
				.map(Link::getHref) //
				.hasValue("fullRelHref");
	}

	/**
	 * @see #787
	 */
	@Test
	void discoversAllTheLinkAttributes() throws IOException {

		$.assertFileContent("hal-link.json").satisfies(linkText -> {

			var expected = Link.valueOf("</customer/1>;" //
					+ "rel=\"self\";" //
					+ "hreflang=\"en\";" //
					+ "media=\"pdf\";" //
					+ "title=\"pdf customer copy\";" //
					+ "type=\"portable document\";" //
					+ "deprecation=\"https://example.com/customers/deprecated\";" //
					+ "profile=\"my-profile\";" //
					+ "name=\"my-name\"");

			assertThat(getDiscoverer().findLinkWithRel(IanaLinkRelations.SELF, linkText)).hasValue(expected);
		});
	}

	@Test // GH-2385
	void detectsEmbeddedLinks() {

		var discoverer = getDiscoverer().inspectEmbeddeds();

		$.assertFileContent("hal-link-discoverer.json").satisfies(it -> {

			assertThat(discoverer.findLinksWithRel("relation", it))
					.hasSize(3)
					.extracting(Link::getHref)
					.containsExactlyInAnyOrder("firstHref", "secondHref", "thirdHref");
		});
	}

	@Override
	protected HalLinkDiscoverer getDiscoverer() {
		return discoverer;
	}

	@Override
	protected String getInputString() {
		return $.readFileContent("hal-link-discoverer.json");
	}

	@Override
	protected String getInputStringWithoutLinkContainer() {
		return "{}";
	}
}
