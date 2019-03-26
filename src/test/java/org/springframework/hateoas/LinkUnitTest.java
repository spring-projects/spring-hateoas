/*
 * Copyright 2012-2018 the original author or authors.
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
import static org.assertj.core.api.SoftAssertions.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Unit tests for {@link Link}.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author Jens Schauder
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

		assertSoftly(softly -> {

			softly.assertThat(link.getHref()).isEqualTo("foo");
			softly.assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		});
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

		assertSoftly(softly -> {

			softly.assertThat(Link.valueOf(null)).isNull();
			softly.assertThat(Link.valueOf("")).isNull();
		});
	}

	/**
	 * @see #54
	 * @see #100
	 * @see #678
	 */
	@Test
	public void parsesRFC5988HeaderIntoLink() {

		assertSoftly(softly -> {

			softly.assertThat(Link.valueOf("</something>;rel=\"foo\"")).isEqualTo(new Link("/something", "foo"));
			softly.assertThat(Link.valueOf("</something>;rel=\"foo\";title=\"Some title\""))
					.isEqualTo(new Link("/something", "foo"));
			softly.assertThat(Link.valueOf("</customer/1>;" //
					+ "rel=\"self\";" //
					+ "hreflang=\"en\";" //
					+ "media=\"pdf\";" //
					+ "title=\"pdf customer copy\";" //
					+ "type=\"portable document\";" //
					+ "deprecation=\"https://example.com/customers/deprecated\";" //
					+ "profile=\"my-profile\"")) //
					.isEqualTo(new Link("/customer/1") //
							.withHreflang("en") //
							.withMedia("pdf") //
							.withTitle("pdf customer copy") //
							.withType("portable document") //
							.withDeprecation("https://example.com/customers/deprecated").withProfile("my-profile"));
		});
	}

	/**
	 * @see #100
	 */
	@Test
	public void ignoresUnrecognizedAttributes() {
		Link link = Link.valueOf("</something>;rel=\"foo\";unknown=\"should fail\"");

		assertSoftly(softly -> {

			softly.assertThat(link.getHref()).isEqualTo("/something");
			softly.assertThat(link.getRel()).isEqualTo("foo");
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsMissingRelAttribute() {
		Link.valueOf("</something>;title=\"title\"");
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsLinkWithoutAttributesAtAll() {
		Link.valueOf("</something>");
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

		assertSoftly(softly -> {

			softly.assertThat(link.isTemplated()).isTrue();
			softly.assertThat(link.getVariableNames()).hasSize(1);
			softly.assertThat(link.getVariableNames()).contains("page");
			softly.assertThat(link.expand("2")).isEqualTo(new Link("/foo?page=2"));
		});
	}

	/**
	 * @see #137
	 */
	@Test
	public void isntTemplatedIfSourceDoesNotContainTemplateVariables() {

		Link link = new Link("/foo");

		assertSoftly(softly -> {

			softly.assertThat(link.isTemplated()).isFalse();
			softly.assertThat(link.getVariableNames()).hasSize(0);
		});
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

		assertThat(Link.valueOf("<http://localhost>; rel=\"https://acme.com/rels/foo-bar\"").getRel()) //
				.isEqualTo("https://acme.com/rels/foo-bar");
	}

	/**
	 * @see #340
	 */
	@Test
	public void linkWithAffordancesShouldWorkProperly() {

		Link originalLink = new Link("/foo");
		Link linkWithAffordance = originalLink.andAffordance(new TestAffordance());
		Link linkWithTwoAffordances = linkWithAffordance.andAffordance(new TestAffordance());

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
	public void exposesLinkRelation() {

		Link link = new Link("/", "foo");

		assertThat(link.hasRel("foo")).isTrue();
		assertThat(link.hasRel("bar")).isFalse();
	}

	/**
	 * @see #671
	 */
	@Test
	public void rejectsInvalidRelationsOnHasRel() {

		Link link = new Link("/");

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> link.hasRel(null));
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> link.hasRel(""));
	}

	static class TestAffordance implements Affordance {

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.Affordance#getAffordanceModel(org.springframework.http.MediaType)
		 */
		@Override
		public <T extends AffordanceModel> T getAffordanceModel(MediaType mediaType) {
			return null;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.Affordance#getHttpMethod()
		 */
		@Override
		public HttpMethod getHttpMethod() {
			return HttpMethod.PATCH;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.Affordance#getName()
		 */
		@Override
		public String getName() {
			return null;
		}

		@Override
		public List<MethodParameter> getInputMethodParameters() {
			return null;
		}

		@Override
		public List<QueryParameter> getQueryMethodParameters() {
			return null;
		}
	}
}
