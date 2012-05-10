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
package org.springframework.hateoas;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

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
		assertThat(support.hasLinks(), is(false));
		assertThat(support.hasLink(Link.REL_SELF), is(false));
		assertThat(support.getLinks().isEmpty(), is(true));
	}

	@Test
	public void addsLinkCorrectly() {

		Link link = new Link("foo", Link.REL_NEXT);
		ResourceSupport support = new ResourceSupport();
		support.add(link);

		assertThat(support.getId(), is(nullValue()));
		assertThat(support.hasLinks(), is(true));
		assertThat(support.hasLink(link.getRel()), is(true));
		assertThat(support.getLink(link.getRel()), is(link));
	}

	@Test
	public void addsLinksCorrectly() {

		Link first = new Link("foo", Link.REL_PREVIOUS);
		Link second = new Link("bar", Link.REL_NEXT);

		ResourceSupport support = new ResourceSupport();
		support.add(Arrays.asList(first, second));

		assertThat(support.getId(), is(nullValue()));
		assertThat(support.hasLinks(), is(true));
		assertThat(support.getLinks(), hasItems(first, second));
		assertThat(support.getLinks().size(), is(2));
	}

	@Test
	public void selfLinkBecomesId() {

		Link link = new Link("foo");
		ResourceSupport support = new ResourceSupport();
		support.add(link);

		assertThat(support.getId(), is(link));
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

}
