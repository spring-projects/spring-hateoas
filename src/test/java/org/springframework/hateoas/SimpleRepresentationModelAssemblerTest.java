/*
 * Copyright 2018-2020 the original author or authors.
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

package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import lombok.Data;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;

/**
 * @author Greg Turnquist
 */
class SimpleRepresentationModelAssemblerTest {

	/**
	 * @see #572
	 */
	@Test
	void convertingToResourceShouldWork() {

		TestResourceAssembler assembler = new TestResourceAssembler();
		EntityModel<Employee> resource = assembler.toModel(new Employee("Frodo"));

		assertThat(resource.getContent().getName()).isEqualTo("Frodo");
		assertThat(resource.getLinks()).isEmpty();
	}

	/**
	 * @see #572
	 */
	@Test
	void convertingToResourcesShouldWork() {

		TestResourceAssembler assembler = new TestResourceAssembler();
		CollectionModel<EntityModel<Employee>> resources = assembler
				.toCollectionModel(Collections.singletonList(new Employee("Frodo")));

		assertThat(resources.getContent()).containsExactly(EntityModel.of(new Employee("Frodo")));
		assertThat(resources.getLinks()).isEmpty();
	}

	/**
	 * @see #572
	 */
	@Test
	void convertingToResourceWithCustomLinksShouldWork() {

		ResourceAssemblerWithCustomLink assembler = new ResourceAssemblerWithCustomLink();
		EntityModel<Employee> resource = assembler.toModel(new Employee("Frodo"));

		assertThat(resource.getContent().getName()).isEqualTo("Frodo");
		assertThat(resource.getLinks()).containsExactly(Link.of("/employees").withRel("employees"));
	}

	/**
	 * @see #572
	 */
	@Test
	void convertingToResourcesWithCustomLinksShouldWork() {

		ResourceAssemblerWithCustomLink assembler = new ResourceAssemblerWithCustomLink();
		CollectionModel<EntityModel<Employee>> resources = assembler
				.toCollectionModel(Collections.singletonList(new Employee("Frodo")));

		assertThat(resources.getContent()).containsExactly(
				EntityModel.of(new Employee("Frodo"), Link.of("/employees").withRel("employees")));
		assertThat(resources.getLinks()).isEmpty();
	}

	class TestResourceAssembler implements SimpleRepresentationModelAssembler<Employee> {

		@Override
		public void addLinks(EntityModel<Employee> resource) {}

		@Override
		public void addLinks(CollectionModel<EntityModel<Employee>> resources) {}
	}

	class ResourceAssemblerWithCustomLink implements SimpleRepresentationModelAssembler<Employee> {

		@Override
		public void addLinks(EntityModel<Employee> resource) {
			resource.add(Link.of("/employees").withRel("employees"));
		}

		@Override
		public void addLinks(CollectionModel<EntityModel<Employee>> resources) {}
	}

	@Data
	class Employee {
		private final String name;
	}
}
