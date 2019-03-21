/*
 * Copyright 2018 the original author or authors.
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

import org.junit.Test;

/**
 * @author Greg Turnquist
 */
public class SimpleResourceAssemblerTest {

	/**
	 * @see #572
	 */
	@Test
	public void convertingToResourceShouldWork() {

		TestResourceAssembler assembler = new TestResourceAssembler();
		Resource<Employee> resource = assembler.toResource(new Employee("Frodo"));

		assertThat(resource.getContent().getName()).isEqualTo("Frodo");
		assertThat(resource.getLinks()).isEmpty();
	}

	/**
	 * @see #572
	 */
	@Test
	public void convertingToResourcesShouldWork() {

		TestResourceAssembler assembler = new TestResourceAssembler();
		Resources<Resource<Employee>> resources = assembler.toResources(Collections.singletonList(new Employee("Frodo")));

		assertThat(resources.getContent()).containsExactly(new Resource<>(new Employee("Frodo")));
		assertThat(resources.getLinks()).isEmpty();
	}

	/**
	 * @see #572
	 */
	@Test
	public void convertingToResourceWithCustomLinksShouldWork() {

		ResourceAssemblerWithCustomLink assembler = new ResourceAssemblerWithCustomLink();
		Resource<Employee> resource = assembler.toResource(new Employee("Frodo"));

		assertThat(resource.getContent().getName()).isEqualTo("Frodo");
		assertThat(resource.getLinks()).containsExactly(new Link("/employees").withRel("employees"));
	}

	/**
	 * @see #572
	 */
	@Test
	public void convertingToResourcesWithCustomLinksShouldWork() {

		ResourceAssemblerWithCustomLink assembler = new ResourceAssemblerWithCustomLink();
		Resources<Resource<Employee>> resources = assembler.toResources(Collections.singletonList(new Employee("Frodo")));

		assertThat(resources.getContent())
				.containsExactly(new Resource<>(new Employee("Frodo"), new Link("/employees").withRel("employees")));
		assertThat(resources.getLinks()).isEmpty();
	}

	class TestResourceAssembler implements SimpleResourceAssembler<Employee> {

		@Override
		public void addLinks(Resource<Employee> resource) {
		}

		@Override
		public void addLinks(Resources<Resource<Employee>> resources) {
		}
	}

	class ResourceAssemblerWithCustomLink implements SimpleResourceAssembler<Employee> {

		@Override
		public void addLinks(Resource<Employee> resource) {
			resource.add(new Link("/employees").withRel("employees"));
		}

		@Override
		public void addLinks(Resources<Resource<Employee>> resources) {
		}
	}

	@Data
	class Employee {
		private final String name;
	}
}
