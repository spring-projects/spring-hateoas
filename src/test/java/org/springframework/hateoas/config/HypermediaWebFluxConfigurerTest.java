/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.hateoas.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.hateoas.server.core.TypeReferences.CollectionModelType;
import org.springframework.hateoas.server.core.TypeReferences.EntityModelType;
import org.springframework.hateoas.support.Employee;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Greg Turnquist
 */
class HypermediaWebFluxConfigurerTest {

	EntityModelType<Employee> resourceEmployeeType = new EntityModelType<Employee>() {};
	CollectionModelType<EntityModel<Employee>> resourcesEmployeeType = new CollectionModelType<EntityModel<Employee>>() {};

	WebTestClient testClient;

	void setUp(Class<?> context) {

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(context);
		ctx.refresh();

		WebClientConfigurer webClientConfigurer = ctx.getBean(WebClientConfigurer.class);

		this.testClient = WebTestClient.bindToApplicationContext(ctx).build().mutate()
				.exchangeStrategies(webClientConfigurer.hypermediaExchangeStrategies()).build();
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalShouldServeHal() {

		setUp(HalWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalFormsShouldServeHalForms() {

		setUp(HalFormsWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringCollectionJsonShouldServerCollectionJson() {

		setUp(CollectionJsonWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.COLLECTION_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.COLLECTION_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.COLLECTION_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringUberShouldServerUber() {

		setUp(UberWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.UBER_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.UBER_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.UBER_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.UBER_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.UBER_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalAndHalFormsShouldServerHalAndHalForms() {

		setUp(AllHalWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalAndHalFormsShouldAllowCreatingViaHalAndHalForms() {

		setUp(AllHalWebFluxConfig.class);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalAndCollectionJsonShouldServerHalAndCollectionJson() {

		setUp(HalAndCollectionJsonWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);

		verifyRootUriServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.COLLECTION_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalAndCollectionJsonShouldAllowCreatingViaHalAndCollectionJson() {

		setUp(HalAndCollectionJsonWebFluxConfig.class);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_FORMS_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.COLLECTION_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.COLLECTION_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringAllHypermediaTypesShouldServerThemAll() {

		setUp(AllHypermediaTypesWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);

		verifyRootUriServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.COLLECTION_JSON);

		verifyRootUriServesHypermedia(MediaTypes.UBER_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.UBER_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.UBER_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringAllHypermediaTypesShouldAllowCreatingThroughAllFormats() {

		setUp(AllHypermediaTypesWebFluxConfig.class);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_FORMS_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.COLLECTION_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.COLLECTION_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.UBER_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.UBER_JSON);
	}

	/**
	 * When requesting an unregistered media type, fallback to Spring Framework's default JSON handler.
	 *
	 * @see #728
	 */
	@Test
	void callingForUnregisteredMediaTypeShouldFallBackToDefaultHandler() {

		setUp(HalWebFluxConfig.class);

		this.testClient.get().uri("/").accept(MediaTypes.UBER_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.returnResult(String.class).getResponseBody() //
				.as(StepVerifier::create) //
				.expectNext("{\"links\":[{\"rel\":\"self\",\"href\":\"/\"},{\"rel\":\"employees\",\"href\":\"/employees\"}]}")
				.verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void reactorTypesShouldWork() {

		setUp(HalWebFluxConfig.class);

		this.testClient.get().uri("/reactive").accept(MediaTypes.HAL_JSON).exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(MediaTypes.HAL_JSON) //
				.returnResult(RepresentationModel.class).getResponseBody().as(StepVerifier::create)
				.expectNextMatches(resourceSupport -> {

					assertThat(resourceSupport.getLinks()).containsExactlyInAnyOrder(Link.of("/", IanaLinkRelations.SELF),
							Link.of("/employees", "employees"));

					return true;
				}).verifyComplete();

		this.testClient.get().uri("/reactive/employees").accept(MediaTypes.HAL_JSON).exchange() //
				.expectStatus().isOk().expectHeader().contentType(MediaTypes.HAL_JSON) //
				.returnResult(this.resourcesEmployeeType).getResponseBody() //
				.as(StepVerifier::create).expectNextMatches(resources -> {

					assertThat(resources.getLinks()).containsExactlyInAnyOrder(Link.of("/employees", IanaLinkRelations.SELF));

					EntityModel<Employee> content = resources.getContent().iterator().next();

					assertThat(content.getContent()).isEqualTo(new Employee("Frodo Baggins", "ring bearer"));
					assertThat(content.getLinks()) //
							.containsExactlyInAnyOrder(Link.of("/employees/1", IanaLinkRelations.SELF),
									Link.of("/employees", "employees"));

					return true;
				}).verifyComplete();

		this.testClient.get().uri("/reactive/employees/1").accept(MediaTypes.HAL_JSON).exchange() //
				.expectStatus().isOk().expectHeader().contentType(MediaTypes.HAL_JSON) //
				.returnResult(this.resourceEmployeeType).getResponseBody() //
				.as(StepVerifier::create).expectNextMatches(employee -> {

					assertThat(employee.getContent()).isEqualTo(new Employee("Frodo Baggins", "ring bearer"));
					assertThat(employee.getLinks()) //
							.containsExactlyInAnyOrder(Link.of("/employees/1", IanaLinkRelations.SELF),
									Link.of("/employees", "employees"));
					return true;
				}).verifyComplete();
	}

	private void verifyRootUriServesHypermedia(MediaType mediaType) {
		verifyRootUriServesHypermedia(mediaType, mediaType);
	}

	private void verifyRootUriServesHypermedia(MediaType requestType, MediaType responseType) {

		this.testClient.get().uri("/").accept(requestType).exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(responseType) //
				.returnResult(RepresentationModel.class).getResponseBody().as(StepVerifier::create)
				.expectNextMatches(resourceSupport -> {

					assertThat(resourceSupport.getLinks()) //
							.containsExactlyInAnyOrder(Link.of("/", IanaLinkRelations.SELF), Link.of("/employees", "employees"));

					return true;
				}).verifyComplete();
	}

	private void verifyAggregateRootServesHypermedia(MediaType mediaType) {
		verifyAggregateRootServesHypermedia(mediaType, mediaType);
	}

	private void verifyAggregateRootServesHypermedia(MediaType requestType, MediaType responseType) {

		this.testClient.get().uri("/employees").accept(requestType).exchange().expectStatus().isOk().expectHeader()
				.contentType(responseType).returnResult(this.resourcesEmployeeType).getResponseBody().as(StepVerifier::create)
				.expectNextMatches(resources -> {

					assertThat(resources.getLinks()).containsExactlyInAnyOrder(Link.of("/employees", IanaLinkRelations.SELF));

					Collection<EntityModel<Employee>> content = resources.getContent();
					assertThat(content).hasSize(1);

					EntityModel<Employee> resource = content.iterator().next();

					assertThat(resource.getContent()).isEqualTo(new Employee("Frodo Baggins", "ring bearer"));
					assertThat(resource.getLinks()) //
							.containsExactlyInAnyOrder(Link.of("/employees/1", IanaLinkRelations.SELF),
									Link.of("/employees", "employees"));

					return true;
				}).verifyComplete();
	}

	private void verifySingleItemResourceServesHypermedia(MediaType mediaType) {
		verifySingleItemResourceServesHypermedia(mediaType, mediaType);
	}

	private void verifySingleItemResourceServesHypermedia(MediaType requestType, MediaType responseType) {

		this.testClient.get().uri("/employees/1") //
				.accept(requestType).exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(responseType) //
				.returnResult(this.resourceEmployeeType).getResponseBody().as(StepVerifier::create) //
				.expectNextMatches(employeeResource -> {

					assertThat(employeeResource.getContent()).isEqualTo(new Employee("Frodo Baggins", "ring bearer"));
					assertThat(employeeResource.getLinks()).containsExactlyInAnyOrder(
							Link.of("/employees/1", IanaLinkRelations.SELF), Link.of("/employees", "employees"));

					return true;
				}).verifyComplete();
	}

	private void verifyCreatingNewEntityWorks(MediaType mediaType) {
		verifyCreatingNewEntityWorks(mediaType, mediaType);
	}

	private void verifyCreatingNewEntityWorks(MediaType contentType, MediaType responseType) {
		verifyCreation("/employees", contentType, responseType);
	}

	private void verifyCreatingNewEntityReactivelyShouldWork(MediaType contentType) {
		verifyCreatingNewEntityReactivelyShouldWork(contentType, contentType);
	}

	private void verifyCreatingNewEntityReactivelyShouldWork(MediaType contentType, MediaType responseType) {
		verifyCreation("/reactive/employees", contentType, responseType);
	}

	private void verifyCreation(String uri, MediaType contentType, MediaType responseType) {

		this.testClient.post().uri(uri) //
				.accept(contentType).contentType(contentType)
				.body(Mono.just(new Employee("Samwise Gamgee", "gardener")), Employee.class) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(responseType).returnResult(this.resourceEmployeeType) //
				.getResponseBody().as(StepVerifier::create).expectNextMatches(resource -> {

					assertThat(resource.getContent()).isEqualTo(new Employee("Samwise Gamgee", "gardener"));
					assertThat(resource.getLinks()) //
							.containsExactlyInAnyOrder(Link.of("/employees/1", IanaLinkRelations.SELF),
									Link.of("/employees", "employees"));

					return true;
				}).verifyComplete();
	}

	@Configuration
	@EnableWebFlux
	static abstract class BaseConfig {

		@Bean
		TestController testController() {
			return new TestController();
		}
	}

	@EnableHypermediaSupport(type = HAL)
	static class HalWebFluxConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = HAL_FORMS)
	static class HalFormsWebFluxConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = COLLECTION_JSON)
	static class CollectionJsonWebFluxConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = UBER)
	static class UberWebFluxConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = { HAL, HAL_FORMS })
	static class AllHalWebFluxConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = { HAL, HAL_FORMS, COLLECTION_JSON })
	static class HalAndCollectionJsonWebFluxConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = { HAL, HAL_FORMS, COLLECTION_JSON, UBER })
	static class AllHypermediaTypesWebFluxConfig extends BaseConfig {}

	@RestController
	static class TestController {

		private List<Employee> employees;
		private EmployeeResourceAssembler assembler = new EmployeeResourceAssembler();

		TestController() {

			this.employees = new ArrayList<>();
			this.employees.add(new Employee("Frodo Baggins", "ring bearer"));
		}

		@GetMapping
		RepresentationModel<?> root() {

			RepresentationModel<?> root = new RepresentationModel<>();

			root.add(Link.of("/").withSelfRel());
			root.add(Link.of("/employees").withRel("employees"));

			return root;
		}

		@GetMapping("/employees")
		CollectionModel<EntityModel<Employee>> employees() {
			return this.assembler.toCollectionModel(this.employees);
		}

		@PostMapping("/employees")
		EntityModel<Employee> newEmployee(@RequestBody Employee newEmployee) {

			this.employees.add(newEmployee);

			return this.assembler.toModel(newEmployee);
		}

		@GetMapping("/employees/{id}")
		EntityModel<Employee> employee(@PathVariable String id) {
			return this.assembler.toModel(this.employees.get(0));
		}

		@PutMapping("/employees/{id}")
		EntityModel<Employee> updateEmployee(@RequestBody Employee newEmployee, @PathVariable String id) {

			this.employees.add(newEmployee);

			return this.assembler.toModel(newEmployee);
		}

		@GetMapping("/reactive")
		Mono<RepresentationModel<?>> reactiveRoot() {
			return Mono.just(root());
		}

		@GetMapping("/reactive/employees")
		Mono<CollectionModel<EntityModel<Employee>>> reactiveEmployees() {

			return findAll() //
					.collectList() //
					.map(assembler::toCollectionModel);
		}

		@PostMapping("/reactive/employees")
		Mono<EntityModel<Employee>> createReactiveEmployee(@RequestBody Mono<Employee> newEmployee) {

			return newEmployee.map(employee -> {
				employees.add(employee);
				return employee;
			}).map(assembler::toModel);
		}

		@GetMapping("/reactive/employees/{id}")
		Mono<EntityModel<Employee>> reactiveEmployee(@PathVariable String id) {
			return findById(0) //
					.map(assembler::toModel);
		}

		Mono<Employee> findById(int id) {
			return Mono.just(this.employees.get(id));
		}

		Flux<Employee> findAll() {
			return Flux.fromIterable(this.employees);
		}
	}

	static class EmployeeResourceAssembler implements SimpleRepresentationModelAssembler<Employee> {

		@Override
		public void addLinks(EntityModel<Employee> resource) {

			resource.add(Link.of("/employees/1").withSelfRel());
			resource.add(Link.of("/employees").withRel("employees"));
		}

		@Override
		public void addLinks(CollectionModel<EntityModel<Employee>> resources) {
			resources.add(Link.of("/employees").withSelfRel());
		}
	}

}
