package org.springframework.hateoas.jaxrs;

import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.http.HttpMethod;

import javax.ws.rs.Path;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link JaxRsLinkBuilderFactory}.
 *
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Daniel Sawano
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
	public void createsPostLinkToServiceRoot() {

		Link link = factory.linkTo(PersonServiceImpl.class).method(HttpMethod.POST).withSelfRel();

		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people"));
        assertThat(link.getMethod(), is(HttpMethod.POST));
	}

    @Test
    public void builderShouldNotBeAffectedByCallingOrder() throws Exception {
        Link link1 =
                factory.linkTo(PersonServiceImpl.class).method(HttpMethod.DELETE).slash("someValue").withSelfRel();
        Link link2 =
                factory.linkTo(PersonServiceImpl.class).slash("someValue").method(HttpMethod.DELETE).withSelfRel();

        assertEquals(link1, link2);
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
