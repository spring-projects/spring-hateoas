/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.reactive;

import static org.assertj.core.api.Assertions.*;

import lombok.Data;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author Greg Turnquist
 */
public class SimpleReactiveResourceAssemblerTest {

	TestResourceAssemblerSimple testResourceAssembler;

	ResourceAssemblerWithCustomLinkSimple resourceAssemblerWithCustomLink;

	@Mock ServerWebExchange exchange;
	
	@Before
	public void setUp() {

		this.testResourceAssembler = new TestResourceAssemblerSimple();
		this.resourceAssemblerWithCustomLink = new ResourceAssemblerWithCustomLinkSimple();
	}

	/**
	 * @see  #728
	 */
	@Test
	public void convertingToResourceShouldWork() {

		this.testResourceAssembler.toResource(new Employee("Frodo"), this.exchange)
			.as(StepVerifier::create)
			.expectNextMatches(resource -> {

				assertThat(resource.getContent().getName()).isEqualTo("Frodo");
				assertThat(resource.getLinks()).isEmpty();
				return true;
			})
			.verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	public void convertingToResourcesShouldWork() {

		this.testResourceAssembler.toResources(Flux.just(new Employee("Frodo")), this.exchange)
			.as(StepVerifier::create)
			.expectNextMatches(resources -> {

				assertThat(resources.getContent()).containsExactly(new Resource<>(new Employee("Frodo")));
				assertThat(resources.getLinks()).isEmpty();

				return true;
			});
	}

	/**
	 * @see #728
	 */
	@Test
	public void convertingToResourceWithCustomLinksShouldWork() {

		this.resourceAssemblerWithCustomLink.toResource(new Employee("Frodo"), this.exchange)
			.as(StepVerifier::create)
			.expectNextMatches(resource -> {

				assertThat(resource.getContent().getName()).isEqualTo("Frodo");
				assertThat(resource.getLinks()).containsExactly(new Link("/employees").withRel("employees"));
				
				return true;
			})
			.verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	public void convertingToResourcesWithCustomLinksShouldWork() {

		this.resourceAssemblerWithCustomLink.toResources(Flux.just(new Employee("Frodo")), this.exchange)
			.as(StepVerifier::create)
			.expectNextMatches(resources -> {

				assertThat(resources.getContent()).containsExactly(
					new Resource<>(new Employee("Frodo"), new Link("/employees").withRel("employees")));
				assertThat(resources.getLinks()).containsExactly(new Link("/", "root"));

				return true;
			})
			.verifyComplete();
	}

	class TestResourceAssemblerSimple implements SimpleReactiveResourceAssembler<Employee> {

		@Override
		public void addLinks(Resource<Employee> resource, ServerWebExchange exchange) {
		}

		@Override
		public void addLinks(Resources<Resource<Employee>> resources, ServerWebExchange exchange) {
		}
	}

	class ResourceAssemblerWithCustomLinkSimple implements SimpleReactiveResourceAssembler<Employee> {

		@Override
		public void addLinks(Resource<Employee> resource, ServerWebExchange exchange) {
			resource.add(new Link("/employees").withRel("employees"));
		}

		@Override
		public void addLinks(Resources<Resource<Employee>> resources, ServerWebExchange exchange) {
			resources.add(new Link("/").withRel("root"));
		}
	}

	@Data
	class Employee {

		private final String name;
	}
}
