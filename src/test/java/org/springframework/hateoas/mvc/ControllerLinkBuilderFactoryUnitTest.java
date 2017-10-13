/*
 * Copyright 2012-2015 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Unit tests for {@link ControllerLinkBuilderFactory}.
 * 
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Kamill Sokol
 * @author Ross Turner
 */
public class ControllerLinkBuilderFactoryUnitTest extends TestUtils {

	ControllerLinkBuilderFactory factory = new ControllerLinkBuilderFactory();

	@Test
	public void createsLinkToControllerRoot() {

		Link link = factory.linkTo(PersonControllerImpl.class).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()).endsWith("/people");
	}

	@Test
	public void createsLinkToParameterizedControllerRoot() {

		Link link = factory.linkTo(PersonsAddressesController.class, 15).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()).endsWith("/people/15/addresses");
	}

	@Test
	public void appliesParameterValueIfContributorConfigured() {

		ControllerLinkBuilderFactory factory = new ControllerLinkBuilderFactory();
		factory.setUriComponentsContributors(Arrays.asList(new SampleUriComponentsContributor()));

		SpecialType specialType = new SpecialType();
		specialType.parameterValue = "value";

		Link link = factory.linkTo(methodOn(SampleController.class).sampleMethod(1L, specialType)).withSelfRel();
		assertPointsToMockServer(link);
		assertThat(link.getHref()).endsWith("/sample/1?foo=value");
	}

	/**
	 * @see #57
	 */
	@Test
	public void usesDateTimeFormatForUriBinding() {

		DateTime now = DateTime.now();

		ControllerLinkBuilderFactory factory = new ControllerLinkBuilderFactory();
		Link link = factory.linkTo(methodOn(SampleController.class).sampleMethod(now)).withSelfRel();
		assertThat(link.getHref()).endsWith("/sample/" + ISODateTimeFormat.date().print(now));
	}

	/**
	 * @see #96
	 */
	@Test
	public void linksToMethodWithPathVariableContainingBlank() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable("with blank")).withSelfRel();
		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()).endsWith("/something/with%20blank/foo");
	}

	/**
	 * @see #96
	 */
	@Test
	public void createsLinkToParameterizedControllerRootContainingBlank() {

		Link link = factory.linkTo(PersonsAddressesController.class, "with blank").withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()).endsWith("/people/with%20blank/addresses");
	}

	/**
	 * @see #209
	 */
	@Test
	public void createsLinkToControllerMethodWithMapRequestParam() {

		Map<String, String> queryParams = new LinkedHashMap<String, String>();
		queryParams.put("firstKey", "firstValue");
		queryParams.put("secondKey", "secondValue");

		Link link = factory.linkTo(methodOn(SampleController.class).sampleMethodWithMap(queryParams)).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()).endsWith("/sample/mapsupport?firstKey=firstValue&secondKey=secondValue");
	}

	/**
	 * @see #209
	 */
	@Test
	public void createsLinkToControllerMethodWithMultiValueMapRequestParam() {

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
		queryParams.put("key1", Arrays.asList("value1a", "value1b"));
		queryParams.put("key2", Arrays.asList("value2a", "value2b"));

		Link link = factory.linkTo(methodOn(SampleController.class).sampleMethodWithMap(queryParams)).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()) //
				.endsWith("/sample/multivaluemapsupport?key1=value1a&key1=value1b&key2=value2a&key2=value2b");
	}

	/**
	 * @see #372
	 */
	@Test
	public void createsLinkToParameterizedControllerRootWithParameterMap() {

		Link link = factory.linkTo(PersonsAddressesController.class, Collections.singletonMap("id", "17")).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(Link.REL_SELF);
		assertThat(link.getHref()).endsWith("/people/17/addresses");
	}

	static interface SampleController {

		@RequestMapping("/sample/{id}")
		HttpEntity<?> sampleMethod(@PathVariable("id") Long id, SpecialType parameter);

		@RequestMapping("/sample/{time}")
		HttpEntity<?> sampleMethod(@PathVariable("time") @DateTimeFormat(iso = ISO.DATE) DateTime time);

		@RequestMapping("/sample/mapsupport")
		HttpEntity<?> sampleMethodWithMap(@RequestParam Map<String, String> queryParams);

		@RequestMapping("/sample/multivaluemapsupport")
		HttpEntity<?> sampleMethodWithMap(@RequestParam MultiValueMap<String, String> queryParams);
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
