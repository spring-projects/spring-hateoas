/*
 * Copyright 2014-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.mediatype.alps.Alps.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Unit tests for serialization of ALPS documents.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class JacksonSerializationTest {

	ObjectMapper mapper;

	@BeforeEach
	void setUp() {

		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	/**
	 * @see #141
	 */
	@Test
	void writesSampleDocument() throws Exception {

		Alps alps = alps().//
				doc(doc().href("https://example.org/samples/full/doc.html").build()). //
				descriptor(Arrays.asList(//
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

		assertThat(mapper.writeValueAsString(alps)).isEqualTo(read(new ClassPathResource("reference.json", getClass())));
	}

	private static String read(Resource resource) throws IOException {

		try (Scanner scanner = new Scanner(resource.getInputStream())) {

			StringBuilder builder = new StringBuilder();

			while (scanner.hasNextLine()) {

				builder.append(scanner.nextLine());

				if (scanner.hasNextLine()) {
					builder.append(System.getProperty("line.separator"));
				}
			}

			return builder.toString();
		}
	}
}
