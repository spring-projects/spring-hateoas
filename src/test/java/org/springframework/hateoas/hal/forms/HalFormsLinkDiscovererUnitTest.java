/*
 * Copyright 2013-2019 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.support.MappingUtils.read;

import java.io.IOException;

import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.IanaLinkRelation;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.core.AbstractLinkDiscovererUnitTest;

/**
 * Unit tests for {@link HalFormsLinkDiscoverer}.
 * 
 * @author Greg Turnquist
 */
public class HalFormsLinkDiscovererUnitTest extends AbstractLinkDiscovererUnitTest {

	static final LinkDiscoverer discoverer = new HalFormsLinkDiscoverer();

	/**
	 * @see #314
	 */
	@Test
	public void discoversFullyQualifiedRel() {
		assertThat(getDiscoverer().findLinkWithRel("http://foo.com/bar", getInputString())).isNotNull();
	}

	/**
	 * @see #787
	 */
	@Test
	public void discoversAllTheLinkAttributes() throws IOException {

		String linkText = read(new ClassPathResource("hal-forms-link.json", getClass()));

		Link actual = getDiscoverer().findLinkWithRel(IanaLinkRelation.SELF.value(), linkText);

		Link expected = Link.valueOf("</customer/1>;" //
			+ "rel=\"self\";" //
			+ "hreflang=\"en\";" //
			+ "media=\"pdf\";" //
			+ "title=\"pdf customer copy\";" //
			+ "type=\"portable document\";" //
			+ "deprecation=\"http://example.com/customers/deprecated\";" //
			+ "profile=\"my-profile\"" //
			+ "name=\"my-name\"");

		assertThat(actual).isEqualTo(expected);
	}

	@Override
	protected LinkDiscoverer getDiscoverer() {
		return discoverer;
	}

	@Override
	protected String getInputString() {

		try {
			return read(new ClassPathResource("hal-forms-link-discoverer.json", getClass()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected String getInputStringWithoutLinkContainer() {
		return "{}";
	}
}
