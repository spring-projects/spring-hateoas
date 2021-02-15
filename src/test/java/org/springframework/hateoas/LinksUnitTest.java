/*
 * Copyright 2013-2021 the original author or authors.
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

import lombok.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.hateoas.Links.MergeMode;
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

	static final Links reference = Links.of(Link.of("/something", "foo"), Link.of("/somethingElse", "bar"));
	static final Links reference2 = Links.of(Link.of("/something", "foo").withHreflang("en"),
			Link.of("/somethingElse", "bar").withHreflang("de"));

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
		assertThat(reference.getLink("bar")).hasValue(Link.of("/somethingElse", "bar"));
		assertThat(reference2.getLink("bar")).hasValue(Link.of("/somethingElse", "bar").withHreflang("de"));
	}

	/**
	 * @see #440
	 */
	@Test
	void parsesLinkWithComma() {

		Link withComma = Link.of("http://localhost:8080/test?page=0&filter=foo,bar", "foo");

		assertThat(Links.parse(WITH_COMMA).getLink("foo")).isEqualTo(Optional.of(withComma));

		Links twoWithCommaInFirst = Links.parse(WITH_COMMA.concat(",").concat(SECOND));

		assertThat(twoWithCommaInFirst.getLink("foo")).hasValue(withComma);
		assertThat(twoWithCommaInFirst.getLink("bar")).hasValue(Link.of("/somethingElse", "bar"));
	}

	/**
	 * @see https://tools.ietf.org/html/rfc8288#section-3.5
	 */
	@Test
	void parsesLinksWithWhitespace() {
		assertThat(Links.parse(WITH_WHITESPACE)).isEqualTo(reference);
	}

	@Test // #805
	void returnsRequiredLink() {

		Link reference = Link.of("http://localhost", "someRel");
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

		Link first = Link.of("http://localhost", "someRel");
		Link second = Link.of("http://localhost", "someOtherRel");

		assertThat(Links.of(first).contains(first)).isTrue();
		assertThat(Links.of(first).contains(second)).isFalse();

		assertThat(Links.of(first).containsSameLinksAs(Links.of(first, second))).isFalse();
		assertThat(Links.of(first, second).containsSameLinksAs(Links.of(first))).isFalse();
		assertThat(Links.of(first, second).containsSameLinksAs(Links.of(first, second))).isTrue();
	}

	@TestFactory // #1322, #1341
	Stream<DynamicTest> conditionallyAddsLink() {

		Links links = Links.NONE;
		Link link = Link.of("/foo");

		List<NamedLinks> adders = Arrays.asList(//
				NamedLinks.of("adds via varargs", links.andIf(true, link)), //
				NamedLinks.of("adds via Supplier", links.andIf(true, () -> link)), //
				NamedLinks.of("adds via Stream", links.andIf(true, Stream.of(link))));

		Stream<DynamicTest> adderTests = DynamicTest.stream(adders.iterator(), NamedLinks::getName, it -> {
			assertThat(it.links.getRequiredLink(IanaLinkRelations.SELF).getHref()).isEqualTo("/foo");
		});

		List<NamedLinks> nonAdders = Arrays.asList(//
				NamedLinks.of("does not add via varargs", links.andIf(false, link)), //
				NamedLinks.of("does not add via Supplier", links.andIf(false, () -> link)), //
				NamedLinks.of("does not add via Stream", links.andIf(false, Stream.of(link))));

		Stream<DynamicTest> nonAdderTests = DynamicTest.stream(nonAdders.iterator(), NamedLinks::getName, it -> {
			assertThat(it.links).isEmpty();
		});

		return Stream.concat(adderTests, nonAdderTests);
	}

	@Test // #1322
	void doesNotEvaluateSupplierIfAddConditionIsFalse() {

		assertThatCode(() -> Links.NONE.andIf(false, () -> {
			throw new IllegalStateException();
		})).doesNotThrowAnyException();
	}

	@TestFactory // #1340
	Stream<DynamicTest> addsStreamOfLinks() {

		Links links = Links.NONE;
		Link link = Link.of("/foo");

		List<NamedLinks> sources = Arrays.asList(//
				NamedLinks.of("via varargs", links.and(link)), //
				NamedLinks.of("via Iterable", links.and(Arrays.asList(link))), //
				NamedLinks.of("via Stream", links.and(Stream.of(link))));

		return DynamicTest.stream(sources.iterator(), NamedLinks::getName,
				it -> assertThat(it.links.getRequiredLink(IanaLinkRelations.SELF)).isNotNull());
	}

	@TestFactory // #1340
	Stream<DynamicTest> mergesStreamOfLinks() {

		Links links = Links.NONE.and(Link.of("/foo"));
		Link same = Link.of("/foo");
		Link sameRel = Link.of("/bar");

		List<NamedLinks> sources = Arrays.asList(//
				NamedLinks.of("merge same via varargs", links.merge(same)),
				NamedLinks.of("merge same rel via varargs", links.merge(MergeMode.SKIP_BY_REL, sameRel)),
				NamedLinks.of("merge same via Stream", links.merge(Stream.of(same))),
				NamedLinks.of("merge same rel via Stream", links.merge(MergeMode.SKIP_BY_REL, Stream.of(sameRel))));

		return DynamicTest.stream(sources.iterator(), NamedLinks::getName,
				it -> assertThat(it.links).hasSize(1) //
						.element(0).extracting(Link::getHref).isEqualTo("/foo"));
	}

	@TestFactory // #1340
	Stream<DynamicTest> replacesLinksViaMerge() {

		Links links = Links.of(Link.of("/foo"));
		Link sameRel = Link.of("/bar");

		List<NamedLinks> sources = Arrays.asList(//
				NamedLinks.of("replace same rel via varargs", links.merge(MergeMode.REPLACE_BY_REL, sameRel)),
				NamedLinks.of("replace same rel via Stream", links.merge(MergeMode.REPLACE_BY_REL, Stream.of(sameRel))));

		return DynamicTest.stream(sources.iterator(), NamedLinks::getName,
				it -> assertThat(it.links).hasSize(1) //
						.element(0).extracting(Link::getHref).isEqualTo("/bar"));
	}

	@Test
	void removesLinkByRel() {
		assertThat(Links.of(Link.of("/foo")).without(IanaLinkRelations.SELF)).isEmpty();
	}

	@Test
	void basics() {

		Links none = Links.NONE;

		assertThat(none.isEmpty()).isTrue();
		assertThat(none.stream()).isEmpty();
		assertThat(none.hasSingleLink()).isFalse();
		assertThat(none.hasSize(0)).isTrue();
		assertThat(none.hasLink("self")).isFalse();

		Links one = none.and(Link.of("/foo"));

		assertThat(one.isEmpty()).isFalse();
		assertThat(one.stream()).isNotEmpty();
		assertThat(one.hasSingleLink()).isTrue();
		assertThat(one.hasSize(1)).isTrue();
		assertThat(one.hasLink("self")).isTrue();

		Links anotherOne = none.and(Link.of("/foo"));

		assertThat(anotherOne).isEqualTo(one);
		assertThat(one).isEqualTo(anotherOne);
		assertThat(one.hashCode()).isEqualTo(anotherOne.hashCode());
	}

	@Test // #1458
	void supportsUnquotedAttributes() {

		assertThat(Links.parse("<https://url.com?page=1>; rel=first").getRequiredLink("first").getHref())
				.isEqualTo("https://url.com?page=1");
	}

	@Value(staticConstructor = "of")
	static class NamedLinks {
		String name;
		Links links;
	}
}
