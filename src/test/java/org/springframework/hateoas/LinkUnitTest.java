/*
 * Copyright 2012-2021 the original author or authors.
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
import static org.assertj.core.api.SoftAssertions.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

/**
 * Unit tests for {@link Link}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author Jens Schauder
 */
class LinkUnitTest {

	@Test
	void linkWithHrefOnlyBecomesSelfLink() {
		assertThat(Link.of("foo").hasRel(IanaLinkRelations.SELF)).isTrue();
	}

	@Test
	void createsLinkFromRelAndHref() {

		Link link = Link.of("foo", IanaLinkRelations.SELF);

		assertSoftly(softly -> {

			softly.assertThat(link.getHref()).isEqualTo("foo");
			softly.assertThat(link.hasRel(IanaLinkRelations.SELF)).isTrue();
		});
	}

	@SuppressWarnings("null")
	@Test
	void rejectsNullHref() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			Link.of(null);
		});
	}

	@SuppressWarnings("null")
	@Test
	void rejectsNullRel() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			Link.of("foo", (String) null);
		});
	}

	@Test
	void rejectsEmptyHref() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			Link.of("");
		});
	}

	@Test
	void rejectsEmptyRel() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			Link.of("foo", "");
		});
	}

	@Test
	void sameRelAndHrefMakeSameLink() {

		Link left = Link.of("foo", IanaLinkRelations.SELF);
		Link right = Link.of("foo", IanaLinkRelations.SELF);

		TestUtils.assertEqualAndSameHashCode(left, right);
	}

	@Test
	void differentRelMakesDifferentLink() {

		Link left = Link.of("foo", IanaLinkRelations.PREV);
		Link right = Link.of("foo", IanaLinkRelations.NEXT);

		TestUtils.assertNotEqualAndDifferentHashCode(left, right);
	}

	@Test
	void differentHrefMakesDifferentLink() {

		Link left = Link.of("foo", IanaLinkRelations.SELF);
		Link right = Link.of("bar", IanaLinkRelations.SELF);

		TestUtils.assertNotEqualAndDifferentHashCode(left, right);
	}

	@Test
	void differentTypeDoesNotEqual() {
		assertThat(Link.of("foo")).isNotEqualTo(new RepresentationModel<>());
	}

	/**
	 * @see #54
	 * @see #100
	 * @see #678
	 */
	@Test
	void parsesRFC8288HeaderIntoLink() {

		assertSoftly(softly -> {

			softly.assertThat(Link.valueOf("</something>;rel=\"foo\"")).isEqualTo(Link.of("/something", "foo"));
			softly.assertThat(Link.valueOf("</something>;rel=\"foo\";title=\"Some title\""))
					.isEqualTo(Link.of("/something", "foo").withTitle("Some title"));
			softly.assertThat(Link.valueOf("</customer/1>;" //
					+ "rel=\"self\";" //
					+ "hreflang=\"en\";" //
					+ "media=\"pdf\";" //
					+ "title=\"pdf customer copy\";" //
					+ "type=\"portable document\";" //
					+ "deprecation=\"https://example.com/customers/deprecated\";" //
					+ "profile=\"my-profile\";" //
					+ "name=\"my-name\";")) //
					.isEqualTo(Link.of("/customer/1") //
							.withHreflang("en") //
							.withMedia("pdf") //
							.withTitle("pdf customer copy") //
							.withType("portable document") //
							.withDeprecation("https://example.com/customers/deprecated") //
							.withProfile("my-profile") //
							.withName("my-name"));
		});
	}

	/**
	 * @see #100
	 */
	@Test
	void ignoresUnrecognizedAttributes() {

		Link link = Link.valueOf("</something>;rel=\"foo\";unknown=\"should fail\"");

		assertSoftly(softly -> {

			softly.assertThat(link.getHref()).isEqualTo("/something");
			softly.assertThat(link.hasRel("foo")).isTrue();
		});
	}

	@Test
	void rejectsMissingRelAttribute() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			Link.valueOf("</something>;title=\"title\"");
		});
	}

	@Test
	void rejectsLinkWithoutAttributesAtAll() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			Link.valueOf("</something>");
		});
	}

	@Test
	void rejectsNonRFC8288String() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			Link.valueOf("foo");
		});
	}

	/**
	 * @see #137
	 */
	@Test
	void isTemplatedIfSourceContainsTemplateVariables() {

		Link link = Link.of("/foo{?page}");

		assertSoftly(softly -> {

			softly.assertThat(link.isTemplated()).isTrue();
			softly.assertThat(link.getVariableNames()).hasSize(1);
			softly.assertThat(link.getVariableNames()).contains("page");
			softly.assertThat(link.expand("2")).isEqualTo(Link.of("/foo?page=2"));
		});
	}

	/**
	 * @see #137
	 */
	@Test
	void isntTemplatedIfSourceDoesNotContainTemplateVariables() {

		Link link = Link.of("/foo");

		assertSoftly(softly -> {

			softly.assertThat(link.isTemplated()).isFalse();
			softly.assertThat(link.getVariableNames()).hasSize(0);
		});
	}

	/**
	 * @see #172
	 */
	@Test
	void serializesCorrectly() throws IOException {

		Link link = Link.of("https://foobar{?foo,bar}");

		ObjectOutputStream stream = new ObjectOutputStream(new ByteArrayOutputStream());
		stream.writeObject(link);
		stream.close();
	}

	/**
	 * @see #312
	 */
	@Test
	void keepsCompleteBaseUri() {

		Link link = Link.of("/customer/{customerId}/programs", "programs");
		assertThat(link.getHref()).isEqualTo("/customer/{customerId}/programs");
	}

	/**
	 * @see #504
	 */
	@Test
	void parsesLinkRelationWithDotAndMinus() {

		assertThat(Link.valueOf("<http://localhost>; rel=\"rel-with-minus-and-.\"").hasRel("rel-with-minus-and-."))
				.isTrue();
	}

	/**
	 * @see #504
	 */
	@Test
	void parsesUriLinkRelations() {

		assertThat(Link.valueOf("<http://localhost>; rel=\"https://acme.com/rels/foo-bar\"").getRel()) //
				.isEqualTo(LinkRelation.of("https://acme.com/rels/foo-bar"));
	}

	/**
	 * @see #340
	 */
	@Test
	void linkWithAffordancesShouldWorkProperly() {

		Link originalLink = Link.of("/foo");

		Link linkWithAffordance = Affordances.of(originalLink).afford(HttpMethod.GET).toLink();
		Link linkWithTwoAffordances = Affordances.of(linkWithAffordance).afford(HttpMethod.GET).toLink();

		assertSoftly(softly -> {

			softly.assertThat(originalLink.getAffordances()).hasSize(0);
			softly.assertThat(linkWithAffordance.getAffordances()).hasSize(1);
			softly.assertThat(linkWithTwoAffordances.getAffordances()).hasSize(2);

			softly.assertThat(originalLink.hashCode()).isNotEqualTo(linkWithAffordance.hashCode());
			softly.assertThat(originalLink).isNotEqualTo(linkWithAffordance);

			softly.assertThat(linkWithAffordance.hashCode()).isNotEqualTo(linkWithTwoAffordances.hashCode());
			softly.assertThat(linkWithAffordance).isNotEqualTo(linkWithTwoAffordances);
		});
	}

	/**
	 * @see #671
	 */
	@Test
	void exposesLinkRelation() {

		Link link = Link.of("/", "foo");

		assertThat(link.hasRel("foo")).isTrue();
		assertThat(link.hasRel("bar")).isFalse();
	}

	/**
	 * @see #671
	 */
	@Test
	void rejectsInvalidRelationsOnHasRel() {

		Link link = Link.of("/");

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> link.hasRel((String) null));
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> link.hasRel(""));
	}

	@Test
	void createsUriForSimpleLink() {
		assertThat(Link.of("/something").toUri()).isEqualTo(URI.create("/something"));
	}

	@Test
	void createsUriForTemplateWithOptionalParameters() {
		assertThat(Link.of("/something{?parameter}").toUri()).isEqualTo(URI.create("/something"));
	}

	@Test
	void uriCreationRejectsLinkWithUnresolvedMandatoryParameters() {
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> Link.of("/{segment}/path").toUri());
	}
}
