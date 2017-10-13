/*
 * Copyright 2011-2016 the original author or authors.
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
package org.springframework.hateoas.jaxrs;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;

import javax.ws.rs.Path;

import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;

/**
 * Unit test for {@link JaxRsLinkBuilderFactory}.
 * 
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Kamill Sokol
 * @author Andrew Naydyonock
 */
public class JaxRsLinkBuilderFactoryUnitTest extends TestUtils {

	JaxRsLinkBuilderFactory factory = new JaxRsLinkBuilderFactory();

	@Test
	public void createsLinkToServiceRoot() {

		Link link = factory.linkTo(PersonServiceImpl.class).withSelfRel();

		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()).endsWith("/people");
	}

	@Test
	public void createsLinkToParameterizedServiceRoot() {

		Link link = factory.linkTo(PersonsAddressesService.class, 15).withSelfRel();

		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()).endsWith("/people/15/addresses");
	}

	/**
	 * @see #96
	 */
	@Test
	public void createsLinkToParameterizedServiceRootWithUrlEncoding() {

		Link link = factory.linkTo(PersonsAddressesService.class, "with blank").withSelfRel();

		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()).endsWith("/people/with%20blank/addresses");
	}

	/**
	 * @see #372
	 */
	@Test
	public void createsLinkToParameterizedServiceRootWithParameterMap() {

		Link link = factory.linkTo(PersonsAddressesService.class, Collections.singletonMap("id", "17")).withSelfRel();

		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()).endsWith("/people/17/addresses");
	}

	@Path("/people")
	interface PersonService {

	}

	class PersonServiceImpl implements PersonService {

	}

	@Path("/people/{id}/addresses")
	class PersonsAddressesService {

	}
}
