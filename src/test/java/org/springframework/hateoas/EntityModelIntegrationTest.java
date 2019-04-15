/*
 * Copyright 2012-2018 the original author or authors.
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

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * Integration tests for {@link EntityModel}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class EntityModelIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final String REFERENCE = "{\"firstname\":\"Dave\",\"lastname\":\"Matthews\",\"links\":[{\"rel\":\"self\",\"href\":\"localhost\"}]}";

	@Test
	void inlinesContent() throws Exception {

		Person person = new Person();
		person.firstname = "Dave";
		person.lastname = "Matthews";

		EntityModel<Person> resource = new EntityModel<>(person);
		resource.add(new Link("localhost"));

		assertThat(write(resource)).isEqualTo(REFERENCE);
	}

	/**
	 * @see #14
	 */
	@Test
	void readsResourceSupportCorrectly() throws Exception {

		PersonModel result = read(REFERENCE, PersonModel.class);

		assertThat(result.getLinks()).hasSize(1);
		assertThat(result.getLinks()).contains(new Link("localhost"));
		assertThat(result.getContent().firstname).isEqualTo("Dave");
		assertThat(result.getContent().lastname).isEqualTo("Matthews");
	}

	static class PersonModel extends EntityModel<Person> {

		public PersonModel(Person person) {
			super(person);
		}

		protected PersonModel() {}
	}

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	static class Person {

		String firstname;
		String lastname;
	}
}
