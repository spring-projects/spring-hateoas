/*
 * Copyright 2017-2018 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import lombok.Data;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Greg Turnquist
 */
public class ResourceAssemblerSupportTest {

	@Test
	public void resourceAssemblerSupportShouldConvertEntitiesToResources() {

		EmployeeAssembler assembler = new EmployeeAssembler();
		Employee employee = new Employee("Frodo");
		
		EmployeeResource resource = assembler.toResource(employee);

		assertThat(resource.getEmployee(), is(employee));
	}

	@Test
	public void resourceAssemblerSupportShouldLetYouLookUpItsTypes() {

		EmployeeAssembler assembler = new EmployeeAssembler();

		assertThat(assembler.getControllerClass().getCanonicalName(), is(EmployeeController.class.getCanonicalName()));
	}

	@Test
	public void resourceAssemblerShouldConvertCollectionOfEntitiesIntoResources() {

		EmployeeAssembler assembler = new EmployeeAssembler();
		Employee employee1 = new Employee("Frodo");
		Employee employee2 = new Employee("Bilbo");

		Resources<EmployeeResource> employees = assembler.toResources(Arrays.asList(employee1, employee2));

		assertThat(employees, is(notNullValue()));
		assertThat(employees.getContent(), hasItems(new EmployeeResource(employee1), new EmployeeResource(employee2)));
		assertThat(employees.getLinks().size(), is(0));
	}

	//
	// Support classes
	//

	class EmployeeAssembler extends ResourceAssemblerSupport<Employee, EmployeeResource> {

		EmployeeAssembler() {
			super(EmployeeController.class, EmployeeResource.class);
		}

		@Override
		public EmployeeResource toResource(Employee entity) {
			return new EmployeeResource(entity);
		}
	}

	@Data
	class Employee {
		private final String name;
	}

	@Data
	class EmployeeResource extends ResourceSupport {
		private final Employee employee;
	}

	@Controller
	class EmployeeController {

		@GetMapping("/employees/{id}")
		Employee findOne(String id) {
			return new Employee("Frodo");
		}
	}

}
