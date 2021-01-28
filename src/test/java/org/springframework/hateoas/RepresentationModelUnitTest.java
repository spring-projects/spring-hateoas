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

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RepresentationModel}.
 *
 * @author Oliver Gierke
 */
class RepresentationModelUnitTest {

	@Test
	void setsUpWithEmptyLinkList() {

		RepresentationModel<?> support = new RepresentationModel<>();

		assertThat(support.hasLinks()).isFalse();
		assertThat(support.hasLink(IanaLinkRelations.SELF.value())).isFalse();
		assertThat(support.getLinks().isEmpty()).isTrue();
		assertThat(support.getLinks(IanaLinkRelations.SELF.value()).isEmpty()).isTrue();
	}

	@Test
	void addsLinkCorrectly() {

		Link link = Link.of("foo", IanaLinkRelations.NEXT.value());
		RepresentationModel<?> support = new RepresentationModel<>();
		support.add(link);

		assertThat(support.hasLinks()).isTrue();
		assertThat(support.hasLink(link.getRel())).isTrue();
		assertThat(support.getLink(link.getRel())).hasValue(link);
		assertThat(support.getLinks(IanaLinkRelations.NEXT.value())).contains(link);
	}

	@Test
	void addsMultipleLinkRelationsCorrectly() {

		Link link = Link.of("/customers/1", "customers");
		Link link2 = Link.of("/orders/1/customer", "customers");
		RepresentationModel<?> support = new RepresentationModel<>();
		support.add(link, link2);

		assertThat(support.getLinks("customers")).hasSize(2);
		assertThat(support.getLinks("customers")).contains(link, link2);
		assertThat(support.getLinks("non-existent")).hasSize(0);
		assertThat(support.getLinks("non-existent")).isEmpty();
	}

	@Test
	void addsLinksCorrectly() {

		Link first = Link.of("foo", IanaLinkRelations.PREV.value());
		Link second = Link.of("bar", IanaLinkRelations.NEXT.value());

		RepresentationModel<?> support = new RepresentationModel<>();
		support.add(Arrays.asList(first, second));

		assertThat(support.hasLinks()).isTrue();
		assertThat(support.getLinks()).contains(first, second);
		assertThat(support.getLinks()).hasSize(2);
		assertThat(support.getLinks(IanaLinkRelations.PREV.value())).contains(first);
		assertThat(support.getLinks(IanaLinkRelations.NEXT.value())).contains(second);
	}

	@Test
	void preventsNullLinkBeingAdded() {

		RepresentationModel<?> support = new RepresentationModel<>();

		assertThatIllegalArgumentException().isThrownBy(() -> {
			support.add((Link) null);
		});
	}

	@Test
	void preventsNullLinksBeingAdded() {

		RepresentationModel<?> support = new RepresentationModel<>();

		assertThatIllegalArgumentException().isThrownBy(() -> {
			support.add((Iterable<Link>) null);
		});
	}

	@Test
	void sameLinkListMeansSameResource() {

		RepresentationModel<?> first = new RepresentationModel<>();
		RepresentationModel<?> second = new RepresentationModel<>();

		TestUtils.assertEqualAndSameHashCode(first, second);

		Link link = Link.of("foo");
		first.add(link);
		second.add(link);

		TestUtils.assertEqualAndSameHashCode(first, second);
	}

	@Test
	void differentLinkListsNotEqual() {

		RepresentationModel<?> first = new RepresentationModel<>();
		RepresentationModel<?> second = new RepresentationModel<>();
		second.add(Link.of("foo"));

		TestUtils.assertNotEqualAndDifferentHashCode(first, second);
	}

	@Test
	@SuppressWarnings("rawtypes")
	void subclassNotEquals() {

		RepresentationModel<?> left = new RepresentationModel<>();
		RepresentationModel<?> right = new RepresentationModel() {

			@Override
			public int hashCode() {
				return super.hashCode() + 1;
			}

			@Override
			public String toString() {
				return super.toString() + "1";
			}
		};

		TestUtils.assertNotEqualAndDifferentHashCode(left, right);
	}

	/**
	 * @see #178
	 */
	@Test
	void doesNotEqualNull() {

		RepresentationModel<?> support = new RepresentationModel<>();
		assertThat(support.equals(null)).isFalse();
	}

	/**
	 * @see #267
	 */
	@Test
	void addsLinksViaVarargs() {

		RepresentationModel<?> support = new RepresentationModel<>();
		support.add(Link.of("/self", "self"), Link.of("/another", "another"));

		assertThat(support.hasLink("self")).isTrue();
		assertThat(support.hasLink("another")).isTrue();
	}

	@Test // #1014
	void addsGuardedLink() {

		RepresentationModel<?> model = new RepresentationModel<>();

		model.addIf(true, () -> Link.of("added", "foo"));
		assertThat(model.hasLink("foo")).isTrue();

		model.addIf(false, () -> Link.of("not-added", "bar"));
		assertThat(model.hasLink("bar")).isFalse();
	}

	@Test // #1014
	void addsGuardedLinks() {

		RepresentationModel<?> model = new RepresentationModel<>();

		model.addAllIf(true, () -> Links.of(Link.of("added", "foo")));
		assertThat(model.hasLink("foo")).isTrue();

		model.addAllIf(false, () -> Links.of(Link.of("not-added", "bar")));
		assertThat(model.hasLink("bar")).isFalse();
	}
}
