package org.springframework.hateoas.mvc;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.junit.Before;
import org.junit.Test;

import org.springframework.hateoas.Link;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Test cases for {@link ControllerLinkBuilder} that are NOT inside an existing Spring MVC request
 *
 * @author Greg Turnquist
 */
public class ControllerLinkBuilderOutsideSpringMvcUnitTest {

	/**
	 * Clear out any existing request attributes left behind by other tests
	 */
	@Before
	public void setUp() {
		RequestContextHolder.setRequestAttributes(null);
	}

	/**
	 * @see #408
	 */
	@Test
	public void requestingLinkOutsideWebRequest() {

		Link link = linkTo(methodOn(ControllerLinkBuilderUnitTest.PersonsAddressesController.class, 15)
			.getAddressesForCountry("DE")).withSelfRel();

		assertThat(link, is(new Link("/people/15/addresses/DE").withSelfRel()));
	}

}
