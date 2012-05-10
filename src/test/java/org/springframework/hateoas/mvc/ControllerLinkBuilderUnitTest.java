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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author Oliver Gierke
 */
public class ControllerLinkBuilderUnitTest extends TestUtils {

	@Test
	public void createsLinkToControllerRoot() {

		Link link = linkTo(PersonController.class).withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), Matchers.endsWith("/people"));
	}

	@Test
	public void createsLinkToParameterizedControllerRoot() {

		Link link = linkTo(PersonsAddressesController.class, 15).withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), Matchers.endsWith("/people/15/addresses"));
	}

	@Test
	public void createsLinkToSubResource() {

		Link link = linkTo(PersonController.class).slash("something").withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), Matchers.endsWith("/people/something"));
	}

	@Test
	public void createsLinkWithCustomRel() {

		Link link = linkTo(PersonController.class).withRel(Link.REL_NEXT);
		assertThat(link.getRel(), is(Link.REL_NEXT));
		assertThat(link.getHref(), Matchers.endsWith("/people"));
	}

	@Test(expected = IllegalStateException.class)
	public void rejectsControllerWithMultipleMappings() {
		linkTo(InvalidController.class);
	}

	@Test
	public void createsLinkToUnmappedController() {

		linkTo(UnmappedController.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void usesIdOfIdentifyableForPathSegment() {

		Identifiable<Long> identifyable = mock(Identifiable.class);
		when(identifyable.getId()).thenReturn(10L);

		Link link = linkTo(PersonController.class).slash(identifyable).withSelfRel();
		assertThat(link.getHref(), Matchers.endsWith("/people/10"));
	}

	@Test
	public void appendingNullIsANoOp() {

		Link link = linkTo(PersonController.class).slash(null).withSelfRel();
		assertThat(link.getHref(), Matchers.endsWith("/people"));

		link = linkTo(PersonController.class).slash((Object) null).withSelfRel();
		assertThat(link.getHref(), Matchers.endsWith("/people"));
	}

	class Person implements Identifiable<Long> {

		Long id;

		@Override
		public Long getId() {
			return id;
		}
	}

	@RequestMapping("/people")
	class PersonController {

	}

	@RequestMapping("/people/{id}/addresses")
	class PersonsAddressesController {

	}

	@RequestMapping({ "/persons", "/people" })
	class InvalidController {

	}

	class UnmappedController {

	}

}
