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
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Unit tests for {@link ControllerLinkBuilder}.
 * 
 * @author Oliver Gierke
 * @author Dietrich Schulten
 * @author Kamill Sokol
 */
public class ControllerLinkBuilderUnitTest extends TestUtils {

	@Test
	public void createsLinkToControllerRoot() {

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), Matchers.endsWith("/people"));
	}

	@Test
	public void createsLinkToParameterizedControllerRoot() {

		Link link = linkTo(PersonsAddressesController.class, 15).withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people/15/addresses"));
	}

	/**
	 * @see #70
	 */
	@Test
	public void createsLinkToMethodOnParameterizedControllerRoot() {

		Link link = linkTo(methodOn(PersonsAddressesController.class, 15).getAddressesForCountry("DE")).withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people/15/addresses/DE"));
	}

	@Test
	public void createsLinkToSubResource() {

		Link link = linkTo(PersonControllerImpl.class).slash("something").withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people/something"));
	}

	@Test
	public void createsLinkWithCustomRel() {

		Link link = linkTo(PersonControllerImpl.class).withRel(Link.REL_NEXT);
		assertThat(link.getRel(), is(Link.REL_NEXT));
		assertThat(link.getHref(), endsWith("/people"));
	}

	@Test(expected = IllegalStateException.class)
	public void rejectsControllerWithMultipleMappings() {
		linkTo(InvalidController.class);
	}

	@Test
	public void createsLinkToUnmappedController() {

		Link link = linkTo(UnmappedController.class).withSelfRel();
		assertThat(link.getHref(), is("http://localhost"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void usesIdOfIdentifyableForPathSegment() {

		Identifiable<Long> identifyable = Mockito.mock(Identifiable.class);
		Mockito.when(identifyable.getId()).thenReturn(10L);

		Link link = linkTo(PersonControllerImpl.class).slash(identifyable).withSelfRel();
		assertThat(link.getHref(), endsWith("/people/10"));
	}

	@Test
	public void appendingNullIsANoOp() {

		Link link = linkTo(PersonControllerImpl.class).slash(null).withSelfRel();
		assertThat(link.getHref(), endsWith("/people"));

		link = linkTo(PersonControllerImpl.class).slash((Object) null).withSelfRel();
		assertThat(link.getHref(), endsWith("/people"));
	}

	@Test
	public void linksToMethod() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).myMethod(null)).withSelfRel();
		assertPointsToMockServer(link);
		assertThat(link.getHref(), endsWith("/something/else"));
	}

	@Test
	public void linksToMethodWithPathVariable() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable("1")).withSelfRel();
		assertPointsToMockServer(link);
		assertThat(link.getHref(), endsWith("/something/1/foo"));
	}

	/**
	 * @see #33
	 */
	@Test
	public void usesForwardedHostAsHostIfHeaderIsSet() {

		request.addHeader("X-Forwarded-Host", "somethingDifferent");

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref(), startsWith("http://somethingDifferent"));
	}

	/**
	 * @see #112
	 */
	@Test
	public void usesForwardedSslIfHeaderIsSet() {

		request.addHeader("X-Forwarded-Ssl", "on");

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref(), startsWith("https://"));
	}

	/**
	 * @see #112
	 */
	@Test
	public void usesForwardedSslIfHeaderIsSetOff() {

		request.addHeader("X-Forwarded-Ssl", "off");

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref(), startsWith("http://"));
	}

	/**
	 * @see #112
	 */
	@Test
	public void usesForwardedSslAndHostIfHeaderIsSet() {

		request.addHeader("X-Forwarded-Host", "somethingDifferent");
		request.addHeader("X-Forwarded-Ssl", "on");

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref(), startsWith("https://somethingDifferent"));
	}

	/**
	 * @see #26, #39
	 */
	@Test
	public void addsRequestParametersHandedIntoSlashCorrectly() {

		Link link = linkTo(PersonController.class).slash("?foo=bar").withSelfRel();

		UriComponents components = toComponents(link);
		assertThat(components.getQuery(), is("foo=bar"));
	}

	/**
	 * @see #26, #39
	 */
	@Test
	public void linksToMethodWithPathVariableAndRequestParams() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodForNextPage("1", 10, 5)).withSelfRel();

		UriComponents components = toComponents(link);
		assertThat(components.getPath(), is("/something/1/foo"));

		MultiValueMap<String, String> queryParams = components.getQueryParams();
		assertThat(queryParams.get("limit"), contains("5"));
		assertThat(queryParams.get("offset"), contains("10"));
	}

	/**
	 * @see #26, #39
	 */
	@Test
	public void linksToMethodWithPathVariableAndMultiValueRequestParams() {

		Link link = linkTo(
				methodOn(ControllerWithMethods.class).methodWithMultiValueRequestParams("1", Arrays.asList(3, 7), 5))
				.withSelfRel();

		UriComponents components = toComponents(link);
		assertThat(components.getPath(), is("/something/1/foo"));

		MultiValueMap<String, String> queryParams = components.getQueryParams();
		assertThat(queryParams.get("limit"), contains("5"));
		assertThat(queryParams.get("items"), containsInAnyOrder("3", "7"));
	}

	/**
	 * @see #26, #39
	 */
	@Test
	public void returnsUriComponentsBuilder() {

		UriComponents components = linkTo(PersonController.class).slash("something?foo=bar").toUriComponentsBuilder()
				.build();

		assertThat(components.getPath(), is("/people/something"));
		assertThat(components.getQuery(), is("foo=bar"));
	}

	/**
	 * @see #90
	 */
	@Test
	public void usesForwardedHostAndPortFromHeader() {

		request.addHeader("X-Forwarded-Host", "foobar:8088");

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref(), startsWith("http://foobar:8088"));
	}

	/**
	 * @see #90
	 */
	@Test
	public void usesFirstHostOfXForwardedHost() {

		request.addHeader("X-Forwarded-Host", "barfoo:8888, localhost:8088");

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref(), startsWith("http://barfoo:8888"));
	}

	/**
	 * @see #122
	 */
	@Test
	public void doesNotAppendParameterForNullRequestParameters() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodForOptionalNextPage(null)).withSelfRel();
		assertThat(link.getHref(), endsWith("/foo"));
	}

	/**
	 * @see #122
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsMissingPathVariable() {
		linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable(null));
	}

	/**
	 * @see #122
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsMissingRequiredRequestParam() {
		linkTo(methodOn(ControllerWithMethods.class).methodWithRequestParam(null));
	}

	/**
	 * @see #170
	 */
	@Test
	public void usesForwardedPortFromHeader() {

		request.addHeader("X-Forwarded-Host", "foobarhost");
		request.addHeader("X-Forwarded-Port", "9090");
		request.setServerPort(8080);

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();

		assertThat(link.getHref(), startsWith("http://foobarhost:9090/"));
	}

	/**
	 * @see #170
	 */
	@Test
	public void usesForwardedHostFromHeaderWithDefaultPort() {

		request.addHeader("X-Forwarded-Host", "foobarhost");
		request.setServerPort(8080);

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref(), startsWith("http://foobarhost/"));
	}

	/**
	 * @see #114
	 */
	@Test
	public void discoversParentClassTypeMappingForInvocation() {

		Link link = linkTo(methodOn(ChildController.class).myMethod()).withSelfRel();
		assertThat(link.getHref(), endsWith("/parent/child"));
	}

	/**
	 * @see #114
	 */
	@Test
	public void includesTypeMappingFromChildClass() {

		Link link = linkTo(methodOn(ChildWithTypeMapping.class).myMethod()).withSelfRel();
		assertThat(link.getHref(), endsWith("/child/parent"));
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

	private static UriComponents toComponents(Link link) {
		return UriComponentsBuilder.fromUriString(link.getHref()).build();
	}

	static class Person implements Identifiable<Long> {

		Long id;

		@Override
		public Long getId() {
			return id;
		}
	}

	@RequestMapping("/people")
	interface PersonController {}

	class PersonControllerImpl implements PersonController {}

	@RequestMapping("/people/{id}/addresses")
	static class PersonsAddressesController {

		@RequestMapping("/{country}")
		public HttpEntity<Void> getAddressesForCountry(@PathVariable String country) {
			return null;
		}
	}

	@RequestMapping({ "/persons", "/people" })
	class InvalidController {

	}

	class UnmappedController {

	}

	@RequestMapping("/something")
	static class ControllerWithMethods {

		@RequestMapping("/else")
		HttpEntity<Void> myMethod(@RequestBody Object payload) {
			return null;
		}

		@RequestMapping("/{id}/foo")
		HttpEntity<Void> methodWithPathVariable(@PathVariable String id) {
			return null;
		}

		@RequestMapping("/foo")
		HttpEntity<Void> methodWithRequestParam(@RequestParam String id) {
			return null;
		}

		@RequestMapping(value = "/{id}/foo")
		HttpEntity<Void> methodForNextPage(@PathVariable String id, @RequestParam Integer offset,
				@RequestParam Integer limit) {
			return null;
		}

		@RequestMapping(value = "/{id}/foo")
		HttpEntity<Void> methodWithMultiValueRequestParams(@PathVariable String id, @RequestParam List<Integer> items,
				@RequestParam Integer limit) {
			return null;
		}

		@RequestMapping(value = "/foo")
		HttpEntity<Void> methodForOptionalNextPage(@RequestParam(required = false) Integer offset) {
			return null;
		}
	}

	@RequestMapping("/parent")
	interface ParentController {}

	interface ChildController extends ParentController {

		@RequestMapping("/child")
		Object myMethod();
	}

	interface ParentWithMethod {

		@RequestMapping("/parent")
		Object myMethod();
	}

	@RequestMapping("/child")
	interface ChildWithTypeMapping extends ParentWithMethod {}
}
