/*
 * Copyright 2012-2024 the original author or authors.
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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.SerializationFeature;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * Integration tests for {@link EntityModel}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class EntityModelIntegrationTest {

	static final String REFERENCE = "{\"firstname\":\"Dave\",\"lastname\":\"Matthews\",\"links\":[{\"rel\":\"self\",\"href\":\"localhost\"}]}";

	ContextualMapper $ = MappingTestUtils.createMapper();

	@Test
	void inlinesContent() throws Exception {

		var person = new Person();
		person.firstname = "Dave";
		person.lastname = "Matthews";

		var model = EntityModel.of(person)
				.add(Link.of("localhost"));

		$.assertSerializes(model)
				.into(REFERENCE)
				.andBack(Person.class);
	}

	/**
	 * @see #14
	 */
	@Test
	void readsResourceSupportCorrectly() throws Exception {

		$.assertDeserializes(REFERENCE)
				.into(PersonModel.class)
				.matching(result -> {

					assertThat(result.getLinks()).hasSize(1);
					assertThat(result.getLinks()).contains(Link.of("localhost"));
					assertThat(result.getContent().firstname).isEqualTo("Dave");
					assertThat(result.getContent().lastname).isEqualTo("Matthews");
				});
	}

	@Test // #1686
	void doesNotFailOnSerializingEmptyBean() {

		var mapper = MappingTestUtils.defaultJsonMapper();

		// Fail if we're supposed to
		assertThatExceptionOfType(DatabindException.class) //
				.isThrownBy(() -> mapper
						.rebuild()
						.enable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
						.build()
						.writeValueAsString(EntityModel.of(new Empty())));

		// Ignore empty bean if we're supposed to
		assertThatNoException() //
				.isThrownBy(() -> mapper.rebuild()
						.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
						.build()
						.writeValueAsString(EntityModel.of(new Empty())));
	}

	static class PersonModel extends EntityModel<Person> {

		public PersonModel(Person person) {
			super(person);
		}

		protected PersonModel() {}
	}

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	static class Person {
		@Nullable String firstname, lastname;
	}

	static class Empty {}
}
