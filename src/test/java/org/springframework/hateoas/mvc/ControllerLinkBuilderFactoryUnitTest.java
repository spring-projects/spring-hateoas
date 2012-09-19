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

import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.PersonControllerImpl;
import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.PersonsAddressesController;
import org.springframework.http.HttpMethod;

/**
 * Unit tests for {@link ControllerLinkBuilderFactory}.
 *
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Daniel Sawano
 */
public class ControllerLinkBuilderFactoryUnitTest extends TestUtils {

	ControllerLinkBuilderFactory factory = new ControllerLinkBuilderFactory();

	@Test
	public void createsLinkToControllerRoot() {

		Link link = factory.linkTo(PersonControllerImpl.class).withSelfRel();

		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people"));
	}

    @Test
	public void createsPutLinkToControllerRoot() {

		Link link = factory.linkTo(PersonControllerImpl.class).method(HttpMethod.PUT).withSelfRel();

		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people"));
        assertThat(link.getMethod(), is(HttpMethod.PUT));
	}

	@Test
	public void createsLinkToParameterizedControllerRoot() {

		Link link = factory.linkTo(PersonsAddressesController.class, 15).withSelfRel();

		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people/15/addresses"));
	}
}
