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
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkToMethod;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkToForm;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.on;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.FormDescriptor;
import org.springframework.hateoas.TestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
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

		Link withRel = linkToMethod(on(ProductsController.class).product(15L)).withRel("product");
		assertEquals("http://localhost/products/15", withRel.getHref());
		assertEquals("product", withRel.getRel());

		Link withSelfRel = linkToMethod(on(ProductsController.class).product(15L)).withSelfRel();
		assertEquals("http://localhost/products/15", withSelfRel.getHref());
		assertEquals("self", withSelfRel.getRel());
	}

	static class PersonControllerForForm {
		@RequestMapping(value = "/person/{personName}", method = RequestMethod.POST)
		public HttpEntity<? extends Object> showPerson(@PathVariable("personName") String bar,
				@RequestParam(value = "personId") Long personId) {
			return null;
		}
	}
	@Test
	public void createsLinkToFormWithPathVariable() throws Exception {
		FormDescriptor formDescriptor = linkToForm("searchPerson", on(PersonControllerForForm.class)
				.showPerson("mike", null));
		// TODO the linkTemplate field should not contain the expanded template
		assertEquals("/person/mike", formDescriptor.getLinkTemplate());
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

	@Test
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

}
