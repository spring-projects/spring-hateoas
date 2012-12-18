/*
 * Copyright 2012 the original author or authors.
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
package org.springframework.hateoas.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;

/**
 * Unit tests for {@link DefaultLinkDiscoverer}.
 * 
 * @author Oliver Gierke
 */
public class DefaultLinkDiscovererUnitTest {

	static final String SAMPLE = "{ links : [ " + //
			"{ rel : 'self', href : 'selfHref' }, " + //
			"{ rel : 'relation', href : 'firstHref' }, " + //
			"{ rel : 'relation', href : 'secondHref' }]}";

	static final LinkDiscoverer discoverer = new DefaultLinkDiscoverer();

	@Test
	public void findsSingleLink() {

		assertThat(discoverer.findLinkWithRel("self", SAMPLE), is(new Link("selfHref")));

		List<Link> links = discoverer.findLinksWithRel("self", SAMPLE);
		assertThat(links, hasSize(1));
		assertThat(links, hasItem(new Link("selfHref")));
	}

	@Test
	public void findsFirstLink() {

		assertThat(discoverer.findLinkWithRel("relation", SAMPLE), is(new Link("firstHref", "relation")));
	}

	@Test
	public void findsAllLinks() {

		List<Link> links = discoverer.findLinksWithRel("relation", SAMPLE);
		assertThat(links, hasSize(2));
		assertThat(links, hasItems(new Link("firstHref", "relation"), new Link("secondHref", "relation")));
	}

	@Test
	public void returnsForInexistingLink() {
		assertThat(discoverer.findLinkWithRel("something", SAMPLE), is(nullValue()));
	}

	@Test
	public void returnsForInxistingLinkFromInputStream() throws Exception {

		InputStream inputStream = new ByteArrayInputStream(SAMPLE.getBytes("UTF-8"));
		assertThat(discoverer.findLinkWithRel("something", inputStream), is(nullValue()));
	}
}
