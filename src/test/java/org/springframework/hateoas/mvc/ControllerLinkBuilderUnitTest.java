/*
 * Copyright 2012 the original author or authors.
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

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
 * @author Oliver Gierke
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

	@Test(expected = IllegalArgumentException.class)
	public void createsLinkToUnmappedController() {
		linkTo(UnmappedController.class);
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
		assertThat(link.getHref(), endsWith("/something/else"));
	}

	@Test
	public void linksToMethodWithPathVariable() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable("1")).withSelfRel();
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

	@Test
	public void linksToMethodWithPathVariableAndRequestParams() {
		Link link = linkTo(methodOn(ControllerWithMethods.class).methodForNextPage("1", 10, 5)).withSelfRel();
		UriComponents components = UriComponentsBuilder.fromUriString(link.getHref()).build();
		assertEquals("/something/1/foo", components.getPath());
		MultiValueMap<String, String> queryParams = components.getQueryParams();
		assertThat(queryParams.get("limit"), contains("5"));
		assertThat(queryParams.get("offset"), contains("10"));
	}

	@Test
	public void linksToMethodWithPathVariableAndMultiValueRequestParams() {
		Link link = linkTo(
				methodOn(ControllerWithMethods.class).methodWithMultiValueRequestParams("1", Arrays.asList(3, 7), 5))
				.withSelfRel();
		UriComponents components = UriComponentsBuilder.fromUriString(link.getHref()).build();
		assertEquals("/something/1/foo", components.getPath());
		MultiValueMap<String, String> queryParams = components.getQueryParams();
		assertThat(queryParams.get("limit"), contains("5"));
		assertThat(queryParams.get("items"), containsInAnyOrder("3", "7"));
	}

	static class Person implements Identifiable<Long> {

		Long id;

		@Override
		public Long getId() {
			return id;
		}
	}

	@RequestMapping("/people")
	interface PersonController {

	}

	class PersonControllerImpl implements PersonController {

	}

	@RequestMapping("/people/{id}/addresses")
	class PersonsAddressesController {

	}

	class Product {

	}

	class PersonsProductsController {

		@RequestMapping(value = "/products/{personId}")
		public HttpEntity<List<Product>> productsOfPerson(@PathVariable("personId") Long personId) {
			return null;
		}

		@RequestMapping(value = "/products/{productId}")
		public HttpEntity<List<Product>> productById(@PathVariable("productId") Long productId) {
			return null;
		}

	}

	@RequestMapping("/products")
	class PersonsProductsControllerClassLevel {

		@RequestMapping("/person/{personId}")
		public HttpEntity<List<Product>> productsOfPerson(@PathVariable("personId") Long personId) {
			return null;
		}

		@RequestMapping("/{productId}")
		public HttpEntity<List<Product>> productById(@PathVariable("productId") String productId) {
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

		@RequestMapping(value = "/{id}/foo", params = { "limit", "offset" })
		HttpEntity<Void> methodForNextPage(@PathVariable String id, @RequestParam Integer offset,
				@RequestParam Integer limit) {
			return null;
		}

		@RequestMapping(value = "/{id}/foo", params = { "limit", "items" })
		HttpEntity<Void> methodWithMultiValueRequestParams(@PathVariable String id, @RequestParam List<Integer> items,
				@RequestParam Integer limit) {
			return null;
		}
	}
}
