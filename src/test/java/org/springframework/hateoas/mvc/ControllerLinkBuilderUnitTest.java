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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
		assertThat(link.getHref(), Matchers.endsWith("/people/15/addresses"));
	}

	@Test
	public void createsLinkToControllerMethodWithPathVariable() throws Exception {

		Link withRel = linkTo(methodOn(ProductsController.class).product(15L)).withRel("product");
		assertEquals("http://localhost/products/15", withRel.getHref());
		assertEquals("product", withRel.getRel());

		Link withSelfRel = linkTo(methodOn(ProductsController.class).product(15L)).withSelfRel();
		assertEquals("http://localhost/products/15", withSelfRel.getHref());
		assertEquals("self", withSelfRel.getRel());
	}




	@Test
	public void createsLinkToSubResource() {

		Link link = linkTo(PersonControllerImpl.class).slash("something").withSelfRel();
		assertThat(link.getRel(), is(Link.REL_SELF));
		assertThat(link.getHref(), Matchers.endsWith("/people/something"));
	}

	@Test
	public void createsLinkWithCustomRel() {

		Link link = linkTo(PersonControllerImpl.class).withRel(Link.REL_NEXT);
		assertThat(link.getRel(), is(Link.REL_NEXT));
		assertThat(link.getHref(), Matchers.endsWith("/people"));
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

		Identifiable<Long> identifyable = mock(Identifiable.class);
		when(identifyable.getId()).thenReturn(10L);

		Link link = linkTo(PersonControllerImpl.class).slash(identifyable).withSelfRel();
		assertThat(link.getHref(), Matchers.endsWith("/people/10"));
	}

	@Test
	public void appendingNullIsANoOp() {

		Link link = linkTo(PersonControllerImpl.class).slash(null).withSelfRel();
		assertThat(link.getHref(), Matchers.endsWith("/people"));

		link = linkTo(PersonControllerImpl.class).slash((Object) null).withSelfRel();
		assertThat(link.getHref(), Matchers.endsWith("/people"));
	}

	@Test
	public void linksToMethod() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).myMethod(null)).withSelfRel();
		assertThat(link.getHref(), Matchers.endsWith("/something/else"));
	}

	@Test
	public void linksToMethodWithPathVariable() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable("1")).withSelfRel();
		assertThat(link.getHref(), Matchers.endsWith("/something/1/foo"));
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
	}
}
