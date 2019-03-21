/*
 * Copyright 2012-2019 the original author or authors.
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
import java.util.Collections;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.support.Employee;
import org.springframework.http.HttpMethod;

/**
 * Unit tests for {@link Link}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author Jens Schauder
 */
public class LinkUnitTest {

	private static final Affordance TEST_AFFORDANCE = new Affordance(null, null, HttpMethod.GET, null,
			Collections.emptyList(), null);

	@Test
	public void linkWithHrefOnlyBecomesSelfLink() {
		assertThat(new Link("foo").hasRel(IanaLinkRelations.SELF)).isTrue();
	}

	@Test
	public void createsLinkFromRelAndHref() {

		Link link = new Link("foo", IanaLinkRelations.SELF);

		assertSoftly(softly -> {

			softly.assertThat(link.getHref()).isEqualTo("foo");
			softly.assertThat(link.hasRel(IanaLinkRelations.SELF)).isTrue();
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullHref() {
		new Link(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullRel() {
		new Link("foo", (String) null);
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

		Link left = new Link("foo", IanaLinkRelations.SELF);
		Link right = new Link("foo", IanaLinkRelations.SELF);

		TestUtils.assertEqualAndSameHashCode(left, right);
	}

	@Test
	public void differentRelMakesDifferentLink() {

		Link left = new Link("foo", IanaLinkRelations.PREV);
		Link right = new Link("foo", IanaLinkRelations.NEXT);

		TestUtils.assertNotEqualAndDifferentHashCode(left, right);
	}

	@Test
	public void differentHrefMakesDifferentLink() {

		Link left = new Link("foo", IanaLinkRelations.SELF);
		Link right = new Link("bar", IanaLinkRelations.SELF);

		TestUtils.assertNotEqualAndDifferentHashCode(left, right);
	}

	@Test
	public void differentTypeDoesNotEqual() {
		assertThat(new Link("foo")).isNotEqualTo(new ResourceSupport());
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
					+ "deprecation=\"http://example.com/customers/deprecated\";" //
					+ "profile=\"my-profile\";" //
					+ "name=\"my-name\";")) //
					.isEqualTo(new Link("/customer/1") //
							.withHreflang("en") //
							.withMedia("pdf") //
							.withTitle("pdf customer copy") //
							.withType("portable document") //
							.withDeprecation("http://example.com/customers/deprecated") //
							.withProfile("my-profile") //
							.withName("my-name"));
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
			softly.assertThat(link.hasRel("foo")).isTrue();
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

		assertThat(Link.valueOf("<http://localhost>; rel=\"rel-with-minus-and-.\"").hasRel("rel-with-minus-and-."))
				.isTrue();
	}

	/**
	 * @see #504
	 */
	@Test
	public void parsesUriLinkRelations() {

		assertThat(Link.valueOf("<http://localhost>; rel=\"http://acme.com/rels/foo-bar\"").getRel()) //
				.isEqualTo(LinkRelation.of("http://acme.com/rels/foo-bar"));
	}

	/**
	 * @see #340
	 */
	@Test
	public void linkWithAffordancesShouldWorkProperly() {

		Link originalLink = new Link("/foo");
		Link linkWithAffordance = originalLink.andAffordance(TEST_AFFORDANCE);
		Link linkWithTwoAffordances = linkWithAffordance.andAffordance(TEST_AFFORDANCE);

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

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> link.hasRel((String) null));
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> link.hasRel(""));
	}

	@Test
	public void affordanceConvenienceMethodChainsExistingLink() {

		Link link = new Link("/").andAffordance("name", HttpMethod.POST, ResolvableType.forClass(Employee.class),
				Collections.emptyList(), ResolvableType.forClass(Employee.class));

		assertThat(link.getHref()).isEqualTo("/");
		assertThat(link.hasRel(IanaLinkRelations.SELF)).isTrue();
		assertThat(link.getAffordances()).hasSize(1);
		assertThat(link.getAffordances().get(0).getAffordanceModels()).hasSize(3);

		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getName()).isEqualTo("name");
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getHttpMethod())
				.isEqualTo(HttpMethod.POST);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getInputType().resolve())
				.isEqualTo(Employee.class);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getQueryMethodParameters())
				.hasSize(0);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getOutputType().resolve())
				.isEqualTo(Employee.class);

		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getName()).isEqualTo("name");
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getHttpMethod())
				.isEqualTo(HttpMethod.POST);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getInputType().resolve())
				.isEqualTo(Employee.class);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getQueryMethodParameters())
				.hasSize(0);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getOutputType().resolve())
				.isEqualTo(Employee.class);

		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getName()).isEqualTo("name");
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getHttpMethod())
				.isEqualTo(HttpMethod.POST);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getInputType().resolve())
				.isEqualTo(Employee.class);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getQueryMethodParameters())
				.hasSize(0);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getOutputType().resolve())
				.isEqualTo(Employee.class);
	}

	@Test
	public void affordanceConvenienceMethodDefaultsNameBasedOnHttpVerb() {

		Link link = new Link("/").andAffordance(HttpMethod.POST, ResolvableType.forClass(Employee.class),
				Collections.emptyList(), ResolvableType.forClass(Employee.class));

		assertThat(link.getHref()).isEqualTo("/");
		assertThat(link.hasRel(IanaLinkRelations.SELF)).isTrue();
		assertThat(link.getAffordances()).hasSize(1);
		assertThat(link.getAffordances().get(0).getAffordanceModels()).hasSize(3);

		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getName())
				.isEqualTo("postEmployee");
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getHttpMethod())
				.isEqualTo(HttpMethod.POST);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getInputType().resolve())
				.isEqualTo(Employee.class);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getQueryMethodParameters())
				.hasSize(0);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getOutputType().resolve())
				.isEqualTo(Employee.class);

		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getName())
				.isEqualTo("postEmployee");
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getHttpMethod())
				.isEqualTo(HttpMethod.POST);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getInputType().resolve())
				.isEqualTo(Employee.class);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getQueryMethodParameters())
				.hasSize(0);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getOutputType().resolve())
				.isEqualTo(Employee.class);

		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getName())
				.isEqualTo("postEmployee");
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getHttpMethod())
				.isEqualTo(HttpMethod.POST);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getInputType().resolve())
				.isEqualTo(Employee.class);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getQueryMethodParameters())
				.hasSize(0);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getOutputType().resolve())
				.isEqualTo(Employee.class);
	}

	@Test
	public void affordanceConvenienceMethodHandlesBareClasses() {

		Link link = new Link("/").andAffordance(HttpMethod.POST, Employee.class, Collections.emptyList(), Employee.class);

		assertThat(link.getHref()).isEqualTo("/");
		assertThat(link.hasRel(IanaLinkRelations.SELF)).isTrue();
		assertThat(link.getAffordances()).hasSize(1);
		assertThat(link.getAffordances().get(0).getAffordanceModels()).hasSize(3);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getName())
				.isEqualTo("postEmployee");
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getHttpMethod())
				.isEqualTo(HttpMethod.POST);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getInputType().resolve())
				.isEqualTo(Employee.class);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getQueryMethodParameters())
				.hasSize(0);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.COLLECTION_JSON).getOutputType().resolve())
				.isEqualTo(Employee.class);

		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getName())
				.isEqualTo("postEmployee");
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getHttpMethod())
				.isEqualTo(HttpMethod.POST);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getInputType().resolve())
				.isEqualTo(Employee.class);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getQueryMethodParameters())
				.hasSize(0);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.HAL_FORMS_JSON).getOutputType().resolve())
				.isEqualTo(Employee.class);

		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getName())
				.isEqualTo("postEmployee");
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getHttpMethod())
				.isEqualTo(HttpMethod.POST);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getInputType().resolve())
				.isEqualTo(Employee.class);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getQueryMethodParameters())
				.hasSize(0);
		assertThat(link.getAffordances().get(0).getAffordanceModel(MediaTypes.UBER_JSON).getOutputType().resolve())
				.isEqualTo(Employee.class);
	}

}
