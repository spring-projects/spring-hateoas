/*
 * Copyright 2011-2014 the original author or authors.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.hateoas.jaxrs.JaxRsLinkBuilder.*;

import java.util.Arrays;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.jaxrs.JaxRsLinkBuilderUnitTest.ControllerWithMethods;
import org.springframework.hateoas.mvc.UriComponentsContributor;
import org.springframework.http.HttpEntity;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Unit test for {@link JaxRsLinkBuilderFactory}.
 *
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Kamill Sokol
 */
public class JaxRsLinkBuilderFactoryUnitTest extends TestUtils {

	JaxRsLinkBuilderFactory factory = new JaxRsLinkBuilderFactory();

	@Test
	public void createsLinkToServiceRoot() {

		Link link = factory.linkTo(PersonService.class).withSelfRel();

		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people"));
	}

	@Test
	public void createsLinkToParameterizedServiceRoot() {

		Link link = factory.linkTo(PersonsAddressesService.class, 15).withSelfRel();

		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people/15/addresses"));
	}

	@Test
	public void appliesParameterValueIfContributorConfigured() {

		JaxRsLinkBuilderFactory factory = new JaxRsLinkBuilderFactory();
		factory.setUriComponentsContributors(Arrays.asList(new SampleUriComponentsContributor()));

		SpecialType specialType = new SpecialType();
		specialType.parameterValue = "value";

		Link link = factory.linkTo(methodOn(PersonService.class).getMethod(1L, specialType)).withSelfRel();
		assertPointsToMockServer(link);
		assertThat(link.getHref(), endsWith("/people/1?foo=value"));
	}

	/**
	 * @see #96
	 */
	@Test
	public void linksToMethodWithPathVariableContainingBlank() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathParam("with blank")).withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/something/with%20blank/foo"));
	}

	/**
	 * @see #96
	 */
	@Test
	public void createsLinkToParameterizedServiceRootWithUrlEncoding() {

		Link link = factory.linkTo(PersonsAddressesService.class, "with blank").withSelfRel();

		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people/with%20blank/addresses"));
	}

	@Path("/people")
	class PersonService {

		@Path("/{id}")
		HttpEntity<?> getMethod(@PathParam("id") Long theIdParameter, SpecialType parameter) {

			return null;
		}
	}

	@Path("/people/{id}/addresses")
	class PersonsAddressesService {

	}

	static class SampleUriComponentsContributor implements UriComponentsContributor {

		@Override
		public boolean supportsParameter(MethodParameter parameter) {
			return SpecialType.class.equals(parameter.getParameterType());
		}

		@Override
		public void enhance(UriComponentsBuilder builder, MethodParameter parameter, Object value) {
			builder.queryParam("foo", ((SpecialType) value).parameterValue);
		}
	}

	static class SpecialType {
		String parameterValue;
	}
}
