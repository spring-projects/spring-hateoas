/*
 * Copyright 2026 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.SimplePojo;

/**
 * Unit tests for the merging of the links of Collection+JSON items during deserialization.
 *
 * @author Oliver Drotbohm
 */
class CollectionJsonLinkMergeUnitTest {

	ContextualMapper $ = MappingTestUtils.createMapper(mapper -> mapper.addModule(new CollectionJsonJacksonModule()));

	@Test // GH-2589
	void keepsLinksWithDistinctRelations() {

		var result = $.readObject(items(3, i -> "r" + i, i -> "h" + i));

		assertThat(result.getLinks().stream().map(it -> it.getRel().value()))
				.containsExactly("self", "r0", "r1", "r2");
	}

	@Test // GH-2589
	void skipsEqualLinksOnRepresentationModel() {

		var result = $.readObject(items(3, i -> "r", i -> "h"));

		assertThat(result.getLinks().stream().map(it -> it.getHref())).containsExactly("/items/0", "h");
	}

	@Test // GH-2589
	void keepsLinksWithSameRelationButDifferentHrefOnRepresentationModel() {

		var result = $.readObject(items(3, i -> "r", i -> "h" + i));

		assertThat(result.getLinks().stream().map(it -> it.getHref())).containsExactly("/items/0", "h0", "h1", "h2");
	}

	@Test // GH-2589
	void replacesLinksWithSameRelationOnEntityModel() {

		var result = $.readEntityModel(items(3, i -> "r", i -> "h" + i), SimplePojo.class);

		assertThat(result.getLinks().stream().map(it -> it.getHref())).containsExactly("/items/0", "h0");
	}

	@Test // GH-2589
	void mergesLargeNumbersOfItemsInLinearTime() {

		var count = 100_000;
		var json = items(count, i -> "r" + i, i -> "h" + i);

		assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {

			assertThat($.readObject(json).getLinks()).hasSize(count + 1);
			assertThat($.readEntityModel(json, SimplePojo.class).getLinks()).hasSize(count + 1);
		});
	}

	private static String items(int count, IntFunction<String> rels, IntFunction<String> hrefs) {

		var items = IntStream.range(0, count) //
				.mapToObj(i -> """
						{"href":"/items/%d","data":[{"name":"text","value":"x"},{"name":"number","value":%d}],\
						"links":[{"rel":"%s","href":"%s"}]}""".formatted(i, i, rels.apply(i), hrefs.apply(i))) //
				.collect(Collectors.joining(","));

		return "{\"collection\":{\"version\":\"1.0\",\"href\":\"/items\",\"items\":[" + items + "]}}";
	}
}
