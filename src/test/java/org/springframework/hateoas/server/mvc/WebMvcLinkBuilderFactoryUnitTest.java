/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.server.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderUnitTest.ControllerWithMethods;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderUnitTest.PersonControllerImpl;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderUnitTest.PersonsAddressesController;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Unit tests for {@link WebMvcLinkBuilderFactory}.
 *
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Kamill Sokol
 * @author Ross Turner
 */
class WebMvcLinkBuilderFactoryUnitTest extends TestUtils {

	WebMvcLinkBuilderFactory factory = new WebMvcLinkBuilderFactory();

	@Test
	void createsLinkToControllerRoot() {

		Link link = factory.linkTo(PersonControllerImpl.class).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/people");
	}

	@Test
	void createsLinkToParameterizedControllerRoot() {

		Link link = factory.linkTo(PersonsAddressesController.class, 15).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/people/15/addresses");
	}

	@Test
	void appliesParameterValueIfContributorConfigured() {

		WebMvcLinkBuilderFactory factory = new WebMvcLinkBuilderFactory();
		factory.setUriComponentsContributors(Collections.singletonList(new SampleUriComponentsContributor()));

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
	void usesDateTimeFormatForUriBinding() {

		DateTime now = DateTime.now();

		WebMvcLinkBuilderFactory factory = new WebMvcLinkBuilderFactory();
		Link link = factory.linkTo(methodOn(SampleController.class).sampleMethod(now)).withSelfRel();
		assertThat(link.getHref()).endsWith("/sample/" + ISODateTimeFormat.date().print(now));
	}

	/**
	 * @see #96
	 */
	@Test
	void linksToMethodWithPathVariableContainingBlank() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable("with blank")).withSelfRel();
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/something/with%20blank/foo");
	}

	/**
	 * @see #96
	 */
	@Test
	void createsLinkToParameterizedControllerRootContainingBlank() {

		Link link = factory.linkTo(PersonsAddressesController.class, "with blank").withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/people/with%20blank/addresses");
	}

	/**
	 * @see #209
	 */
	@Test
	void createsLinkToControllerMethodWithMapRequestParam() {

		Map<String, String> queryParams = new LinkedHashMap<>();
		queryParams.put("firstKey", "firstValue");
		queryParams.put("secondKey", "secondValue");

		Link link = factory.linkTo(methodOn(SampleController.class).sampleMethodWithMap(queryParams)).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/sample/mapsupport?firstKey=firstValue&secondKey=secondValue");
	}

	/**
	 * @see #209
	 */
	@Test
	void createsLinkToControllerMethodWithMultiValueMapRequestParam() {

		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.put("key1", Arrays.asList("value1a", "value1b"));
		queryParams.put("key2", Arrays.asList("value2a", "value2b"));

		Link link = factory.linkTo(methodOn(SampleController.class).sampleMethodWithMap(queryParams)).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()) //
				.endsWith("/sample/multivaluemapsupport?key1=value1a&key1=value1b&key2=value2a&key2=value2b");
	}

	/**
	 * @see #372
	 */
	@Test
	void createsLinkToParameterizedControllerRootWithParameterMap() {

		Link link = factory.linkTo(PersonsAddressesController.class, Collections.singletonMap("id", "17")).withSelfRel();

		assertPointsToMockServer(link);
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/people/17/addresses");
	}

	interface SampleController {

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
