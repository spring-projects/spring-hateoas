/*
 * Copyright 2012 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.TestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link IdentifiableResourceAssemblerSupport}.
 * 
 * @author Oliver Gierke
 */
public class IdentifiableResourceAssemblerSupportUnitTest extends TestUtils {

	PersonResourceAssembler<Long> assemblerForLongId = new PersonResourceAssembler<Long>();
	PersonResourceAssembler<String> assemblerForStringId = new PersonResourceAssembler<String>();
	Person<Long> personWithLongId;
	Person<String> personWithStringId;

	@Override
	@Before
	public void setUp() {
		super.setUp();
		this.personWithLongId = new Person<Long>();
		this.personWithLongId.id = 10L;
		this.personWithLongId.alternateId = "id";

		this.personWithStringId = new Person<String>();
		this.personWithStringId.id = "firstname lastname";
		this.personWithStringId.alternateId = "with blank";
	}

	@Test
	public void createsInstanceWithSelfLinkToController() {

		PersonResource resource = assemblerForLongId.createResource(personWithLongId);
		Link link = resource.getLink(Link.REL_SELF);

		assertThat(link, is(notNullValue()));
		assertThat(resource.getLinks().size(), is(1));
	}

	@Test
	public void usesAlternateIdIfGivenExplicitly() {

		PersonResource resource = assemblerForLongId.createResourceWithId(personWithLongId.alternateId, personWithLongId);
		Link selfLink = resource.getId();
		assertThat(selfLink.getHref(), endsWith("/people/id"));
	}

	@Test
	public void usesAlternateIdIfGivenExplicitlyWithEncoding() {

		PersonResource resource = assemblerForStringId.createResourceWithId(personWithStringId.alternateId, personWithStringId);
		Link selfLink = resource.getId();
		assertThat(selfLink.getHref(), endsWith("/people/with%20blank"));
	}

	@Test
	public void unwrapsIdentifyablesForParameters() {

		PersonResource resource = new PersonResourceAssembler<Long>(ParameterizedController.class).createResource(personWithLongId, personWithLongId,
				"bar");
		Link selfLink = resource.getId();
		assertThat(selfLink.getHref(), endsWith("/people/10/bar/addresses/10"));
	}

	@Test
	public void unwrapsIdentifyablesForParametersWithEncoding() {

		PersonResource resource = new PersonResourceAssembler<String>(ParameterizedController.class).createResource(personWithStringId, personWithStringId,
				"bar");
		Link selfLink = resource.getId();
		assertThat(selfLink.getHref(), endsWith("/people/firstname%20lastname/bar/addresses/firstname%20lastname"));
	}

	@Test
	public void convertsEntitiesToResources() {

		Person<Long> first = new Person<Long>();
		first.id = 1L;
		Person<Long> second = new Person<Long>();
		second.id = 2L;

		List<PersonResource> result = assemblerForLongId.toResources(Arrays.asList(first, second));

		LinkBuilder builder = linkTo(PersonController.class);

		PersonResource firstResource = new PersonResource();
		firstResource.add(builder.slash(1L).withSelfRel());

		PersonResource secondResource = new PersonResource();
		secondResource.add(builder.slash(1L).withSelfRel());

		assertThat(result.size(), is(2));
		assertThat(result, hasItems(firstResource, secondResource));
	}

	@RequestMapping("/people")
	static class PersonController {

	}

	@RequestMapping("/people/{id}/{foo}/addresses")
	static class ParameterizedController {

	}

	static class Person<ID extends Serializable> implements Identifiable<ID> {

		ID id;
		String alternateId;

		@Override
		public ID getId() {
			return id;
		}
	}

	static class PersonResource extends ResourceSupport {

	}

	class PersonResourceAssembler<ID extends Serializable> extends IdentifiableResourceAssemblerSupport<Person<ID>, PersonResource> {

		public PersonResourceAssembler() {
			this(PersonController.class);
		}

		public PersonResourceAssembler(Class<?> controllerType) {
			super(controllerType, PersonResource.class);
		}

		@Override
		public PersonResource toResource(Person entity) {
			return createResource(entity);
		}
	}
}
