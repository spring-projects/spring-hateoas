package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Integration tests for {@link EntityModel}.
 * 
 * @author Oliver Gierke
 * @author Jon Brisbin
 */
class Jackson2ResourceIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final String REFERENCE = "{\"firstname\":\"Dave\",\"lastname\":\"Matthews\",\"links\":[{\"rel\":\"self\",\"href\":\"localhost\"}]}";

	/**
	 * @see #27
	 * @throws Exception
	 */
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
	 * @see #27
	 */
	@Test
	void readsResourceSupportCorrectly() throws Exception {

		PersonResource result = read(REFERENCE, PersonResource.class);

		assertThat(result.getLinks()).hasSize(1);
		assertThat(result.getLinks()).contains(new Link("localhost"));
		assertThat(result.getContent().firstname).isEqualTo("Dave");
		assertThat(result.getContent().lastname).isEqualTo("Matthews");
	}

	static class PersonResource extends EntityModel<Person> {

		public PersonResource() {

		}
	}

	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	static class Person {

		String firstname;
		String lastname;
	}

}
