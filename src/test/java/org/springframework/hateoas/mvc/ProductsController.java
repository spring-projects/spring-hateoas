package org.springframework.hateoas.mvc;

import java.util.List;
import java.util.Map;

import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.Person;
import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.Product;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class ProductsController {

	@RequestMapping(value = "/products")
	public HttpEntity<List<Person>> products() {
		return null;
	}

	@RequestMapping(value = "/products/{productId}")
	public HttpEntity<Person> product(@PathVariable Long productId) {
		return null;
	}

	@RequestMapping(value = "/products/{productId}/details", params = "attr")
	public HttpEntity<Map<String, String>> productDetails(@PathVariable Long personId,
			@RequestParam(value = "attr", required = true) String[] attr) {
		return null;
	}

	@RequestMapping(value = "/people/{personId}/products")
	public HttpEntity<List<Product>> productsOfPerson(@PathVariable Long personId) {
		return null;
	}
}