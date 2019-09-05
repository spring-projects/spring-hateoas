/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.springframework.hateoas.support.JsonPathUtils.*;
import static org.springframework.hateoas.support.MappingUtils.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.WebClientConfigurer;
import org.springframework.hateoas.support.WebFluxEmployeeController;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Greg Turnquist
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
class CollectionJsonWebFluxIntegrationTest {

	@Autowired WebTestClient testClient;

	@BeforeEach
	void setUp() {
		WebFluxEmployeeController.reset();
	}

	/**
	 * @see #728
	 */
	@Test
	void singleEmployee() {

		this.testClient.get().uri("http://localhost/employees/0") //
				.accept(MediaTypes.COLLECTION_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(MediaTypes.COLLECTION_JSON) //
				.expectBody(String.class) //
				.value(jsonPath("$.collection.version", is("1.0")))
				.value(jsonPath("$.collection.href", is("http://localhost/employees/0")))
				.value(jsonPath("$.collection.links.*", hasSize(1)))
				.value(jsonPath("$.collection.links[0].rel", is("employees")))
				.value(jsonPath("$.collection.links[0].href", is("http://localhost/employees")))

				.value(jsonPath("$.collection.items.*", hasSize(1)))
				.value(jsonPath("$.collection.items[0].data[1].name", is("name")))
				.value(jsonPath("$.collection.items[0].data[1].value", is("Frodo Baggins")))
				.value(jsonPath("$.collection.items[0].data[0].name", is("role")))
				.value(jsonPath("$.collection.items[0].data[0].value", is("ring bearer")))

				.value(jsonPath("$.collection.items[0].links.*", hasSize(1)))
				.value(jsonPath("$.collection.items[0].links[0].rel", is("employees")))
				.value(jsonPath("$.collection.items[0].links[0].href", is("http://localhost/employees")))

				.value(jsonPath("$.collection.template.*", hasSize(1)))
				.value(jsonPath("$.collection.template.data[0].name", is("name")))
				.value(jsonPath("$.collection.template.data[0].value", is("")))
				.value(jsonPath("$.collection.template.data[1].name", is("role")))
				.value(jsonPath("$.collection.template.data[1].value", is("")));
	}

	/**
	 * @see #728
	 */
	@Test
	void collectionOfEmployees() {

		this.testClient.get().uri("http://localhost/employees") //
				.accept(MediaTypes.COLLECTION_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(MediaTypes.COLLECTION_JSON) //
				.expectBody(String.class) //

				.value(jsonPath("$.collection.version", is("1.0")))
				.value(jsonPath("$.collection.href", is("http://localhost/employees")))

				.value(jsonPath("$.collection.items.*", hasSize(2)))
				.value(jsonPath("$.collection.items[0].data[1].name", is("name")))
				.value(jsonPath("$.collection.items[0].data[1].value", is("Frodo Baggins")))
				.value(jsonPath("$.collection.items[0].data[0].name", is("role")))
				.value(jsonPath("$.collection.items[0].data[0].value", is("ring bearer")))

				.value(jsonPath("$.collection.items[0].links.*", hasSize(1)))
				.value(jsonPath("$.collection.items[0].links[0].rel", is("employees")))
				.value(jsonPath("$.collection.items[0].links[0].href", is("http://localhost/employees")))

				.value(jsonPath("$.collection.items[1].data[1].name", is("name")))
				.value(jsonPath("$.collection.items[1].data[1].value", is("Bilbo Baggins")))
				.value(jsonPath("$.collection.items[1].data[0].name", is("role")))
				.value(jsonPath("$.collection.items[1].data[0].value", is("burglar")))

				.value(jsonPath("$.collection.items[1].links.*", hasSize(1)))
				.value(jsonPath("$.collection.items[1].links[0].rel", is("employees")))
				.value(jsonPath("$.collection.items[1].links[0].href", is("http://localhost/employees")))

				.value(jsonPath("$.collection.template.*", hasSize(1)))
				.value(jsonPath("$.collection.template.data[0].name", is("name")))
				.value(jsonPath("$.collection.template.data[0].value", is("")))
				.value(jsonPath("$.collection.template.data[1].name", is("role")))
				.value(jsonPath("$.collection.template.data[1].value", is("")));
	}

	/**
	 * @see #728
	 */
	@Test
	void createNewEmployee() throws Exception {

		String specBasedJson = read(new ClassPathResource("spec-part7-adjusted.json", getClass()));

		this.testClient.post().uri("http://localhost/employees") //
				.contentType(MediaTypes.COLLECTION_JSON) //
				.bodyValue(specBasedJson) //
				.exchange() //
				.expectStatus().isCreated() //
				.expectHeader().valueEquals(HttpHeaders.LOCATION, "http://localhost/employees/2");

		this.testClient.get().uri("http://localhost/employees/2") //
				.accept(MediaTypes.COLLECTION_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(MediaTypes.COLLECTION_JSON) //
				.expectBody(String.class) //

				.value(jsonPath("$.collection.version", is("1.0")))
				.value(jsonPath("$.collection.href", is("http://localhost/employees/2")))

				.value(jsonPath("$.collection.links.*", hasSize(1)))
				.value(jsonPath("$.collection.links[0].rel", is("employees")))
				.value(jsonPath("$.collection.links[0].href", is("http://localhost/employees")))

				.value(jsonPath("$.collection.items.*", hasSize(1)))
				.value(jsonPath("$.collection.items[0].data[1].name", is("name")))
				.value(jsonPath("$.collection.items[0].data[1].value", is("W. Chandry")))
				.value(jsonPath("$.collection.items[0].data[0].name", is("role")))
				.value(jsonPath("$.collection.items[0].data[0].value", is("developer")))

				.value(jsonPath("$.collection.items[0].links.*", hasSize(1)))
				.value(jsonPath("$.collection.items[0].links[0].rel", is("employees")))
				.value(jsonPath("$.collection.items[0].links[0].href", is("http://localhost/employees")))

				.value(jsonPath("$.collection.template.*", hasSize(1)))
				.value(jsonPath("$.collection.template.data[0].name", is("name")))
				.value(jsonPath("$.collection.template.data[0].value", is("")))
				.value(jsonPath("$.collection.template.data[1].name", is("role")))
				.value(jsonPath("$.collection.template.data[1].value", is("")));
	}

	@Configuration
	@EnableWebFlux
	@EnableHypermediaSupport(type = { HypermediaType.COLLECTION_JSON })
	static class TestConfig {

		@Bean
		WebFluxEmployeeController employeeController() {
			return new WebFluxEmployeeController();
		}

		@Bean
		WebTestClient webTestClient(WebClientConfigurer webClientConfigurer, ApplicationContext ctx) {

			return WebTestClient.bindToApplicationContext(ctx).build() //
					.mutate() //
					.exchangeStrategies(webClientConfigurer.hypermediaExchangeStrategies()) //
					.build();
		}
	}
}
