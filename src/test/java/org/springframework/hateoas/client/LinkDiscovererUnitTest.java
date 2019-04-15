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
package org.springframework.hateoas.client;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;

/**
 * Base class for unit tests for {@link LinkDiscoverer} implementations.
 *
 * @author Oliver Gierke
 */
public abstract class LinkDiscovererUnitTest {

	@Test
	void findsSingleLink() {

		assertThat(getDiscoverer().findLinkWithRel("self", getInputString())) //
				.hasValue(new Link("selfHref"));

		Links links = getDiscoverer().findLinksWithRel("self", getInputString());

		assertThat(links).hasSize(1);
		assertThat(links).contains(new Link("selfHref"));
	}

	@Test
	void findsFirstLink() {

		assertThat(getDiscoverer().findLinkWithRel("relation", getInputString()))
				.hasValue(new Link("firstHref", "relation"));
	}

	@Test
	void findsAllLinks() {

		Links links = getDiscoverer().findLinksWithRel("relation", getInputString());

		assertThat(links).hasSize(2);
		assertThat(links).contains(new Link("firstHref", "relation"), new Link("secondHref", "relation"));
	}

	@Test
	void returnsForInexistingLink() {
		assertThat(getDiscoverer().findLinkWithRel("something", getInputString())).isEmpty();
	}

	@Test
	void returnsForInexistingLinkFromInputStream() throws Exception {

		InputStream inputStream = new ByteArrayInputStream(getInputString().getBytes("UTF-8"));
		assertThat(getDiscoverer().findLinkWithRel("something", inputStream)).isEmpty();
	}

	@Test
	void returnsNullForNonExistingLinkContainer() {

		assertThat(getDiscoverer().findLinksWithRel("something", getInputStringWithoutLinkContainer())).isEmpty();
		assertThat(getDiscoverer().findLinkWithRel("something", getInputStringWithoutLinkContainer())).isEmpty();
	}

	@Test // #840
	void throwsExceptionForRequiredLinkNotFoundInString() {

		LinkDiscoverer discoverer = getDiscoverer();

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> discoverer.findRequiredLinkWithRel(LinkRelation.of("something-obscure"), getInputString()));
	}

	@Test // #840
	void throwsExceptionForRequiredLinkNotFoundInInputStream() throws IOException {

		LinkDiscoverer discoverer = getDiscoverer();

		try (InputStream stream = new ByteArrayInputStream(getInputString().getBytes(StandardCharsets.UTF_8))) {

			assertThatExceptionOfType(IllegalArgumentException.class) //
					.isThrownBy(() -> discoverer.findRequiredLinkWithRel(LinkRelation.of("something-obscure"), stream));
		}
	}

	/**
	 * Return the {@link LinkDiscoverer} to be tested.
	 *
	 * @return
	 */
	protected abstract LinkDiscoverer getDiscoverer();

	/**
	 * Return the JSON structure we expect to find the links in.
	 *
	 * @return
	 */
	protected abstract String getInputString();

	protected abstract String getInputStringWithoutLinkContainer();
}
