/*
 * Copyright 2018-2019 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.springframework.hateoas.IanaLinkRelations.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.hateoas.support.MappingUtils.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.ModelBuilder;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Test one controller, rendered into multiple media types.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class ZoomProtocolWebMvcIntegrationTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	private static Map<Integer, Product> PRODUCTS;

	@Before
	public void setUp() {

		this.mockMvc = webAppContextSetup(this.context).build();

		PRODUCTS = new TreeMap<>();

		PRODUCTS.put(998, new Product("someValue", true, true));
		PRODUCTS.put(777, new Product("someValue", true, false));
		PRODUCTS.put(444, new Product("someValue", false, true));
		PRODUCTS.put(333, new Product("someValue", false, true));
		PRODUCTS.put(222, new Product("someValue", false, true));
		PRODUCTS.put(111, new Product("someValue", false, true));
		PRODUCTS.put(555, new Product("someValue", false, true));
		PRODUCTS.put(666, new Product("someValue", false, true));
	}

	@Test // #175
	public void modelBuilderCanAssembleZoomProtocol() throws Exception {

		String results = this.mockMvc.perform(get("/products").accept(MediaTypes.HAL_JSON)) //
				.andExpect(status().isOk()) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();

		assertThat(results).isEqualTo(read(new ClassPathResource("zoom-hypermedia.json", getClass())));
	}

	@RestController
	static class ProductController {

		LinkRelation favoriteProducts = LinkRelation.of("favorite products");
		LinkRelation purchasedProducts = LinkRelation.of("purchased products");

		@GetMapping("/products")
		public RepresentationModel<?> all() {

			ModelBuilder.EmbeddedModelBuilder builder = ModelBuilder //
					.embed() //
					.rootLink(linkTo(methodOn(ProductController.class).all()).withSelfRel());

			PRODUCTS.keySet().stream() //
					.map(id -> new EntityModel<>(PRODUCTS.get(id), new Link("http://localhost/products/{id}").expand(id))) //
					.forEach(productEntityModel -> {

						if (productEntityModel.getContent().isFavorite()) {

							builder //
									.embed(favoriteProducts) //
									.entityModel(productEntityModel) //
									.rootLink(productEntityModel.getRequiredLink(SELF).withRel(favoriteProducts));
						}

						if (productEntityModel.getContent().isPurchased()) {

							builder //
									.embed(purchasedProducts) //
									.entityModel(productEntityModel) //
									.rootLink(productEntityModel.getRequiredLink(SELF).withRel(purchasedProducts));
						}
					});

			return builder.build();
		}
	}

	@Data
	@AllArgsConstructor
	static class Product {
		private String someProductProperty;
		@Getter(onMethod = @__({ @JsonIgnore })) private boolean favorite = false;
		@Getter(onMethod = @__({ @JsonIgnore })) private boolean purchased = false;
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.HAL })
	static class TestConfig {

		@Bean
		ProductController employeeController() {
			return new ProductController();
		}

		@Bean
		ObjectMapper testMapper() {

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			return objectMapper;
		}
	}
}
