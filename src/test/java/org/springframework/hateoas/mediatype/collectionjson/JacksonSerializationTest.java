/*
 * Copyright 2015-2024 the original author or authors.
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

import tools.jackson.databind.SerializationFeature;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;

/**
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
class JacksonSerializationTest {

	ContextualMapper $ = MappingTestUtils.createMapper(
			it -> it.addModule(new CollectionJsonJacksonModule()).enable(SerializationFeature.INDENT_OUTPUT));

	@Test
	void createSimpleCollection() throws IOException {

		var collection = new CollectionJson<>()
				.withVersion("1.0")
				.withHref("localhost")
				.withLinks(Links.of(Link.of("foo").withSelfRel())) //
				.withItems(new CollectionJsonItem<>() //
						.withHref("localhost") //
						.withRawData("Greetings programs") //
						.withLinks(Link.of("localhost").withSelfRel()), //
						new CollectionJsonItem<>() //
								.withHref("localhost") //
								.withRawData("Yo") //
								.withLinks(Link.of("localhost/orders").withRel("orders")));

		$.assertSerializes(collection).intoContentOf("reference.json");
	}
}
