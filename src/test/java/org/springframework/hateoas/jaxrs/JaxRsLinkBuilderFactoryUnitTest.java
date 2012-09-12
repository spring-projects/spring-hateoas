package org.springframework.hateoas.jaxrs;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import javax.ws.rs.Path;

import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;

/**
 * Unit test for {@link JaxRsLinkBuilderFactory}.
 * 
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 */
public class JaxRsLinkBuilderFactoryUnitTest extends TestUtils {

	JaxRsLinkBuilderFactory factory = new JaxRsLinkBuilderFactory();

	@Test
	public void createsLinkToServiceRoot() {

		Link link = factory.linkTo(PersonServiceImpl.class).withSelfRel();

		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people"));
	}

	@Test
	public void createsLinkToParameterizedServiceRoot() {

		Link link = factory.linkTo(PersonsAddressesService.class, 15).withSelfRel();

		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people/15/addresses"));
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
