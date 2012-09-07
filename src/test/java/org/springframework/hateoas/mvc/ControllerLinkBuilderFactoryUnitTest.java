package org.springframework.hateoas.mvc;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.PersonControllerImpl;
import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.PersonsAddressesController;

public class ControllerLinkBuilderFactoryUnitTest extends TestUtils {

	private ControllerLinkBuilderFactory factory = new ControllerLinkBuilderFactory();

	@Test
	public void createsLinkToControllerRoot() {
		Link link = factory.linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), Matchers.endsWith("/people"));
	}

	@Test
	public void createsLinkToParameterizedControllerRoot() {
		Link link = factory.linkTo(PersonsAddressesController.class, 15).withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), Matchers.endsWith("/people/15/addresses"));
	}

}
