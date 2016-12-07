package org.springframework.hateoas.mvc;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import static org.springframework.web.util.UriComponentsBuilder.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UriComponentsBuilder;

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
	 * Calling linkTo outside of a HTTP request should not throw an exception.
	 * @see #408
	 */
	@Test
	public void callingLinkToOutsideOfHttpRequestShouldNotThrowException() {
		Link link = linkTo(methodOn(ControllerLinkBuilderUnitTest.PersonsAddressesController.class, 15)
				.getAddressesForCountry("DE")).withSelfRel();
		assertThat(link.getHref(), is("/people/15/addresses/DE"));
	}

	/**
	 * Calling linkTo outside of a HTTP request, should allow passing in a custom builder.
	 * @see #408
	 */
	@Test
	public void linkToShouldTakeAUriCompoentsBuilder() {
		Link link = linkTo(fromUriString("https://myproxy.net:1234/somepath"), methodOn
				(ControllerLinkBuilderUnitTest
				.PersonsAddressesController
				.class, 15)
				.getAddressesForCountry("DE")).withSelfRel();
		assertThat(link.getHref(), is("https://myproxy.net:1234/somepath/people/15/addresses/DE"));
	}

}
