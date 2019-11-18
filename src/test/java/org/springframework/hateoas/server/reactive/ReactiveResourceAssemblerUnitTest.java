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
package org.springframework.hateoas.server.reactive;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;

import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author Greg Turnquist
 */
class ReactiveResourceAssemblerUnitTest {

	TestAssembler assembler;

	TestAssemblerWithCustomResources assemblerWithCustomResources;

	Employee employee;

	ServerWebExchange exchange;

	@BeforeEach
	void setUp() {

		this.assembler = new TestAssembler();
		this.assemblerWithCustomResources = new TestAssemblerWithCustomResources();
		this.employee = new Employee("Frodo Baggins");
		this.exchange = mock(ServerWebExchange.class);
	}

	/**
	 * @see #728
	 */
	@Test
	void simpleConversionShouldWork() {

		this.assembler.toModel(this.employee, this.exchange).as(StepVerifier::create)
				.expectNextMatches(employeeResource -> {

					assertThat(employeeResource.getEmployee()).isEqualTo(new Employee("Frodo Baggins"));
					AssertionsForInterfaceTypes.assertThat(employeeResource.getLinks())
							.containsExactlyInAnyOrder(Link.of("/employees", "employees"));

					return true;
				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void defaultResourcesConversionShouldWork() {

		this.assembler.toCollectionModel(Flux.just(this.employee), this.exchange).as(StepVerifier::create)
				.expectNextMatches(employeeResources -> {

					Collection<EmployeeResource> content = employeeResources.getContent();

					assertThat(content) //
							.extracting("employee") //
							.containsExactly(new Employee("Frodo Baggins"));

					assertThat(content.iterator().next().getLinks()).containsExactly(Link.of("/employees", "employees"));
					assertThat(employeeResources.getLinks()).isEmpty();

					return true;
				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void customResourcesShouldWork() {

		this.assemblerWithCustomResources.toCollectionModel(Flux.just(this.employee), this.exchange) //
				.as(StepVerifier::create) //
				.expectNextMatches(employeeResources -> {

					assertThat(employeeResources.getLinks()) //
							.containsExactlyInAnyOrder(Link.of("/employees").withSelfRel(), Link.of("/", "root"));

					EmployeeResource content = employeeResources.getContent().iterator().next();

					assertThat(content.getEmployee()).isEqualTo(new Employee("Frodo Baggins"));
					assertThat(content.getLinks()).containsExactly(Link.of("/employees", "employees"));

					return true;
				}).verifyComplete();
	}

	class TestAssembler implements ReactiveRepresentationModelAssembler<Employee, EmployeeResource> {

		@Override
		public Mono<EmployeeResource> toModel(Employee entity, ServerWebExchange exchange) {

			EmployeeResource employeeResource = new EmployeeResource(entity);
			employeeResource.add(Link.of("/employees", "employees"));

			return Mono.just(employeeResource);
		}
	}

	class TestAssemblerWithCustomResources extends TestAssembler {

		@Override
		public Mono<CollectionModel<EmployeeResource>> toCollectionModel(Flux<? extends Employee> entities,
				ServerWebExchange exchange) {

			return entities.flatMap(entity -> toModel(entity, exchange)).collectList().map(listOfResources -> {

				CollectionModel<EmployeeResource> employeeResources = CollectionModel.of(
						listOfResources);

				employeeResources.add(Link.of("/employees").withSelfRel());
				employeeResources.add(Link.of("/", "root"));

				return employeeResources;
			});
		}
	}

	@Data
	@AllArgsConstructor
	class Employee {
		private String name;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	@AllArgsConstructor
	class EmployeeResource extends RepresentationModel<EmployeeResource> {
		private Employee employee;
	}
}
