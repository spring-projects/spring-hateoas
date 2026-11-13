/*
 * Copyright 2017-2024 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;
import static org.springframework.hateoas.support.JsonPathUtils.*;

import reactor.core.publisher.Mono;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.HypermediaWebTestClientConfigurer;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.hateoas.support.Employee;
import org.springframework.hateoas.support.WebFluxEmployeeController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Greg Turnquist
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
class HalFormsWebFluxIntegrationTest {

	@Autowired WebTestClient testClient;
	ContextualMapper $ = MappingTestUtils.createMapper();

	@BeforeEach
	void setUp() {
		WebFluxEmployeeController.reset();
	}

	/**
	 * @see #728
	 */
	@Test
	void singleEmployee() {

		testClient.get().uri("http://localhost/employees/0").accept(MediaTypes.HAL_FORMS_JSON).exchange()

				.expectStatus().isOk() //
				.expectHeader().contentType(MediaTypes.HAL_FORMS_JSON) //
				.expectBody(String.class) //

				.value(jsonPath("$.name", is("Frodo Baggins"))) //
				.value(jsonPath("$.role", is("ring bearer"))) //

				.value(jsonPath("$._links.*", hasSize(2))) //
				.value(jsonPath("$._links['self'].href", is("http://localhost/employees/0"))) //
				.value(jsonPath("$._links['employees'].href", is("http://localhost/employees"))) //

				.value(jsonPath("$._templates.*", hasSize(2))) //
				.value(jsonPath("$._templates['updateEmployee'].method", is("PUT"))) //
				.value(jsonPath("$._templates['updateEmployee'].properties[0].name", is("name"))) //
				.value(jsonPath("$._templates['updateEmployee'].properties[0].required", is(true))) //
				.value(jsonPath("$._templates['updateEmployee'].properties[1].name", is("role"))) //
				.value(jsonPath("$._templates['updateEmployee'].properties[1].required").doesNotExist()) //

				.value(jsonPath("$._templates['partiallyUpdateEmployee'].method", is("PATCH"))) //
				.value(jsonPath("$._templates['partiallyUpdateEmployee'].properties[0].name", is("name"))) //
				.value(jsonPath("$._templates['partiallyUpdateEmployee'].properties[0].required").doesNotExist()) //
				.value(jsonPath("$._templates['partiallyUpdateEmployee'].properties[1].name", is("role"))) //
				.value(jsonPath("$._templates['partiallyUpdateEmployee'].properties[1].required").doesNotExist());
	}

	/**
	 * @see #728
	 */
	@Test
	void collectionOfEmployees() {

		testClient.get().uri("http://localhost/employees").accept(MediaTypes.HAL_FORMS_JSON).exchange().expectStatus()
				.isOk().expectHeader().contentType(MediaTypes.HAL_FORMS_JSON).expectBody(String.class)
				.value(jsonPath("$._embedded.employees[0].name", is("Frodo Baggins")))
				.value(jsonPath("$._embedded.employees[0].role", is("ring bearer")))
				.value(jsonPath("$._embedded.employees[0]._links['self'].href", is("http://localhost/employees/0")))
				.value(jsonPath("$._embedded.employees[1].name", is("Bilbo Baggins")))
				.value(jsonPath("$._embedded.employees[1].role", is("burglar")))
				.value(jsonPath("$._embedded.employees[1]._links['self'].href", is("http://localhost/employees/1")))

				.value(jsonPath("$._links.*", hasSize(1)))
				.value(jsonPath("$._links['self'].href", is("http://localhost/employees")))

				.value(jsonPath("$._templates.*", hasSize(1)))
				.value(jsonPath("$._templates['newEmployee'].method", is("POST")))
				.value(jsonPath("$._templates['newEmployee'].properties[0].name", is("name")))
				.value(jsonPath("$._templates['newEmployee'].properties[0].required", is(true)))
				.value(jsonPath("$._templates['newEmployee'].properties[1].name", is("role")))
				.value(jsonPath("$._templates['newEmployee'].properties[1].required").doesNotExist());
	}

	/**
	 * @see #728
	 */
	@Test
	void createNewEmployee() throws Exception {

		$.assertFileContent("new-employee.json").satisfies(specBasedJson -> {

			testClient.post().uri("http://localhost/employees").contentType(MediaTypes.HAL_FORMS_JSON)
					.bodyValue(specBasedJson) //
					.exchange() //
					.expectStatus().isCreated() //
					.expectHeader().valueEquals(HttpHeaders.LOCATION, "http://localhost/employees/2");
		});
	}

	@Test
	void rendersQueryAffordanceWithRequestBodyProperties() {

		testClient.get().uri("http://localhost/query-form").accept(MediaTypes.HAL_FORMS_JSON).exchange() //
				.expectStatus().isOk() //
				.expectBody(String.class) //
				.value(jsonPath("$._templates.*", hasSize(1))) //
				.value(jsonPath("$._templates.query.method", is("QUERY"))) //
				.value(jsonPath("$._templates.query.contentType", is("application/json"))) //
				.value(jsonPath("$._templates.query.target", is("http://localhost/query-form/search"))) //
				.value(jsonPath("$._templates.query.properties", hasSize(2))) //
				.value(jsonPath("$._templates.query.properties[0].name", is("name"))) //
				.value(jsonPath("$._templates.query.properties[0].required", is(true))) //
				.value(jsonPath("$._templates.query.properties[1].name", is("role"))) //
				.value(jsonPath("$._templates.query.properties[1].required").doesNotExist());
	}

	@Test // #786
	void problemReturningControllerMethod() {

		var problem = testClient.get().uri("http://localhost/employees/problem")
				.accept(MediaTypes.HTTP_PROBLEM_DETAILS_JSON) //
				.exchange() //
				.expectStatus().isBadRequest() //
				.expectHeader().contentType(MediaTypes.HTTP_PROBLEM_DETAILS_JSON) //
				.expectBody(Problem.class) //
				.returnResult().getResponseBody();

		assertThat(problem).isNotNull();
		assertThat(problem.getType()).isEqualTo(URI.create("http://example.com/problem"));
		assertThat(problem.getTitle()).isEqualTo("Employee-based problem");
		assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(problem.getDetail()).isEqualTo("This is a test case");
	}

	@Configuration
	@EnableWebFlux
	@EnableHypermediaSupport(type = { HypermediaType.HAL_FORMS })
	static class TestConfig {

		@Bean
		WebFluxEmployeeController employeeController() {
			return new WebFluxEmployeeController();
		}

		@Bean
		QueryController queryController() {
			return new QueryController();
		}

		@Bean
		WebTestClient webTestClient(HypermediaWebTestClientConfigurer configurer, ApplicationContext ctx) {
			return WebTestClient.bindToApplicationContext(ctx).build().mutateWith(configurer);
		}
	}

	@RestController
	static class QueryController {

		@GetMapping("/query-form")
		public Mono<RepresentationModel<?>> form() {

			var controller = methodOn(QueryController.class);

			return linkTo(controller.form()).withSelfRel() //
					.andAffordance(controller.query(null)) //
					.toMono() //
					.map(link -> new RepresentationModel<>().add(link));
		}

		@RequestMapping(path = "/query-form/search", method = RequestMethod.QUERY,
				consumes = MediaType.APPLICATION_JSON_VALUE)
		public Mono<EntityModel<Employee>> query(@RequestBody Employee criteria) {
			return Mono.just(EntityModel.of(criteria));
		}
	}
}
