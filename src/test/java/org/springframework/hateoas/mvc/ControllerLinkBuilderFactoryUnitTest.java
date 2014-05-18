/*
 * Copyright 2012-2014 the original author or authors.
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
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.ControllerWithMethods;
import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.PersonControllerImpl;
import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.PersonsAddressesController;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Unit tests for {@link ControllerLinkBuilderFactory}.
 * 
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Kamill Sokol
 */
public class ControllerLinkBuilderFactoryUnitTest extends TestUtils {

	ControllerLinkBuilderFactory factory = new ControllerLinkBuilderFactory();

	@Test
	public void createsLinkToControllerRoot() {

		Link link = factory.linkTo(PersonControllerImpl.class).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people"));
	}

	@Test
	public void createsLinkToParameterizedControllerRoot() {

		Link link = factory.linkTo(PersonsAddressesController.class, 15).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people/15/addresses"));
	}

	@Test
	public void appliesParameterValueIfContributorConfigured() {

		ControllerLinkBuilderFactory factory = new ControllerLinkBuilderFactory();
		factory.setUriComponentsContributors(Arrays.asList(new SampleUriComponentsContributor()));

		SpecialType specialType = new SpecialType();
		specialType.parameterValue = "value";

		Link link = factory.linkTo(methodOn(SampleController.class).sampleMethod(1L, specialType)).withSelfRel();
		assertPointsToMockServer(link);
		assertThat(link.getHref(), endsWith("/sample/1?foo=value"));
	}

	/**
	 * @see #57
	 */
	@Test
	public void usesDateTimeFormatForUriBinding() {

		DateTime now = DateTime.now();

		ControllerLinkBuilderFactory factory = new ControllerLinkBuilderFactory();
		Link link = factory.linkTo(methodOn(SampleController.class).sampleMethod(now)).withSelfRel();
		assertThat(link.getHref(), endsWith("/sample/" + ISODateTimeFormat.date().print(now)));
	}

	/**
	 * @see #96
	 */
	@Test
	public void linksToMethodWithPathVariableContainingBlank() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable("with blank")).withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/something/with%20blank/foo"));
	}

	/**
	 * @see #96
	 */
	@Test
	public void createsLinkToParameterizedControllerRootContainingBlank() {

		Link link = factory.linkTo(PersonsAddressesController.class, "with blank").withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people/with%20blank/addresses"));
	}

	static interface SampleController {

		@RequestMapping("/sample/{id}")
		HttpEntity<?> sampleMethod(@PathVariable("id") Long id, SpecialType parameter);

		@RequestMapping("/sample/{time}")
		HttpEntity<?> sampleMethod(@PathVariable("time") @DateTimeFormat(iso = ISO.DATE) DateTime time);
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
