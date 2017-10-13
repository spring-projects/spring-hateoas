/*
 * Copyright 2014-2016 the original author or authors.
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
package org.springframework.hateoas.alps;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.alps.Alps.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Unit tests for serialization of ALPS documents.
 * 
 * @author Oliver Gierke
 */
public class JacksonSerializationTest {

	ObjectMapper mapper;

	@Before
	public void setUp() {

		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	/**
	 * @see #141
	 */
	@Test
	public void writesSampleDocument() throws Exception {

		Alps alps = alps().//
				doc(doc().href("http://example.org/samples/full/doc.html").build()). //
				descriptors(Arrays.asList(//
						descriptor().id("search").type(Type.SAFE).//
								doc(new Doc("A search form with two inputs.", Format.TEXT)).//
								descriptors(Arrays.asList( //
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

		Scanner scanner = null;

		try {

			scanner = new Scanner(resource.getInputStream());
			StringBuilder builder = new StringBuilder();

			while (scanner.hasNextLine()) {

				builder.append(scanner.nextLine());

				if (scanner.hasNextLine()) {
					builder.append(System.getProperty("line.separator"));
				}
			}

			return builder.toString();

		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}
}
