package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Integration tests for {@link EntityModel}.
 *
 * @author Oliver Gierke
 * @author Jon Brisbin
 */
class Jackson2ResourceIntegrationTest {

	static final String REFERENCE = "{\"firstname\":\"Dave\",\"lastname\":\"Matthews\",\"links\":[{\"rel\":\"self\",\"href\":\"localhost\"}]}";

	ContextualMapper mapper = MappingTestUtils.createMapper();

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

		mapper.assertSerializes(resource).into(REFERENCE);
	}

	/**
	 * @see #27
	 */
	@Test
	void readsResourceSupportCorrectly() throws Exception {

		mapper.assertDeserializes(REFERENCE)
				.into(PersonResource.class)
				.matching(result -> {

					assertThat(result.getLinks()).hasSize(1);
					assertThat(result.getLinks()).contains(Link.of("localhost"));
					assertThat(result.getContent().firstname).isEqualTo("Dave");
					assertThat(result.getContent().lastname).isEqualTo("Matthews");
				});
	}

	static class PersonResource extends EntityModel<Person> {

		public PersonResource() {}
	}

	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	static class Person {
		@Nullable String firstname, lastname;
	}
}
