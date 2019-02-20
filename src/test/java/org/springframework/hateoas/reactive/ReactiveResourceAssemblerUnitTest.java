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
package org.springframework.hateoas.reactive;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.mockito.Mockito.*;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;

import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author Greg Turnquist
 */
public class ReactiveResourceAssemblerUnitTest {

	TestAssembler assembler;

	TestAssemblerWithCustomResources assemblerWithCustomResources;

	Employee employee;
	
	ServerWebExchange exchange;

	@Before
	public void setUp() {

		this.assembler = new TestAssembler();
		this.assemblerWithCustomResources = new TestAssemblerWithCustomResources();
		this.employee = new Employee("Frodo Baggins");
		this.exchange = mock(ServerWebExchange.class);
	}

	@Test
	public void simpleConversionShouldWork() {

		this.assembler.toResource(this.employee, this.exchange)
			.as(StepVerifier::create)
			.expectNextMatches(employeeResource -> {

				assertThat(employeeResource.getEmployee()).isEqualTo(new Employee("Frodo Baggins"));
				AssertionsForInterfaceTypes.assertThat(employeeResource.getLinks()).containsExactlyInAnyOrder(
					new Link("/employees", "employees"));

				return true;
			})
			.verifyComplete();
	}
	@Test
	public void defaultResourcesConversionShouldWork() {

		this.assembler.toResources(Flux.just(this.employee), this.exchange)
			.as(StepVerifier::create)
			.expectNextMatches(employeeResources -> {

				assertThat(employeeResources.getContent()).extracting("employee").containsExactly(
					new Employee("Frodo Baggins"));

				assertThat(employeeResources.getContent()).extracting("links").containsExactly(
					Collections.singletonList(new Link("/employees", "employees")));

				assertThat(employeeResources.getLinks()).isEmpty();

				return true;
			})
			.verifyComplete();
	}

	@Test
	public void customResourcesShouldWork() {

		this.assemblerWithCustomResources.toResources(Flux.just(this.employee), this.exchange)
			.as(StepVerifier::create)
			.expectNextMatches(employeeResources -> {

				AssertionsForInterfaceTypes.assertThat(employeeResources.getContent()).extracting("employee").containsExactly(
					new Employee("Frodo Baggins"));

				AssertionsForInterfaceTypes.assertThat(employeeResources.getContent()).extracting("links").containsExactly(
					Collections.singletonList(new Link("/employees", "employees")));

				AssertionsForInterfaceTypes.assertThat(employeeResources.getLinks()).containsExactlyInAnyOrder(
					new Link("/employees").withSelfRel(),
					new Link("/", "root")
				);

				return true;
			})
			.verifyComplete();
	}

	class TestAssembler implements ReactiveResourceAssembler<Employee, EmployeeResource> {

		@Override
		public Mono<EmployeeResource> toResource(Employee entity, ServerWebExchange exchange) {

			EmployeeResource employeeResource = new EmployeeResource(entity);
			employeeResource.add(new Link("/employees", "employees"));
			
			return Mono.just(employeeResource);
		}
	}

	class TestAssemblerWithCustomResources extends TestAssembler {

		@Override
		public Mono<Resources<EmployeeResource>> toResources(Flux<? extends Employee> entities, ServerWebExchange exchange) {

			return entities
				.flatMap(entity -> toResource(entity, exchange))
				.collectList()
				.map(listOfResources -> {

					Resources<EmployeeResource> employeeResources = new Resources<>(listOfResources);
					
					employeeResources.add(new Link("/employees").withSelfRel());
					employeeResources.add(new Link("/", "root"));

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
	@AllArgsConstructor
	class EmployeeResource extends ResourceSupport {
		private Employee employee;
	}

}
