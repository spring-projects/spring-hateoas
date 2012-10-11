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
//import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkToMethod;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linksToResources;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkTemplate;
import org.springframework.hateoas.TestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestMapping;
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
	public void createsLinksToResourcesInController() {
		List<LinkTemplate> links = linksToResources(PersonsProductsController.class);
		assertEquals(2, links.size());

		assertEquals("productsOfPerson", links.get(0).getRel());
		assertEquals("/products", links.get(0).getHref());
		assertEquals(Object.class, links.get(0).getParams().get("personId"));

		assertEquals("productById", links.get(1).getRel());
		assertEquals("/products", links.get(1).getHref());
		assertEquals(Object.class, links.get(1).getParams().get("productId"));

	}

	@Test
	public void createsLinksToResourcesInControllerAtClassLevel() {
		List<LinkTemplate> links = linksToResources(PersonsProductsControllerClassLevel.class);
		assertEquals(2, links.size());

		assertEquals("productsOfPerson", links.get(0).getRel());
		assertEquals("/products", links.get(0).getHref());
		assertEquals(Object.class, links.get(0).getParams().get("personId"));

		assertEquals("productById", links.get(1).getRel());
		assertEquals("/products", links.get(1).getHref());
		assertEquals(Object.class, links.get(1).getParams().get("productId"));

		// TODO: now I know I have resources with params
		// at /products I would have to create two forms with input personId and
		// input productId

		// what to do about path variables, I do not know. The actual value is
		// known when a person is requested
		// i.e. within getPerson.
		// actually, we could return links
	}

	@Test
	public void createsLinksToProductsController() {
		List<LinkTemplate> links = linksToResources(ProductsController.class);
		assertEquals(4, links.size());

		assertEquals("product", links.get(0).getRel());
		assertEquals("/products/{productId}", links.get(0).getHref());

		assertEquals("productsOfPerson", links.get(1).getRel());
		assertEquals("/people/{personId}/products", links.get(1).getHref());

		assertEquals("products", links.get(2).getRel());
		assertEquals("/products", links.get(2).getHref());

		assertEquals("productDetails", links.get(3).getRel());
		assertEquals("/products/{productId}/details", links.get(3).getHref());

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

	class Person implements Identifiable<Long> {

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

		@RequestMapping(value = "/products", params = "personId")
		public HttpEntity<List<Product>> productsOfPerson(@RequestParam Long personId) {
			return null;
		}

		@RequestMapping(value = "/products", params = "productId")
		public HttpEntity<List<Product>> productById(@RequestParam Long productId) {
			return null;
		}

	}

	@RequestMapping("/products")
	class PersonsProductsControllerClassLevel {

		@RequestMapping(params = "personId")
		public HttpEntity<List<Product>> productsOfPerson(@RequestParam Long personId) {
			return null;
		}

		@RequestMapping(params = "productId")
		public HttpEntity<List<Product>> productById(@RequestParam Long productId) {
			return null;
		}
	}

	@RequestMapping({ "/persons", "/people" })
	class InvalidController {

	}

	class UnmappedController {

	}

}
