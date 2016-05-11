/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.affordance;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class AffordanceTest {

	@Test
	public void testConstructorWithoutRels() {
		final Affordance affordance = new Affordance("http://localhost/things/{id}");
		Assert.assertEquals("http://localhost/things/{id}", affordance.getHref());
		Assert.assertNull("rel must be null", affordance.getRel());
		Assert.assertEquals(0, affordance.getRels()
				.size());
		Assert.assertThat(affordance.getRels(), Matchers.is(Matchers.empty()));
	}

	@Test
	public void testConstructorWithSingleRel() {
		final Affordance affordance = new Affordance("http://localhost/things/{id}", "thing");
		Assert.assertEquals("http://localhost/things/{id}", affordance.getHref());
		Assert.assertEquals("thing", affordance.getRel());
		Assert.assertThat(affordance.getRels(), Matchers.contains("thing"));
	}

	@Test
	public void testConstructorWithRels() {
		final Affordance affordance = new Affordance("http://localhost/things/{id}",
				"start", "http://example.net/relation/other");
		Assert.assertEquals("http://localhost/things/{id}", affordance.getHref());
		Assert.assertEquals("start", affordance.getRel());
		Assert.assertThat(affordance.getRels(), Matchers.contains("start", "http://example.net/relation/other"));
	}

	@Test
	public void testIsTemplated() {
		final Affordance affordance = new Affordance("http://localhost/things/{id}", "thing");
		Assert.assertEquals("http://localhost/things/{id}", affordance.getHref());
		Assert.assertTrue("must recognize template", affordance.isTemplated());
	}

	@Test
	public void testGetVariables() {
		final Affordance affordance = new Affordance("http://localhost/things/{id}", "thing");
		Assert.assertThat(affordance.getVariableNames(), Matchers.contains("id"));
	}

	@Test
	public void testExpand() {
		final Affordance affordance = new Affordance("http://localhost/things{/id}", "thing");
		Assert.assertEquals("http://localhost/things/100", affordance.expand(100).getHref());
	}

	@Test
	public void testExpandWithArgumentsMap() {
		final Affordance affordance = new Affordance("http://localhost/things{?id}", "thing");

		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("id", 101);

		Assert.assertEquals("http://localhost/things?id=101", affordance.expand(101).getHref());
	}
}