/*
 * Copyright 2012-2017 the original author or authors.
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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

/**
 * Unit tests for {@link Link}.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class LinkUnitTest {

	@Test
	public void linkWithHrefOnlyBecomesSelfLink() {
		Link link = new Link("foo");
		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
	}

	@Test
	public void createsLinkFromRelAndHref() {
		Link link = new Link("foo", Link.REL_SELF);
		assertThat(link.getHref()).isEqualTo("foo");
		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullHref() {
		new Link(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullRel() {
		new Link("foo", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsEmptyHref() {
		new Link("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsEmptyRel() {
		new Link("foo", "");
	}

	@Test
	public void sameRelAndHrefMakeSameLink() {

		Link left = new Link("foo", Link.REL_SELF);
		Link right = new Link("foo", Link.REL_SELF);

		TestUtils.assertEqualAndSameHashCode(left, right);
	}

	@Test
	public void differentRelMakesDifferentLink() {

		Link left = new Link("foo", Link.REL_PREVIOUS);
		Link right = new Link("foo", Link.REL_NEXT);

		TestUtils.assertNotEqualAndDifferentHashCode(left, right);
	}

	@Test
	public void differentHrefMakesDifferentLink() {

		Link left = new Link("foo", Link.REL_SELF);
		Link right = new Link("bar", Link.REL_SELF);

		TestUtils.assertNotEqualAndDifferentHashCode(left, right);
	}

	@Test
	public void differentTypeDoesNotEqual() {
		assertThat(new Link("foo")).isNotEqualTo((Object) new ResourceSupport());
	}

	@Test
	public void returnsNullForNullOrEmptyLink() {

		assertThat(Link.valueOf(null)).isNull();
		assertThat(Link.valueOf("")).isNull();
	}

	/**
	 * @see #54
	 * @see #100
	 */
	@Test
	public void parsesRFC5988HeaderIntoLink() {

		assertThat(Link.valueOf("</something>;rel=\"foo\"")).isEqualTo(new Link("/something", "foo"));
		assertThat(Link.valueOf("</something>;rel=\"foo\";title=\"Some title\"")).isEqualTo(new Link("/something", "foo"));
		assertThat(Link.valueOf("</customer/1>;" //
				+ "rel=\"self\";" //
				+ "hreflang=\"en\";" //
				+ "media=\"pdf\";" //
				+ "title=\"pdf customer copy\";" //
				+ "type=\"portable document\";" //
				+ "deprecation=\"http://example.com/customers/deprecated\"")) //
						.isEqualTo(new Link("/customer/1") //
								.withHreflang("en") //
								.withMedia("pdf") //
								.withTitle("pdf customer copy") //
								.withType("portable document") //
								.withDeprecation("http://example.com/customers/deprecated"));
	}

	/**
	 * @see #100
	 */
	@Test
	public void ignoresUnrecognizedAttributes() {
		Link link = Link.valueOf("</something>;rel=\"foo\";unknown=\"should fail\"");

		assertThat(link.getHref()).isEqualTo("/something");
		assertThat(link.getRel()).isEqualTo("foo");
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsMissingRelAttribute() {
		Link.valueOf("</something>;title=\"title\"");
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsLinkWithoutAttributesAtAll() {

		Link link = Link.valueOf("</something>");

		System.out.println(link);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNonRFC5988String() {
		Link.valueOf("foo");
	}

	/**
	 * @see #137
	 */
	@Test
	public void isTemplatedIfSourceContainsTemplateVariables() {

		Link link = new Link("/foo{?page}");

		assertThat(link.isTemplated()).isTrue();
		assertThat(link.getVariableNames()).hasSize(1);
		assertThat(link.getVariableNames()).contains("page");
		assertThat(link.expand("2")).isEqualTo(new Link("/foo?page=2"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void isntTemplatedIfSourceDoesNotContainTemplateVariables() {

		Link link = new Link("/foo");

		assertThat(link.isTemplated()).isFalse();
		assertThat(link.getVariableNames()).hasSize(0);
	}

	/**
	 * @see #172
	 */
	@Test
	public void serializesCorrectly() throws IOException {

		Link link = new Link("http://foobar{?foo,bar}");

		ObjectOutputStream stream = new ObjectOutputStream(new ByteArrayOutputStream());
		stream.writeObject(link);
		stream.close();
	}

	/**
	 * @see #312
	 */
	@Test
	public void keepsCompleteBaseUri() {

		Link link = new Link("/customer/{customerId}/programs", "programs");
		assertThat(link.getHref()).isEqualTo("/customer/{customerId}/programs");
	}

	/**
	 * @see #504
	 */
	@Test
	public void parsesLinkRelationWithDotAndMinus() {
		assertThat(Link.valueOf("<http://localhost>; rel=\"rel-with-minus-and-.\"").getRel())
				.isEqualTo("rel-with-minus-and-.");
	}

	/**
	 * @see #504
	 */
	@Test
	public void parsesUriLinkRelations() {

		assertThat(Link.valueOf("<http://localhost>; rel=\"http://acme.com/rels/foo-bar\"").getRel()) //
				.isEqualTo("http://acme.com/rels/foo-bar");
	}
}
