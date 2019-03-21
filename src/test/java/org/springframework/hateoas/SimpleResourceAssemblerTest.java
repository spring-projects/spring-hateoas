/*
 * Copyright 2017 the original author or authors.
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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import lombok.Data;

import java.util.Arrays;

import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * @author Greg Turnquist
 */
public class SimpleResourceAssemblerTest {

	@Test
	public void convertingToResourceShouldWork() {

		TestResourceAssembler assembler = new TestResourceAssembler();
		Resource<Employee> resource = assembler.toResource(new Employee("Frodo"));

		assertThat(resource.getContent().getName(), is("Frodo"));
	}

	@Test
	public void convertingToResourcesShouldWork() {

		TestResourceAssembler assembler = new TestResourceAssembler();
		Resources<Resource<Employee>> resources = assembler.toResources(Arrays.asList(new Employee("Frodo")));

		assertThat(resources.getContent(), hasSize(1));
		assertThat(resources.getContent(), Matchers.<Resource<Employee>>contains(new Resource(new Employee("Frodo"))));
		assertThat(resources.getLinks(), is(Matchers.<Link>empty()));

		assertThat(resources.getContent().iterator().next(), is(new Resource(new Employee("Frodo"))));
	}

	@Test
	public void convertingToResourceWithCustomLinksShouldWork() {

		ResourceAssemblerWithCustomLink assembler = new ResourceAssemblerWithCustomLink();
		Resource<Employee> resource = assembler.toResource(new Employee("Frodo"));

		assertThat(resource.getContent().getName(), is("Frodo"));
		assertThat(resource.getLinks(), hasSize(1));
		assertThat(resource.getLinks(), hasItem(new Link("/employees").withRel("employees")));
	}

	@Test
	public void convertingToResourcesWithCustomLinksShouldWork() {

		ResourceAssemblerWithCustomLink assembler = new ResourceAssemblerWithCustomLink();
		Resources<Resource<Employee>> resources = assembler.toResources(Arrays.asList(new Employee("Frodo")));

		assertThat(resources.getContent(), hasSize(1));
		assertThat(resources.getContent(),
			Matchers.<Resource<Employee>>contains(new Resource(new Employee("Frodo"), new Link("/employees").withRel("employees"))));
		assertThat(resources.getLinks(), is(Matchers.<Link>empty()));

		assertThat(resources.getContent().iterator().next(),
			is(new Resource(new Employee("Frodo"), new Link("/employees").withRel("employees"))));
	}


	class TestResourceAssembler implements SimpleResourceAssembler<Employee> {}

	class ResourceAssemblerWithCustomLink implements SimpleResourceAssembler<Employee> {

		@Override
		public void addLinks(Resource<Employee> resource) {
			resource.add(new Link("/employees").withRel("employees"));
		}
	}

	@Data
	class Employee {
		private final String name;
	}

}