/*
 * Copyright 2015 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.support.MappingUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 */
class JacksonSerializationTest {

	ObjectMapper mapper;

	@BeforeEach
	void setUp() {

		mapper = new ObjectMapper();
		mapper.registerModule(new Jackson2CollectionJsonModule());
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@Test
	void createSimpleCollection() throws IOException {

		CollectionJson<?> collection = new CollectionJson<>().withVersion("1.0").withHref("localhost")
				.withLinks(Links.of(new Link("foo").withSelfRel())) //
				.withItems(new CollectionJsonItem<>() //
						.withHref("localhost") //
						.withRawData("Greetings programs") //
						.withLinks(new Link("localhost").withSelfRel()), //
						new CollectionJsonItem<>() //
								.withHref("localhost") //
								.withRawData("Yo") //
								.withLinks(new Link("localhost/orders").withRel("orders")));

		String actual = mapper.writeValueAsString(collection);

		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("reference.json", getClass())));
	}
}
