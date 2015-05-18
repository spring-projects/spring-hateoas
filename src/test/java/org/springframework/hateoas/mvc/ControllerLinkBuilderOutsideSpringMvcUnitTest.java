package org.springframework.hateoas.mvc;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.junit.Test;

/**
 * Test cases for {@link ControllerLinkBuilder} that are NOT inside an existing Spring MVC request
 *
 * @author Greg Turnquist
 */
public class ControllerLinkBuilderOutsideSpringMvcUnitTest {

	/**
	 * @see #342
	 */
	@Test(expected = IllegalStateException.class)
	public void createsLinkToMethodOnParameterizedControllerRoot() {

		try {
			linkTo(methodOn(ControllerLinkBuilderUnitTest.PersonsAddressesController.class, 15)
					.getAddressesForCountry("DE")).withSelfRel();
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(), equalTo("Could not find current request via RequestContextHolder. Is this being called from a Spring MVC handler?"));
			throw e;
		}
	}

}
