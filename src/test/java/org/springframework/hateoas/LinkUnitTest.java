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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Unit tests for {@link Link}.
 *
 * @author Oliver Gierke
 */
public class LinkUnitTest {

	@Test
	public void linkWithHrefOnlyBecomesSelfLink() {
		Link link = new Link("foo");
		assertThat(link.getRel(), is(Link.REL_SELF));
	}

	@Test
	public void createsLinkFromRelAndHref() {
		Link link = new Link("foo", Link.REL_SELF);
		assertThat(link.getHref(), is("foo"));
		assertThat(link.getRel(), is(Link.REL_SELF));
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
    public void acceptsNullMethod() throws Exception {
        new Link("href", "rel", null);
    }

    @Test
	public void sameRelAndHrefMakeSameLink() {

		Link left = new Link("foo", Link.REL_SELF);
		Link right = new Link("foo", Link.REL_SELF);

		TestUtils.assertEqualAndSameHashCode(left, right);
	}

    @Test
	public void sameRelAndHrefAndMethodMakeSameLink() {

		Link left = new Link("foo", Link.REL_SELF, HttpMethod.DELETE);
		Link right = new Link("foo", Link.REL_SELF, HttpMethod.DELETE);

		TestUtils.assertEqualAndSameHashCode(left, right);
	}

	@Test
	public void differentRelMakesDifferentLink() {

		Link left = new Link("foo", Link.REL_PREVIOUS, HttpMethod.GET);
		Link right = new Link("foo", Link.REL_NEXT, HttpMethod.GET);

		TestUtils.assertNotEqualAndDifferentHashCode(left, right);
	}

	@Test
	public void differentHrefMakesDifferentLink() {

		Link left = new Link("foo", Link.REL_SELF, HttpMethod.GET);
		Link right = new Link("bar", Link.REL_SELF, HttpMethod.GET);

		TestUtils.assertNotEqualAndDifferentHashCode(left, right);
	}

    @Test
	public void differentMethodMakesDifferentLink() {

		Link left = new Link("foo", Link.REL_SELF, HttpMethod.GET);
		Link right = new Link("foo", Link.REL_SELF, HttpMethod.PUT);

		TestUtils.assertNotEqualAndDifferentHashCode(left, right);
	}

	@Test
	public void differentTypeDoesNotEqual() {
		assertThat(new Link("foo"), is(not((Object) new ResourceSupport())));
	}
}
