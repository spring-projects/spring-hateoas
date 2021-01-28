/*
 * Copyright 2019-2021 the original author or authors.
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

import lombok.Data;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author Greg Turnquist
 */
class SimpleReactiveResourceAssemblerTest {

	TestResourceAssemblerSimple testResourceAssembler;

	ResourceAssemblerWithCustomLinkSimple resourceAssemblerWithCustomLink;

	@Mock ServerWebExchange exchange;

	@BeforeEach
	void setUp() {

		this.testResourceAssembler = new TestResourceAssemblerSimple();
		this.resourceAssemblerWithCustomLink = new ResourceAssemblerWithCustomLinkSimple();
	}

	/**
	 * @see #728
	 */
	@Test
	void convertingToResourceShouldWork() {

		this.testResourceAssembler.toModel(new Employee("Frodo"), this.exchange).as(StepVerifier::create)
				.expectNextMatches(resource -> {

					assertThat(resource.getContent().getName()).isEqualTo("Frodo");
					assertThat(resource.getLinks()).isEmpty();
					return true;
				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void convertingToResourcesShouldWork() {

		this.testResourceAssembler.toCollectionModel(Flux.just(new Employee("Frodo")), this.exchange)
				.as(StepVerifier::create).expectNextMatches(resources -> {

					assertThat(resources.getContent()).containsExactly(EntityModel.of(new Employee("Frodo")));
					assertThat(resources.getLinks()).isEmpty();

					return true;
				});
	}

	/**
	 * @see #728
	 */
	@Test
	void convertingToResourceWithCustomLinksShouldWork() {

		this.resourceAssemblerWithCustomLink.toModel(new Employee("Frodo"), this.exchange).as(StepVerifier::create)
				.expectNextMatches(resource -> {

					assertThat(resource.getContent().getName()).isEqualTo("Frodo");
					assertThat(resource.getLinks()).containsExactly(Link.of("/employees").withRel("employees"));

					return true;
				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void convertingToResourcesWithCustomLinksShouldWork() {

		this.resourceAssemblerWithCustomLink.toCollectionModel(Flux.just(new Employee("Frodo")), this.exchange)
				.as(StepVerifier::create).expectNextMatches(resources -> {

					assertThat(resources.getContent()).containsExactly(
							EntityModel.of(new Employee("Frodo"), Link.of("/employees").withRel("employees")));
					assertThat(resources.getLinks()).containsExactly(Link.of("/", "root"));

					return true;
				}).verifyComplete();
	}

	class TestResourceAssemblerSimple implements SimpleReactiveRepresentationModelAssembler<Employee> {}

	class ResourceAssemblerWithCustomLinkSimple implements SimpleReactiveRepresentationModelAssembler<Employee> {

		@Override
		public EntityModel<Employee> addLinks(EntityModel<Employee> resource,
				ServerWebExchange exchange) {
			return resource.add(Link.of("/employees").withRel("employees"));
		}

		@Override
		public CollectionModel<EntityModel<Employee>> addLinks(
				CollectionModel<EntityModel<Employee>> resources, ServerWebExchange exchange) {
			return resources.add(Link.of("/").withRel("root"));
		}
	}

	@Data
	class Employee {

		private final String name;
	}
}
