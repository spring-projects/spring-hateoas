/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.hateoas.hal;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.core.EvoInflectorRelProvider;

/**
 * Unit tests for {@link HalEmbeddedBuilder}.
 * 
 * @author Oliver Gierke
 */
public class HalEmbeddedBuilderUnitTests {

	RelProvider provider;

	@Before
	public void setUp() {
		provider = new EvoInflectorRelProvider();
	}

	@Test
	public void rendersSingleElementsWithSingleEntityRel() {

		Map<String, List<Object>> map = setUpBuilder("foo", 1L);

		assertThat(map.get("string"), Matchers.<List<Object>> allOf(hasSize(1), hasItem("foo")));
		assertThat(map.get("long"), Matchers.<List<Object>> allOf(hasSize(1), hasItem(1L)));
	}

	@Test
	public void rendersMultipleElementsWithCollectionResourceRel() {

		Map<String, List<Object>> map = setUpBuilder("foo", "bar", 1L);

		assertThat(map.containsKey("string"), is(false));
		assertThat(map.get("strings"), Matchers.<List<Object>> allOf(hasSize(2), Matchers.<Object> hasItems("foo", "bar")));
		assertThat(map.get("long"), Matchers.<List<Object>> allOf(hasSize(1), hasItem(1L)));
	}

	@Test
	public void addsNoEmbeddedsForResourceWithoutContent() {

		class SimplePojoResource extends Resource<SimplePojo> {
		}
		SimplePojoResource samplePersonResource = new SimplePojoResource();
		HalEmbeddedBuilder halEmbeddedBuilder = new HalEmbeddedBuilder(provider);
		halEmbeddedBuilder.add(samplePersonResource);
		assertTrue("embeddeds should be empty", halEmbeddedBuilder.asMap().isEmpty());
	}

	private Map<String, List<Object>> setUpBuilder(Object... values) {

		HalEmbeddedBuilder builder = new HalEmbeddedBuilder(provider);

		for (Object value : values) {
			builder.add(value);
		}

		return builder.asMap();
	}
}
