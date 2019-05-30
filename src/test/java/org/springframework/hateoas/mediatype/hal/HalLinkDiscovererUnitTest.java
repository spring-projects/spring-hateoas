/*
 * Copyright 2013-2019 the original author or authors.
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
import static org.springframework.hateoas.support.MappingUtils.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscovererUnitTest;

/**
 * Unit tests for {@link HalLinkDiscoverer}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class HalLinkDiscovererUnitTest extends LinkDiscovererUnitTest {

	static final LinkDiscoverer discoverer = new HalLinkDiscoverer();

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

		String linkText = read(new ClassPathResource("hal-link.json", getClass()));

		Link expected = Link.valueOf("</customer/1>;" //
				+ "rel=\"self\";" //
				+ "hreflang=\"en\";" //
				+ "media=\"pdf\";" //
				+ "title=\"pdf customer copy\";" //
				+ "type=\"portable document\";" //
				+ "deprecation=\"https://example.com/customers/deprecated\";" //
				+ "profile=\"my-profile\"" //
				+ "name=\"my-name\"");

		assertThat(getDiscoverer().findLinkWithRel(IanaLinkRelations.SELF.value(), linkText)) //
				.hasValue(expected);
	}

	@Override
	protected LinkDiscoverer getDiscoverer() {
		return discoverer;
	}

	@Override
	protected String getInputString() {

		try {
			return read(new ClassPathResource("hal-link-discoverer.json", getClass()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected String getInputStringWithoutLinkContainer() {
		return "{}";
	}
}
