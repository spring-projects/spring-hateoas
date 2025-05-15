package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Integration tests for {@link EntityModel}.
 *
 * @author Oliver Gierke
 * @author Jon Brisbin
 */
class Jackson2ResourceIntegrationTest {

	static final String REFERENCE = "{\"firstname\":\"Dave\",\"lastname\":\"Matthews\",\"links\":[{\"rel\":\"self\",\"href\":\"localhost\"}]}";

	ObjectMapper mapper = MappingTestUtils.defaultObjectMapper();

	/**
	 * @see #27
	 * @throws Exception
	 */
	@Test
	void inlinesContent() throws Exception {

		var person = new Person();
		person.firstname = "Dave";
		person.lastname = "Matthews";

		var resource = EntityModel.of(person)
				.add(Link.of("localhost"));

		assertThat(mapper.writeValueAsString(resource)).isEqualTo(REFERENCE);
	}

	/**
	 * @see #27
	 */
	@Test
	void readsResourceSupportCorrectly() throws Exception {

		var result = mapper.readValue(REFERENCE, PersonResource.class);

		assertThat(result.getLinks()).hasSize(1);
		assertThat(result.getLinks()).contains(Link.of("localhost"));
		assertThat(result.getContent().firstname).isEqualTo("Dave");
		assertThat(result.getContent().lastname).isEqualTo("Matthews");
	}

	static class PersonResource extends EntityModel<Person> {

		public PersonResource() {}
	}

	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	static class Person {

		String firstname;
		String lastname;
	}
}
