package org.springframework.hateoas;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ResourcesIntegrationTest extends AbstractMarshallingIntegrationTests {

	private final static String RESOURCES = "{\"links\":[{\"rel\":\"self\",\"href\":\"location1\"}],\"content\":[{\"links\":[{\"rel\":\"self\",\"href\":\"location2\"}]}]}";

	public static class Person extends ResourceSupport {

		public Person() {
			super();
		}

		public Person(Link link) {
			super();
			add(link);
		}
		
	}

	public static class People extends Resources<Person> {
		
		public People() {
			
		}

		public People(Person person, Link link) {
			super(list(person), link);
		}
		
		private static List<Person> list(Person person) {
			List<Person> list = new ArrayList<Person>();
			list.add(person);
			return list;
		}
	}

	@Test
	public void writesResourcesCorrectly() throws Exception {
		assertThat(write(new People(new Person(new Link("location2")), new Link("location1"))), is(RESOURCES));
	}

	@Test
	public void readsLinkCorrectly() throws Exception {
		People result = read(RESOURCES, People.class);
		assertThat(result.getLink(Link.REL_SELF).getHref(), is("location1"));
	}

	@Test
	public void readsContentCorrectly() throws Exception {
		People result = read(RESOURCES, People.class);
		assertThat(result.getContent(), hasItem(new Person(new Link("location2"))));
	}

}
