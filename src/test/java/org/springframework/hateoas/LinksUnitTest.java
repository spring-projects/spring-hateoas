/*
 * Copyright 2013-2017 the original author or authors.
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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

/**
 * Unit test for {@link Links}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class LinksUnitTest {

	static final String FIRST = "</something>;rel=\"foo\"";
	static final String SECOND = "</somethingElse>;rel=\"bar\"";
	static final String WITH_COMMA = "<http://localhost:8080/test?page=0&filter=foo,bar>;rel=\"foo\"";
	static final String WITH_WHITESPACE = "</something>; rel=\"foo\"," + SECOND;

	static final String LINKS = StringUtils.collectionToCommaDelimitedString(Arrays.asList(FIRST, SECOND));

	static final String THIRD = "</something>;rel=\"foo\";hreflang=\"en\"";
	static final String FOURTH = "</somethingElse>;rel=\"bar\";hreflang=\"de\"";

	static final String LINKS2 = StringUtils.collectionToCommaDelimitedString(Arrays.asList(THIRD, FOURTH));

	static final Links reference = Links.of(new Link("/something", "foo"), new Link("/somethingElse", "bar"));
	static final Links reference2 = Links.of(new Link("/something", "foo").withHreflang("en"),
			new Link("/somethingElse", "bar").withHreflang("de"));

	@Test
	void parsesLinkHeaderLinks() {

		assertThat(Links.parse(LINKS)).isEqualTo(reference);
		assertThat(Links.parse(LINKS2)).isEqualTo(reference2);
		assertThat(reference.toString()).isEqualTo(LINKS);
		assertThat(reference2.toString()).isEqualTo(LINKS2);
	}

	@Test
	void skipsEmptyLinkElements() {
		assertThat(Links.parse(LINKS + ",,,")).isEqualTo(reference);
		assertThat(Links.parse(LINKS2 + ",,,")).isEqualTo(reference2);
	}

	@Test
	void returnsNullForNullOrEmptySource() {

		assertThat(Links.parse(null)).isEqualTo(Links.NONE);
		assertThat(Links.parse("")).isEqualTo(Links.NONE);
	}

	/**
	 * @see #54
	 * @see #100
	 */
	@Test
	void getSingleLinkByRel() {
		assertThat(reference.getLink("bar")).hasValue(new Link("/somethingElse", "bar"));
		assertThat(reference2.getLink("bar")).hasValue(new Link("/somethingElse", "bar").withHreflang("de"));
	}

	/**
	 * @see #440
	 */
	@Test
	void parsesLinkWithComma() {

		Link withComma = new Link("http://localhost:8080/test?page=0&filter=foo,bar", "foo");

		assertThat(Links.parse(WITH_COMMA).getLink("foo")).isEqualTo(Optional.of(withComma));

		Links twoWithCommaInFirst = Links.parse(WITH_COMMA.concat(",").concat(SECOND));

		assertThat(twoWithCommaInFirst.getLink("foo")).hasValue(withComma);
		assertThat(twoWithCommaInFirst.getLink("bar")).hasValue(new Link("/somethingElse", "bar"));
	}

	/**
	 * @see https://tools.ietf.org/html/rfc5988#section-5.5
	 */
	@Test
	void parsesLinksWithWhitespace() {
		assertThat(Links.parse(WITH_WHITESPACE)).isEqualTo(reference);
	}

	@Test // #805
	void returnsRequiredLink() {

		Link reference = new Link("http://localhost", "someRel");
		Links links = Links.of(reference);

		assertThat(links.getRequiredLink("someRel")).isEqualTo(reference);
	}

	@Test // #805
	void rejectsMissingLinkWithIllegalArgumentException() {

		Links links = Links.of();

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> links.getRequiredLink("self")) //
				.withMessageContaining("self");
	}

	@Test
	void detectsContainedLinks() {

		Link first = new Link("http://localhost", "someRel");
		Link second = new Link("http://localhost", "someOtherRel");

		assertThat(Links.of(first).contains(first)).isTrue();
		assertThat(Links.of(first).contains(second)).isFalse();

		assertThat(Links.of(first).containsSameLinksAs(Links.of(first, second))).isFalse();
		assertThat(Links.of(first, second).containsSameLinksAs(Links.of(first))).isFalse();
		assertThat(Links.of(first, second).containsSameLinksAs(Links.of(first, second))).isTrue();
	}
}
