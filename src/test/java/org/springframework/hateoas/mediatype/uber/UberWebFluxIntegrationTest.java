/*
 * Copyright 2017-2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.uber;

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
import org.springframework.hateoas.config.HypermediaWebTestClientConfigurer;
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
class UberWebFluxIntegrationTest {

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
				.accept(MediaTypes.UBER_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(MediaTypes.UBER_JSON) //
				.expectBody(String.class) //

				.value(jsonPath("$.uber.version", is("1.0")))

				.value(jsonPath("$.uber.data.*", hasSize(5))) //
				.value(jsonPath("$.uber.data[0].name", is("self"))) //
				.value(jsonPath("$.uber.data[0].rel[0]", is("self"))) //
				.value(jsonPath("$.uber.data[0].rel[1]", is("findOne"))) //
				.value(jsonPath("$.uber.data[0].url", is("http://localhost/employees/0")))

				.value(jsonPath("$.uber.data[1].name", is("updateEmployee")))
				.value(jsonPath("$.uber.data[1].rel[0]", is("updateEmployee")))
				.value(jsonPath("$.uber.data[1].url", is("http://localhost/employees/0")))
				.value(jsonPath("$.uber.data[1].action", is("replace")))
				.value(jsonPath("$.uber.data[1].model", is("name={name}&role={role}")))

				.value(jsonPath("$.uber.data[2].name", is("partiallyUpdateEmployee")))
				.value(jsonPath("$.uber.data[2].rel[0]", is("partiallyUpdateEmployee")))
				.value(jsonPath("$.uber.data[2].url", is("http://localhost/employees/0")))
				.value(jsonPath("$.uber.data[2].action", is("partial")))
				.value(jsonPath("$.uber.data[2].model", is("name={name}&role={role}")))

				.value(jsonPath("$.uber.data[3].name", is("employees")))
				.value(jsonPath("$.uber.data[3].rel[0]", is("employees"))).value(jsonPath("$.uber.data[3].rel[1]", is("all")))
				.value(jsonPath("$.uber.data[3].url", is("http://localhost/employees")))

				.value(jsonPath("$.uber.data[4].name", is("employee"))).value(jsonPath("$.uber.data[4].data.*", hasSize(2)))
				.value(jsonPath("$.uber.data[4].data[0].name", is("role")))
				.value(jsonPath("$.uber.data[4].data[0].value", is("ring bearer")))
				.value(jsonPath("$.uber.data[4].data[1].name", is("name")))
				.value(jsonPath("$.uber.data[4].data[1].value", is("Frodo Baggins")));
	}

	/**
	 * @see #728
	 */
	@Test
	void collectionOfEmployees() {

		this.testClient.get().uri("http://localhost/employees") //
				.accept(MediaTypes.UBER_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(MediaTypes.UBER_JSON) //
				.expectBody(String.class) //

				.value(jsonPath("$.uber.version", is("1.0")))

				.value(jsonPath("$.uber.data.*", hasSize(4)))

				.value(jsonPath("$.uber.data[0].name", is("self"))) //
				.value(jsonPath("$.uber.data[0].rel[0]", is("self"))).value(jsonPath("$.uber.data[0].rel[1]", is("all")))
				.value(jsonPath("$.uber.data[0].url", is("http://localhost/employees")))

				.value(jsonPath("$.uber.data[1].name", is("newEmployee")))
				.value(jsonPath("$.uber.data[1].rel[0]", is("newEmployee")))
				.value(jsonPath("$.uber.data[1].url", is("http://localhost/employees")))
				.value(jsonPath("$.uber.data[1].action", is("append")))
				.value(jsonPath("$.uber.data[1].model", is("name={name}&role={role}")))

				.value(jsonPath("$.uber.data[2].data[0].name", is("self")))
				.value(jsonPath("$.uber.data[2].data[0].rel[0]", is("self")))
				.value(jsonPath("$.uber.data[2].data[0].rel[1]", is("findOne")))
				.value(jsonPath("$.uber.data[2].data[0].url", is("http://localhost/employees/0")))

				.value(jsonPath("$.uber.data[2].data[1].name", is("updateEmployee")))
				.value(jsonPath("$.uber.data[2].data[1].rel[0]", is("updateEmployee")))
				.value(jsonPath("$.uber.data[2].data[1].url", is("http://localhost/employees/0")))
				.value(jsonPath("$.uber.data[2].data[1].action", is("replace")))
				.value(jsonPath("$.uber.data[2].data[1].model", is("name={name}&role={role}")))

				.value(jsonPath("$.uber.data[2].data[2].name", is("partiallyUpdateEmployee")))
				.value(jsonPath("$.uber.data[2].data[2].rel[0]", is("partiallyUpdateEmployee")))
				.value(jsonPath("$.uber.data[2].data[2].url", is("http://localhost/employees/0")))
				.value(jsonPath("$.uber.data[2].data[2].action", is("partial")))
				.value(jsonPath("$.uber.data[2].data[2].model", is("name={name}&role={role}")))

				.value(jsonPath("$.uber.data[2].data[3].rel[0]", is("employees")))
				.value(jsonPath("$.uber.data[2].data[3].rel[1]", is("all")))
				.value(jsonPath("$.uber.data[2].data[3].url", is("http://localhost/employees")))

				.value(jsonPath("$.uber.data[2].data[4].name", is("employee")))
				.value(jsonPath("$.uber.data[2].data[4].data[0].name", is("role")))
				.value(jsonPath("$.uber.data[2].data[4].data[0].value", is("ring bearer")))
				.value(jsonPath("$.uber.data[2].data[4].data[1].name", is("name")))
				.value(jsonPath("$.uber.data[2].data[4].data[1].value", is("Frodo Baggins")))

				.value(jsonPath("$.uber.data[3].data[0].name", is("self")))
				.value(jsonPath("$.uber.data[3].data[0].rel[0]", is("self")))
				.value(jsonPath("$.uber.data[3].data[0].rel[1]", is("findOne")))
				.value(jsonPath("$.uber.data[3].data[0].url", is("http://localhost/employees/1")))

				.value(jsonPath("$.uber.data[3].data[1].name", is("updateEmployee")))
				.value(jsonPath("$.uber.data[3].data[1].rel[0]", is("updateEmployee")))
				.value(jsonPath("$.uber.data[3].data[1].url", is("http://localhost/employees/1")))
				.value(jsonPath("$.uber.data[3].data[1].action", is("replace")))
				.value(jsonPath("$.uber.data[3].data[1].model", is("name={name}&role={role}")))

				.value(jsonPath("$.uber.data[3].data[2].name", is("partiallyUpdateEmployee")))
				.value(jsonPath("$.uber.data[3].data[2].rel[0]", is("partiallyUpdateEmployee")))
				.value(jsonPath("$.uber.data[3].data[2].url", is("http://localhost/employees/1")))
				.value(jsonPath("$.uber.data[3].data[2].action", is("partial")))
				.value(jsonPath("$.uber.data[3].data[2].model", is("name={name}&role={role}")))

				.value(jsonPath("$.uber.data[3].data[3].rel[0]", is("employees")))
				.value(jsonPath("$.uber.data[3].data[3].rel[1]", is("all")))
				.value(jsonPath("$.uber.data[3].data[3].url", is("http://localhost/employees")))

				.value(jsonPath("$.uber.data[3].data[4].name", is("employee")))
				.value(jsonPath("$.uber.data[3].data[4].data[0].name", is("role")))
				.value(jsonPath("$.uber.data[3].data[4].data[0].value", is("burglar")))
				.value(jsonPath("$.uber.data[3].data[4].data[1].name", is("name")))
				.value(jsonPath("$.uber.data[3].data[4].data[1].value", is("Bilbo Baggins")));
	}

	/**
	 * @see #728
	 */
	@Test
	void createNewEmployee() throws Exception {

		String input = read(new ClassPathResource("create-employee.json", getClass()));

		this.testClient.post().uri("http://localhost/employees") //
				.contentType(MediaTypes.UBER_JSON) //
				.bodyValue(input) //
				.exchange() //
				.expectStatus().isCreated() //
				.expectHeader().valueEquals(HttpHeaders.LOCATION, "http://localhost/employees/2");

		this.testClient.get().uri("http://localhost/employees/2") //
				.accept(MediaTypes.UBER_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(MediaTypes.UBER_JSON) //
				.expectBody(String.class) //

				.value(jsonPath("$.uber.version", is("1.0")))

				.value(jsonPath("$.uber.data.*", hasSize(5))) //
				.value(jsonPath("$.uber.data[0].name", is("self"))) //
				.value(jsonPath("$.uber.data[0].rel[0]", is("self"))) //
				.value(jsonPath("$.uber.data[0].rel[1]", is("findOne"))) //
				.value(jsonPath("$.uber.data[0].url", is("http://localhost/employees/2")))

				.value(jsonPath("$.uber.data[1].name", is("updateEmployee"))) //
				.value(jsonPath("$.uber.data[1].rel[0]", is("updateEmployee"))) //
				.value(jsonPath("$.uber.data[1].url", is("http://localhost/employees/2"))) //
				.value(jsonPath("$.uber.data[1].action", is("replace"))) //
				.value(jsonPath("$.uber.data[1].model", is("name={name}&role={role}")))

				.value(jsonPath("$.uber.data[2].name", is("partiallyUpdateEmployee"))) //
				.value(jsonPath("$.uber.data[2].rel[0]", is("partiallyUpdateEmployee"))) //
				.value(jsonPath("$.uber.data[2].url", is("http://localhost/employees/2"))) //
				.value(jsonPath("$.uber.data[2].action", is("partial"))) //
				.value(jsonPath("$.uber.data[2].model", is("name={name}&role={role}")))

				.value(jsonPath("$.uber.data[3].name", is("employees"))) //
				.value(jsonPath("$.uber.data[3].rel[0]", is("employees"))) //
				.value(jsonPath("$.uber.data[3].rel[1]", is("all"))) //
				.value(jsonPath("$.uber.data[3].url", is("http://localhost/employees")))

				.value(jsonPath("$.uber.data[4].name", is("employee"))) //
				.value(jsonPath("$.uber.data[4].data.*", hasSize(2))) //
				.value(jsonPath("$.uber.data[4].data[0].name", is("role"))) //
				.value(jsonPath("$.uber.data[4].data[0].value", is("gardener"))) //
				.value(jsonPath("$.uber.data[4].data[1].name", is("name"))) //
				.value(jsonPath("$.uber.data[4].data[1].value", is("Samwise Gamgee")));
	}

	@Configuration
	@EnableWebFlux
	@EnableHypermediaSupport(type = { HypermediaType.UBER })
	static class TestConfig {

		@Bean
		WebFluxEmployeeController employeeController() {
			return new WebFluxEmployeeController();
		}

		@Bean
		WebTestClient webTestClient(HypermediaWebTestClientConfigurer configurer, ApplicationContext ctx) {
			return WebTestClient.bindToApplicationContext(ctx).build().mutateWith(configurer);
		}
	}
}
