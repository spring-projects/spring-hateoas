/*
 * Copyright 2012-2014 the original author or authors.
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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * Integration tests for {@link Resource}.
 * 
 * @author Oliver Gierke
 */
public class ResourceIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final String REFERENCE = "{\"firstname\":\"Dave\",\"lastname\":\"Matthews\",\"links\":[{\"rel\":\"self\",\"href\":\"localhost\"}]}";
	static final String XML_REFERENCE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><personResource xmlns:ns2=\"http://www.w3.org/2005/Atom\"><ns2:link href=\"/foo\" rel=\"bar\"/><person lastname=\"Matthews\" firstname=\"Dave\"/></personResource>";

	@Test
	public void inlinesContent() throws Exception {

		Person person = new Person();
		person.firstname = "Dave";
		person.lastname = "Matthews";

		Resource<Person> resource = new Resource<Person>(person);
		resource.add(new Link("localhost"));

		assertThat(write(resource)).isEqualTo(REFERENCE);
	}

	/**
	 * @see #124
	 * @see #154
	 */
	@Test
	public void marshalsResourceToXml() throws Exception {

		Person person = new Person();
		person.firstname = "Dave";
		person.lastname = "Matthews";

		PersonResource resource = new PersonResource(person);
		resource.add(new Link("/foo", "bar"));

		JAXBContext context = JAXBContext.newInstance(PersonResource.class, Person.class);
		StringWriter writer = new StringWriter();

		Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(resource, writer);

		assertThat(new Diff(XML_REFERENCE, writer.toString()).similar()).isTrue();
	}

	/**
	 * @see #14
	 */
	@Test
	public void readsResourceSupportCorrectly() throws Exception {

		PersonResource result = read(REFERENCE, PersonResource.class);

		assertThat(result.getLinks()).hasSize(1);
		assertThat(result.getLinks()).contains(new Link("localhost"));
		assertThat(result.getContent().firstname).isEqualTo("Dave");
		assertThat(result.getContent().lastname).isEqualTo("Matthews");
	}

	@XmlRootElement
	static class PersonResource extends Resource<Person> {

		public PersonResource(Person person) {
			super(person);
		}

		protected PersonResource() {}
	}

	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	@XmlRootElement
	static class Person {

		@XmlAttribute String firstname;
		@XmlAttribute String lastname;
	}
}
