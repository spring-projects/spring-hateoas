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
package org.springframework.hateoas.jaxrs;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.hateoas.jaxrs.JaxRsLinkBuilder.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Unit tests for {@link JaxRsLinkBuilder}.
 *
 */
public class JaxRsLinkBuilderUnitTest extends TestUtils {

	@Test
	public void createsLinkToControllerRoot() {

		Link link = linkTo(PersonController.class).withSelfRel();
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

		Link link = linkTo(PersonController.class).slash("something").withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/people/something"));
	}

	@Test
	public void createsLinkWithCustomRel() {

		Link link = linkTo(PersonController.class).withRel(Link.REL_NEXT);
		assertThat(link.getRel(), is(Link.REL_NEXT));
		assertThat(link.getHref(), endsWith("/people"));
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

		Link link = linkTo(PersonController.class).slash(identifyable).withSelfRel();
		assertThat(link.getHref(), endsWith("/people/10"));
	}

	@Test
	public void appendingNullIsANoOp() {

		Link link = linkTo(PersonController.class).slash(null).withSelfRel();
		assertThat(link.getHref(), endsWith("/people"));

		link = linkTo(PersonController.class).slash((Object) null).withSelfRel();
		assertThat(link.getHref(), endsWith("/people"));
	}

	@Test
	public void linksToMethod() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).myMethod(null)).withSelfRel();
		assertPointsToMockServer(link);
		assertThat(link.getHref(), endsWith("/something/else"));
	}

	@Test
	public void linksToMethodWithPathParam() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathParam("1")).withSelfRel();
		assertPointsToMockServer(link);
		assertThat(link.getHref(), endsWith("/something/1/foo"));
	}

	/**
	 * @see #33
	 */
	@Test
	public void usesForwardedHostAsHostIfHeaderIsSet() {

		request.addHeader("X-Forwarded-Host", "somethingDifferent");

		Link link = linkTo(PersonController.class).withSelfRel();
		assertThat(link.getHref(), startsWith("http://somethingDifferent"));
	}

	/**
	 * @see #112
	 */
	@Test
	public void usesForwardedSslIfHeaderIsSet() {

		request.addHeader("X-Forwarded-Ssl", "on");

		Link link = linkTo(PersonController.class).withSelfRel();
		assertThat(link.getHref(), startsWith("https://"));
	}

	/**
	 * @see #112
	 */
	@Test
	public void usesForwardedSslIfHeaderIsSetOff() {

		request.addHeader("X-Forwarded-Ssl", "off");

		Link link = linkTo(PersonController.class).withSelfRel();
		assertThat(link.getHref(), startsWith("http://"));
	}

	/**
	 * @see #112
	 */
	@Test
	public void usesForwardedSslAndHostIfHeaderIsSet() {

		request.addHeader("X-Forwarded-Host", "somethingDifferent");
		request.addHeader("X-Forwarded-Ssl", "on");

		Link link = linkTo(PersonController.class).withSelfRel();
		assertThat(link.getHref(), startsWith("https://somethingDifferent"));
	}

	/**
	 * @see #26, #39
	 */
	@Test
	public void addsQueryParametersHandedIntoSlashCorrectly() {

		Link link = linkTo(PersonController.class).slash("?foo=bar").withSelfRel();

		UriComponents components = toComponents(link);
		assertThat(components.getQuery(), is("foo=bar"));
	}

	/**
	 * @see #26, #39
	 */
	@Test
	public void linksToMethodWithPathParamAndQueryParams() {

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
	public void linksToMethodWithPathParamAndMultiValueQueryParams() {

		Link link = linkTo(
				methodOn(ControllerWithMethods.class).methodWithMultiValueQueryParams("1", Arrays.asList(3, 7), 5))
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

		Link link = linkTo(PersonController.class).withSelfRel();
		assertThat(link.getHref(), startsWith("http://foobar:8088"));
	}

	/**
	 * @see #90
	 */
	@Test
	public void usesFirstHostOfXForwardedHost() {

		request.addHeader("X-Forwarded-Host", "barfoo:8888, localhost:8088");

		Link link = linkTo(PersonController.class).withSelfRel();
		assertThat(link.getHref(), startsWith("http://barfoo:8888"));
	}

	/**
	 * @see #122
	 */
	@Test
	public void doesNotAppendParameterForNullQueryParameters() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithQueryParam(null)).withSelfRel();
		assertThat(link.getHref(), endsWith("/foo"));
	}

	/**
	 * @see #122
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsMissingPathParam() {
		linkTo(methodOn(ControllerWithMethods.class).methodWithPathParam(null));
	}

	/**
	 * @see #170
	 */
	@Test
	public void usesForwardedPortFromHeader() {

		request.addHeader("X-Forwarded-Host", "foobarhost");
		request.addHeader("X-Forwarded-Port", "9090");
		request.setServerPort(8080);

		Link link = linkTo(PersonController.class).withSelfRel();

		assertThat(link.getHref(), startsWith("http://foobarhost:9090/"));
	}

	/**
	 * @see #170
	 */
	@Test
	public void usesForwardedHostFromHeaderWithDefaultPort() {

		request.addHeader("X-Forwarded-Host", "foobarhost");
		request.setServerPort(8080);

		Link link = linkTo(PersonController.class).withSelfRel();
		assertThat(link.getHref(), startsWith("http://foobarhost/"));
	}

	@Test
	public void parentClassAnnotationsAreNotInherited() {

		Link link = linkTo(methodOn(ChildController.class).myMethod()).withSelfRel();
		assertThat(link.getHref(), endsWith("/child"));
	}

	@Test
	public void parentMethodAnnotationsAreInherited() {

		Link link = linkTo(methodOn(ChildWithTypeMapping.class).myMethod()).withSelfRel();
		assertThat(link.getHref(), endsWith("/child/parent"));
	}

	/**
	 * @see #96
	 */
	@Test
	public void linksToMethodWithPathParamContainingBlank() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathParam("with blank")).withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), endsWith("/something/with%20blank/foo"));
	}

	/**
	 * @see #192
	 */
	@Test
	public void usesRootMappingOfTargetClassForMethodsOfParentClass() {

		Link link = linkTo(methodOn(ChildControllerWithRootMapping.class).someEmptyMappedMethod()).withSelfRel();
		assertThat(link.getHref(), endsWith("/root"));
	}

	/**
	 * @see #192
	 */
	@Test
	public void usesRootMappingOfTargetClassForMethodsOfParen() throws Exception {

		Method method = ParentControllerWithoutRootMapping.class.getMethod("someEmptyMappedMethod");

		Link link = linkTo(ChildControllerWithRootMapping.class, method).withSelfRel();
		assertThat(link.getHref(), endsWith("/root"));
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

	@Path("/people")
	static class PersonController {}

	@Path("/people/{id}/addresses")
	static class PersonsAddressesController {

		@Path("/{country}")
		public HttpEntity<Void> getAddressesForCountry(@PathParam("country") String country) {
			return null;
		}
	}

	static class UnmappedController {

	}

	@Path("/something")
	static class ControllerWithMethods {

		@Path("/else")
		HttpEntity<Void> myMethod(@RequestBody Object payload) {
			return null;
		}

		@Path("/{id}/foo")
		HttpEntity<Void> methodWithPathParam(@PathParam("id") String id) {
			return null;
		}

		@Path("/foo")
		HttpEntity<Void> methodWithQueryParam(@QueryParam("id") String id) {
			return null;
		}

		@Path(value = "/{id}/foo")
		HttpEntity<Void> methodForNextPage(@PathParam("id") String id, @QueryParam("offset") Integer offset,
				@QueryParam("limit") Integer limit) {
			return null;
		}

		@Path(value = "/{id}/foo")
		HttpEntity<Void> methodWithMultiValueQueryParams(@PathParam("id") String id, @QueryParam("items") List<Integer> items,
				@QueryParam("limit") Integer limit) {
			return null;
		}
	}

	@Path("/parent")
	interface ParentController {}

	interface ChildController extends ParentController {

		@Path("/child")
		Object myMethod();
	}

	interface ParentWithMethod {

		@Path("/parent")
		Object myMethod();
	}

	@Path("/child")
	class ChildWithTypeMapping implements ParentWithMethod {

		@Override
		public Object myMethod() {

			return null;
		}
	}

	interface ParentControllerWithoutRootMapping {

		@Path("")
		Object someEmptyMappedMethod();
	}

	@Path("/root")
	interface ChildControllerWithRootMapping extends ParentControllerWithoutRootMapping {

	}
}
