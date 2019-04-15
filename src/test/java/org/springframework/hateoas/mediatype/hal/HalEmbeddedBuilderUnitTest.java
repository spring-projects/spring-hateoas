/*
 * Copyright 2013-2019 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.mediatype.hal.HalLinkRelation.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.EmbeddedWrapper;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;

/**
 * Unit tests for {@link HalEmbeddedBuilder}.
 *
 * @author Oliver Gierke
 * @author Dietrich Schulten
 */
class HalEmbeddedBuilderUnitTest {

	LinkRelationProvider provider;
	CurieProvider curieProvider;

	@BeforeEach
	void setUp() {
		provider = new EvoInflectorLinkRelationProvider();
		curieProvider = new DefaultCurieProvider("curie", UriTemplate.of("http://localhost/{rel}"));
	}

	@Test
	void rendersSingleElementsWithSingleEntityRel() {

		Map<HalLinkRelation, Object> map = setUpBuilder(CurieProvider.NONE, "foo", 1L);

		assertThat(map.get(uncuried("string"))).isEqualTo("foo");
		assertThat(map.get(uncuried("long"))).isEqualTo(1L);
	}

	@Test
	void rendersMultipleElementsWithCollectionResourceRel() {

		Map<HalLinkRelation, Object> map = setUpBuilder(CurieProvider.NONE, "foo", "bar", 1L);

		assertThat(map.containsKey(uncuried("string"))).isFalse();
		assertThat(map.get(uncuried("long"))).isEqualTo(1L);
		assertHasValues(map, uncuried("strings"), "foo", "bar");
	}

	/**
	 * @see #110
	 */
	@Test
	void correctlyPilesUpResourcesInCollectionRel() {

		Map<HalLinkRelation, Object> map = setUpBuilder(CurieProvider.NONE, "foo", "bar", "foobar", 1L);

		assertThat(map.containsKey(uncuried("string"))).isFalse();
		assertHasValues(map, uncuried("strings"), "foo", "bar", "foobar");
		assertThat(map.get(uncuried("long"))).isEqualTo(1L);
	}

	/**
	 * @see #135
	 */
	@Test
	void forcesCollectionRelToBeUsedIfConfigured() {

		HalEmbeddedBuilder builder = new HalEmbeddedBuilder(provider, CurieProvider.NONE, true);
		builder.add("Sample");

		assertThat(builder.asMap().get(uncuried("string"))).isNull();
		assertHasValues(builder.asMap(), uncuried("strings"), "Sample");
	}

	/**
	 * @see #195
	 */
	@Test
	void doesNotPreferCollectionsIfRelAwareWasAdded() {

		EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

		HalEmbeddedBuilder builder = new HalEmbeddedBuilder(provider, CurieProvider.NONE, true);
		builder.add(wrappers.wrap("MyValue", LinkRelation.of("foo")));

		assertThat(builder.asMap().get(uncuried("foo"))).isInstanceOf(String.class);
	}

	/**
	 * @see #195
	 */
	@Test
	@SuppressWarnings("null")
	void rejectsNullRelProvider() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new HalEmbeddedBuilder(null, CurieProvider.NONE, false);
		});
	}

	/**
	 * @see #229
	 */
	@Test
	void rendersSingleElementsWithSingleEntityRelWithCurieProvider() {

		Map<HalLinkRelation, Object> map = setUpBuilder(curieProvider, "foo", 1L);

		assertThat(map.get(curied("curie", "string"))).isEqualTo("foo");
		assertThat(map.get(curied("curie", "long"))).isEqualTo(1L);
	}

	/**
	 * @see #229
	 */
	@Test
	void rendersMultipleElementsWithCollectionResourceRelWithCurieProvider() {

		Map<HalLinkRelation, Object> map = setUpBuilder(curieProvider, "foo", "bar", 1L);

		assertThat(map.containsKey(curied("curie", "string"))).isFalse();
		assertThat(map.get(curied("curie", "long"))).isEqualTo(1L);
		assertHasValues(map, curied("curie", "strings"), "foo", "bar");
	}

	/**
	 * @see #229
	 */
	@Test
	void correctlyPilesUpResourcesInCollectionRelWithCurieprovider() {

		Map<HalLinkRelation, Object> map = setUpBuilder(curieProvider, "foo", "bar", "foobar", 1L);

		assertThat(map.containsKey(curied("curie", "string"))).isFalse();
		assertHasValues(map, curied("curie", "strings"), "foo", "bar", "foobar");
		assertThat(map.get(curied("curie", "long"))).isEqualTo(1L);
	}

	/**
	 * @see #229
	 */
	@Test
	void forcesCollectionRelToBeUsedIfConfiguredWithCurieProvider() {

		HalEmbeddedBuilder builder = new HalEmbeddedBuilder(provider, curieProvider, true);
		builder.add("Sample");

		assertThat(builder.asMap().get(curied("curie", "string"))).isNull();
		assertHasValues(builder.asMap(), curied("curie", "strings"), "Sample");
	}

	/**
	 * @see #286
	 */
	@Test
	void rejectsInvalidEmbeddedWrapper() {

		HalEmbeddedBuilder builder = new HalEmbeddedBuilder(provider, curieProvider, false);

		assertThatIllegalStateException().isThrownBy(() -> {
			builder.add(mock(EmbeddedWrapper.class));
		});
	}

	@SuppressWarnings("unchecked")
	private static void assertHasValues(Map<HalLinkRelation, Object> source, HalLinkRelation rel, Object... values) {

		Object value = source.get(rel);

		assertThat(value).isInstanceOfSatisfying(List.class, it -> {
			assertThat(it).hasSize(values.length);
			assertThat(it).contains(values);
		});
	}

	private Map<HalLinkRelation, Object> setUpBuilder(CurieProvider curieProvider, Object... values) {

		HalEmbeddedBuilder builder = new HalEmbeddedBuilder(provider, curieProvider, false);

		for (Object value : values) {
			builder.add(value);
		}

		return builder.asMap();
	}
}
