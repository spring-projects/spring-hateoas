/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.config.reactive;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.IanaLinkRelation;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.SimpleResourceAssembler;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.mvc.TypeReferences.ResourceType;
import org.springframework.hateoas.mvc.TypeReferences.ResourcesType;
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
public class HypermediaWebFluxConfigurerTest {

	ParameterizedTypeReference<Resource<Employee>> resourceEmployeeType = new ResourceType<Employee>() {};
	ParameterizedTypeReference<Resources<Resource<Employee>>> resourcesEmployeeType = new ResourcesType<Resource<Employee>>() {};

	WebTestClient testClient;

	void setUp(Class<?> context) {

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(context);
		ctx.refresh();

		WebClientConfigurer webClientConfigurer = ctx.getBean(WebClientConfigurer.class);

		this.testClient = WebTestClient.bindToApplicationContext(ctx).build()
			.mutate()
			.exchangeStrategies(webClientConfigurer.hypermediaExchangeStrategies())
			.build();
	}

	/**
	 * @see  #728
	 */
	@Test
	public void registeringHalShouldServeHal() {

		setUp(HalWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
	}

	/**
	 * @see  #728
	 */
	@Test
	public void registeringHalFormsShouldServeHalForms() {

		setUp(HalFormsWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * @see  #728
	 */
	@Test
	public void registeringCollectionJsonShouldServerCollectionJson() {

		setUp(CollectionJsonWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.COLLECTION_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.COLLECTION_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.COLLECTION_JSON);
	}

	/**
	 * @see  #728
	 */
	@Test
	public void registeringUberShouldServerUber() {

		setUp(UberWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.UBER_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.UBER_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.UBER_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.UBER_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.UBER_JSON);
	}

	/**
	 * @see  #728
	 */
	@Test
	public void registeringHalAndHalFormsShouldServerHalAndHalForms() {

		setUp(AllHalWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * @see  #728
	 */
	@Test
	public void registeringHalAndHalFormsShouldAllowCreatingViaHalAndHalForms() {

		setUp(AllHalWebFluxConfig.class);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		
		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * @see  #728
	 */
	@Test
	public void registeringHalAndCollectionJsonShouldServerHalAndCollectionJson() {

		setUp(HalAndCollectionJsonWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);

		verifyRootUriServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.COLLECTION_JSON);
	}

	/**
	 * @see  #728
	 */
	@Test
	public void registeringHalAndCollectionJsonShouldAllowCreatingViaHalAndCollectionJson() {

		setUp(HalAndCollectionJsonWebFluxConfig.class);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_FORMS_JSON);
		
		verifyCreatingNewEntityWorks(MediaTypes.COLLECTION_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.COLLECTION_JSON);
	}

	/**
	 * @see  #728
	 */
	@Test
	public void registeringAllHypermediaTypesShouldServerThemAll() {

		setUp(AllHypermediaTypesWebFluxConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);

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
	 * @see  #728
	 */
	@Test
	public void registeringAllHypermediaTypesShouldAllowCreatingThroughAllFormats() {

		setUp(AllHypermediaTypesWebFluxConfig.class);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.HAL_FORMS_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.COLLECTION_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.COLLECTION_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.UBER_JSON);
		verifyCreatingNewEntityReactivelyShouldWork(MediaTypes.UBER_JSON);
	}

	/**
	 * @see  #728
	 */
	@Test
	public void callingForUnregisteredMediaTypeShouldFail() {

		setUp(HalWebFluxConfig.class);

		this.testClient.get().uri("/")
			.accept(MediaTypes.UBER_JSON)
			.exchange()
			.expectStatus().is4xxClientError()
			.returnResult(String.class)
			.getResponseBody()
			.as(StepVerifier::create)
			.verifyComplete();
	}

	/**
	 * @see  #728
	 */
	@Test
	public void reactorTypesShouldWork() {

		setUp(HalWebFluxConfig.class);

		this.testClient.get().uri("/reactive")
			.accept(MediaTypes.HAL_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaTypes.HAL_JSON_UTF8)
			.returnResult(ResourceSupport.class)
			.getResponseBody()
			.as(StepVerifier::create)
			.expectNextMatches(resourceSupport -> {

				assertThat(resourceSupport.getLinks()).containsExactlyInAnyOrder(
					new Link("/", IanaLinkRelation.SELF.value()),
					new Link("/employees", "employees")
				);
				
				return true;
			})
			.verifyComplete();

		this.testClient.get().uri("/reactive/employees")
			.accept(MediaTypes.HAL_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaTypes.HAL_JSON_UTF8)
			.returnResult(this.resourcesEmployeeType)
			.getResponseBody()
			.as(StepVerifier::create)
			.expectNextMatches(resources -> {

				assertThat(resources.getContent()).extracting("content").containsExactlyInAnyOrder(
					new Employee("Frodo Baggins", "ring bearer"));

				assertThat(resources.getContent()).extracting("links").containsExactlyInAnyOrder(
					Arrays.asList(
						new Link("/employees/1", IanaLinkRelation.SELF.value()),
						new Link("/employees", "employees")));

				assertThat(resources.getLinks()).containsExactlyInAnyOrder(
					new Link("/employees", IanaLinkRelation.SELF.value()));

				return true;
			})
			.verifyComplete();

		this.testClient.get().uri("/reactive/employees/1")
			.accept(MediaTypes.HAL_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaTypes.HAL_JSON_UTF8)
			.returnResult(this.resourceEmployeeType)
			.getResponseBody()
			.as(StepVerifier::create)
			.expectNextMatches(employee -> {

				assertThat(employee.getContent()).isEqualTo(new Employee("Frodo Baggins", "ring bearer"));
				
				assertThat(employee.getLinks()).containsExactlyInAnyOrder(
					new Link("/employees/1", IanaLinkRelation.SELF.value()),
					new Link("/employees", "employees")
				);
				return true;
			})
			.verifyComplete();
	}

	private void verifyRootUriServesHypermedia(MediaType mediaType) {
		verifyRootUriServesHypermedia(mediaType, mediaType);
	}

	private void verifyRootUriServesHypermedia(MediaType requestType, MediaType responseType) {

		this.testClient.get().uri("/")
			.accept(requestType)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(responseType)
			.returnResult(ResourceSupport.class)
			.getResponseBody()
			.as(StepVerifier::create)
			.expectNextMatches(resourceSupport -> {

				assertThat(resourceSupport.getLinks()).containsExactlyInAnyOrder(
					new Link("/", IanaLinkRelation.SELF.value()),
					new Link("/employees", "employees"));

				return true;
			})
			.verifyComplete();
	}

	private void verifyAggregateRootServesHypermedia(MediaType mediaType) {
		verifyAggregateRootServesHypermedia(mediaType, mediaType);
	}

	private void verifyAggregateRootServesHypermedia(MediaType requestType, MediaType responseType) {

		this.testClient.get().uri("/employees")
			.accept(requestType)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(responseType)
			.returnResult(this.resourcesEmployeeType)
			.getResponseBody()
			.as(StepVerifier::create)
			.expectNextMatches(resources -> {

				assertThat(resources.getContent()).hasSize(1);

				assertThat(resources.getContent()).extracting("content").containsExactlyInAnyOrder(
					new Employee("Frodo Baggins", "ring bearer")
				);

				assertThat(resources.getContent()).extracting("links").containsExactlyInAnyOrder(
					Arrays.asList(
						new Link("/employees/1", IanaLinkRelation.SELF.value()),
						new Link("/employees", "employees"))
				);

				assertThat(resources.getLinks()).containsExactlyInAnyOrder(
					new Link("/employees", IanaLinkRelation.SELF.value())
				);

				return true;
			})
			.verifyComplete();
	}

	private void verifySingleItemResourceServesHypermedia(MediaType mediaType) {
		verifySingleItemResourceServesHypermedia(mediaType, mediaType);
	}

	private void verifySingleItemResourceServesHypermedia(MediaType requestType, MediaType responseType) {

		this.testClient.get().uri("/employees/1")
			.accept(requestType)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(responseType)
			.returnResult(this.resourceEmployeeType)
			.getResponseBody()
			.as(StepVerifier::create)
			.expectNextMatches(employeeResource -> {

				assertThat(employeeResource.getContent()).isEqualTo(
					new Employee("Frodo Baggins", "ring bearer"));

				assertThat(employeeResource.getLinks()).containsExactlyInAnyOrder(
					new Link("/employees/1", IanaLinkRelation.SELF.value()),
					new Link("/employees", "employees"));

				return true;
			})
			.verifyComplete();
	}

	private void verifyCreatingNewEntityWorks(MediaType mediaType) {
		verifyCreatingNewEntityWorks(mediaType, mediaType);
	}

	private void verifyCreatingNewEntityWorks(MediaType contentType, MediaType responseType) {

		this.testClient.post().uri("/employees")
			.accept(contentType)
			.contentType(contentType)
			.body(Mono.just(new Employee("Samwise Gamgee", "gardener")), Employee.class)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(responseType)
			.returnResult(this.resourceEmployeeType)
			.getResponseBody()
			.as(StepVerifier::create)
			.expectNextMatches(resource -> {

				assertThat(resource.getContent()).isEqualTo(new Employee("Samwise Gamgee", "gardener"));
				assertThat(resource.getLinks()).containsExactlyInAnyOrder(
					new Link("/employees/1", IanaLinkRelation.SELF.value()),
					new Link("/employees", "employees"));

				return true;
			})
			.verifyComplete();
	}

	private void verifyCreatingNewEntityReactivelyShouldWork(MediaType mediaType) {
		verifyCreatingNewEntityReactivelyShouldWork(mediaType, mediaType);
	}

	private void verifyCreatingNewEntityReactivelyShouldWork(MediaType contentType, MediaType responseType) {
		
		this.testClient.post().uri("/reactive/employees")
			.accept(contentType)
			.contentType(contentType)
			.body(Mono.just(new Employee("Samwise Gamgee", "gardener")), Employee.class)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(responseType)
			.returnResult(this.resourceEmployeeType)
			.getResponseBody()
			.as(StepVerifier::create)
			.expectNextMatches(resource -> {

				assertThat(resource.getContent()).isEqualTo(new Employee("Samwise Gamgee", "gardener"));
				assertThat(resource.getLinks()).containsExactlyInAnyOrder(
					new Link("/employees/1", IanaLinkRelation.SELF.value()),
					new Link("/employees", "employees"));
				
				return true;
			})
			.verifyComplete();
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
	static class HalWebFluxConfig extends BaseConfig {
	}

	@EnableHypermediaSupport(type = HAL_FORMS)
	static class HalFormsWebFluxConfig extends BaseConfig {
	}

	@EnableHypermediaSupport(type = COLLECTION_JSON)
	static class CollectionJsonWebFluxConfig extends BaseConfig {
	}

	@EnableHypermediaSupport(type = UBER)
	static class UberWebFluxConfig extends BaseConfig {
	}

	@EnableHypermediaSupport(type = {HAL, HAL_FORMS})
	static class AllHalWebFluxConfig extends BaseConfig {
	}

	@EnableHypermediaSupport(type = {HAL, HAL_FORMS, COLLECTION_JSON})
	static class HalAndCollectionJsonWebFluxConfig extends BaseConfig {
	}

	@EnableHypermediaSupport(type = {HAL, HAL_FORMS, COLLECTION_JSON, UBER})
	static class AllHypermediaTypesWebFluxConfig extends BaseConfig {
	}

	@RestController
	static class TestController {

		private List<Employee> employees;
		private EmployeeResourceAssembler assembler = new EmployeeResourceAssembler();

		TestController() {
			
			this.employees = new ArrayList<>();
			this.employees.add(new Employee("Frodo Baggins", "ring bearer"));
		}

		@GetMapping
		ResourceSupport root() {
			
			ResourceSupport root = new ResourceSupport();

			root.add(new Link("/").withSelfRel());
			root.add(new Link("/employees").withRel("employees"));

			return root;
		}

		@GetMapping("/employees")
		Resources<Resource<Employee>> employees() {
			return this.assembler.toResources(this.employees);
		}

		@PostMapping("/employees")
		Resource<Employee> newEmployee(@RequestBody Employee newEmployee) {

			this.employees.add(newEmployee);

			return this.assembler.toResource(newEmployee);
		}
		
		@GetMapping("/employees/{id}")
		Resource<Employee> employee(@PathVariable String id) {
			return this.assembler.toResource(this.employees.get(0));
		}

		@PutMapping("/employees/{id}")
		Resource<Employee> updateEmployee(@RequestBody Employee newEmployee, @PathVariable String id) {

			this.employees.add(newEmployee);

			return this.assembler.toResource(newEmployee);
		}

		@GetMapping("/reactive")
		Mono<ResourceSupport> reactiveRoot() {
			return Mono.just(root());
		}

		@GetMapping("/reactive/employees")
		Mono<Resources<Resource<Employee>>> reactiveEmployees() {

			return findAll()
				.collectList()
				.map(employees -> this.assembler.toResources(employees));
		}

		@PostMapping("/reactive/employees")
		Mono<Resource<Employee>> reactiveNewEmployee(@RequestBody Mono<Employee> newEmployee) {

			return newEmployee
				.map(employee -> {
					this.employees.add(employee);
					return employee;
				})
				.map(this.assembler::toResource);
		}


		@GetMapping("/reactive/employees/{id}")
		Mono<Resource<Employee>> reactiveEmployee(@PathVariable String id) {
			return findById(0)
				.map(this.assembler::toResource);
		}

		Mono<Employee> findById(int id) {
			return Mono.just(this.employees.get(id));
		}

		Flux<Employee> findAll() {
			return Flux.fromIterable(this.employees);
		}
	}

	static class EmployeeResourceAssembler implements SimpleResourceAssembler<Employee> {

		@Override
		public void addLinks(Resource<Employee> resource) {

			resource.add(new Link("/employees/1").withSelfRel());
			resource.add(new Link("/employees").withRel("employees"));
		}

		@Override
		public void addLinks(Resources<Resource<Employee>> resources) {
			resources.add(new Link("/employees").withSelfRel());
		}
	}
}