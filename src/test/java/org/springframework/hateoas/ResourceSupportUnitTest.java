/*
 * Copyright 2012-2015 the original author or authors.
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

import java.util.Arrays;

import org.junit.Test;

/**
 * Unit tests for {@link ResourceSupport}.
 * 
 * @author Oliver Gierke
 */
public class ResourceSupportUnitTest {

	@Test
	public void setsUpWithEmptyLinkList() {

		ResourceSupport support = new ResourceSupport();
		assertThat(support.hasLinks()).isFalse();
		assertThat(support.hasLink(Link.REL_SELF)).isFalse();
		assertThat(support.getLinks().isEmpty()).isTrue();
		assertThat(support.getLinks(Link.REL_SELF).isEmpty()).isTrue();
	}

	@Test
	public void addsLinkCorrectly() {

		Link link = new Link("foo", Link.REL_NEXT);
		ResourceSupport support = new ResourceSupport();
		support.add(link);

		assertThat(support.getId()).isEmpty();
		assertThat(support.hasLinks()).isTrue();
		assertThat(support.hasLink(link.getRel())).isTrue();
		assertThat(support.getLink(link.getRel())).hasValue(link);
		assertThat(support.getLinks(Link.REL_NEXT)).contains(link);
	}

	@Test
	public void addsMultipleLinkRelationsCorrectly() {

		Link link = new Link("/customers/1", "customers");
		Link link2 = new Link("/orders/1/customer", "customers");
		ResourceSupport support = new ResourceSupport();
		support.add(link, link2);

		assertThat(support.getLinks("customers")).hasSize(2);
		assertThat(support.getLinks("customers")).contains(link, link2);
		assertThat(support.getLinks("non-existent")).hasSize(0);
		assertThat(support.getLinks("non-existent")).isEmpty();
	}

	@Test
	public void addsLinksCorrectly() {

		Link first = new Link("foo", Link.REL_PREVIOUS);
		Link second = new Link("bar", Link.REL_NEXT);

		ResourceSupport support = new ResourceSupport();
		support.add(Arrays.asList(first, second));

		assertThat(support.getId()).isEmpty();
		assertThat(support.hasLinks()).isTrue();
		assertThat(support.getLinks()).contains(first, second);
		assertThat(support.getLinks()).hasSize(2);
		assertThat(support.getLinks(Link.REL_PREVIOUS)).contains(first);
		assertThat(support.getLinks(Link.REL_NEXT)).contains(second);
	}

	@Test
	public void selfLinkBecomesId() {

		Link link = new Link("foo");
		ResourceSupport support = new ResourceSupport();
		support.add(link);

		assertThat(support.getId()).hasValue(link);
	}

	@Test(expected = IllegalArgumentException.class)
	public void preventsNullLinkBeingAdded() {

		ResourceSupport support = new ResourceSupport();
		support.add((Link) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void preventsNullLinksBeingAdded() {
		ResourceSupport support = new ResourceSupport();
		support.add((Iterable<Link>) null);
	}

	@Test
	public void sameLinkListMeansSameResource() {

		ResourceSupport first = new ResourceSupport();
		ResourceSupport second = new ResourceSupport();

		TestUtils.assertEqualAndSameHashCode(first, second);

		Link link = new Link("foo");
		first.add(link);
		second.add(link);

		TestUtils.assertEqualAndSameHashCode(first, second);
	}

	@Test
	public void differentLinkListsNotEqual() {

		ResourceSupport first = new ResourceSupport();
		ResourceSupport second = new ResourceSupport();
		second.add(new Link("foo"));

		TestUtils.assertNotEqualAndDifferentHashCode(first, second);
	}

	@Test
	public void subclassNotEquals() {

		ResourceSupport left = new ResourceSupport();
		ResourceSupport right = new ResourceSupport() {

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
	public void doesNotEqualNull() {

		ResourceSupport support = new ResourceSupport();
		assertThat(support.equals(null)).isFalse();
	}

	/**
	 * @see #267
	 */
	@Test
	public void addsLinksViaVarargs() {

		ResourceSupport support = new ResourceSupport();
		support.add(new Link("/self", "self"), new Link("/another", "another"));

		assertThat(support.hasLink("self")).isTrue();
		assertThat(support.hasLink("another")).isTrue();
	}
}
