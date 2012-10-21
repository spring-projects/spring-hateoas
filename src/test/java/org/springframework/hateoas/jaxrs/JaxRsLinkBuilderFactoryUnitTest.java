package org.springframework.hateoas.jaxrs;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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

	@Path("people")
	class PersonsService {

		@GET
		@Path("person/{id}")
		public String searchPerson(@PathParam("id") Long id) {
			return "";
		}
	}
}
