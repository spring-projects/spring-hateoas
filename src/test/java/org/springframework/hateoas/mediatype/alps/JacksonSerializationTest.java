/*
 * Copyright 2014-2024 the original author or authors.
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
package org.springframework.hateoas.mediatype.alps;

import static org.springframework.hateoas.mediatype.alps.Alps.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;

/**
 * Unit tests for serialization of ALPS documents.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class JacksonSerializationTest {

	ContextualMapper $ = MappingTestUtils.createMapper();

	/**
	 * @see #141
	 */
	@Test
	void writesSampleDocument() throws Exception {

		var alps = alps().//
				doc(doc().href("https://example.org/samples/full/doc.html").build()). //
				descriptor(List.of(//
						descriptor().id("search").type(Type.SAFE).//
								doc(new Doc("A search form with two inputs.", Format.TEXT)).//
								descriptor(Arrays.asList( //
										descriptor().href("#resultType").build(), //
										descriptor().id("value").name("search").type(Type.SEMANTIC).build())//
								).build(), //
						descriptor().id("resultType").type(Type.SEMANTIC).//
								doc(doc().value("results format").build()).//
								ext(ext().id("#ext-range").href("http://alps.io/ext/range").value("summary,detail").build()//
								).build())//
				).build();

		$.assertSerializes(alps).intoContentOf("reference.json");
	}
}
