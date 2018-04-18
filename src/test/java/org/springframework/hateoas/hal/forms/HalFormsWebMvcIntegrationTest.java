/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.support.Employee;
import org.springframework.hateoas.support.MappingUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Greg Turnquist
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class HalFormsWebMvcIntegrationTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = webAppContextSetup(this.context).build();
	}

	@Test
	public void singleEmployee() throws Exception {

		this.mockMvc.perform(get("/employees/0").accept(MediaTypes.HAL_FORMS_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$.name", is("Frodo Baggins"))).andExpect(jsonPath("$.role", is("ring bearer")))

				.andExpect(jsonPath("$._links.*", hasSize(2)))
				.andExpect(jsonPath("$._links['self'].href", is("http://localhost/employees/0")))
				.andExpect(jsonPath("$._links['employees'].href", is("http://localhost/employees")))

				.andExpect(jsonPath("$._templates.*", hasSize(2)))
				.andExpect(jsonPath("$._templates['default'].method", is("put")))
				.andExpect(jsonPath("$._templates['default'].properties[0].name", is("name")))
				.andExpect(jsonPath("$._templates['default'].properties[0].required", is(true)))
				.andExpect(jsonPath("$._templates['default'].properties[1].name", is("role")))
				.andExpect(jsonPath("$._templates['default'].properties[1].required", is(true)))

				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].method", is("patch")))
				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].properties[0].name", is("name")))
				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].properties[0].required", is(false)))
				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].properties[1].name", is("role")))
				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].properties[1].required", is(false)));
	}

	@Test
	public void collectionOfEmployees() throws Exception {

		this.mockMvc.perform(get("/employees").accept(MediaTypes.HAL_FORMS_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$._embedded.employees[0].name", is("Frodo Baggins")))
				.andExpect(jsonPath("$._embedded.employees[0].role", is("ring bearer")))
				.andExpect(jsonPath("$._embedded.employees[0]._links['self'].href", is("http://localhost/employees/0")))
				.andExpect(jsonPath("$._embedded.employees[1].name", is("Bilbo Baggins")))
				.andExpect(jsonPath("$._embedded.employees[1].role", is("burglar")))
				.andExpect(jsonPath("$._embedded.employees[1]._links['self'].href", is("http://localhost/employees/1")))

				.andExpect(jsonPath("$._links.*", hasSize(1)))
				.andExpect(jsonPath("$._links['self'].href", is("http://localhost/employees")))

				.andExpect(jsonPath("$._templates.*", hasSize(1)))
				.andExpect(jsonPath("$._templates['default'].method", is("post")))
				.andExpect(jsonPath("$._templates['default'].properties[0].name", is("name")))
				.andExpect(jsonPath("$._templates['default'].properties[0].required", is(true)))
				.andExpect(jsonPath("$._templates['default'].properties[1].name", is("role")))
				.andExpect(jsonPath("$._templates['default'].properties[1].required", is(true)));
	}

	@Test
	public void createNewEmployee() throws Exception {

		String specBasedJson = MappingUtils.read(new ClassPathResource("new-employee.json", getClass()));

		this.mockMvc.perform(post("/employees")
			.content(specBasedJson)
			.contentType(MediaTypes.HAL_FORMS_JSON_VALUE))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(header().stringValues(HttpHeaders.LOCATION, "http://localhost/employees/2"));
	}

	@RestController
	static class EmployeeController {

		private final static Map<Integer, Employee> EMPLOYEES = new TreeMap<>();

		static {
			EMPLOYEES.put(0, new Employee("Frodo Baggins", "ring bearer"));
			EMPLOYEES.put(1, new Employee("Bilbo Baggins", "burglar"));
		}

		@GetMapping("/employees")
		public Resources<Resource<Employee>> all() {

			// Create a list of Resource<Employee>'s to return
			List<Resource<Employee>> employees = new ArrayList<>();

			// Fetch each Resource<Employee> using the controller's findOne method.
			for (int i = 0; i < EMPLOYEES.size(); i++) {
				employees.add(findOne(i));
			}

			// Generate an "Affordance" based on this method (the "self" link)
			Link selfLink = linkTo(methodOn(EmployeeController.class).all()).withSelfRel()
					.andAffordance(afford(methodOn(EmployeeController.class).newEmployee(null)));

			// Return the collection of employee resources along with the composite affordance
			return new Resources<>(employees, selfLink);
		}

		@GetMapping("/employees/{id}")
		public Resource<Employee> findOne(@PathVariable Integer id) {

			// Start the affordance with the "self" link, i.e. this method.
			Link findOneLink = linkTo(methodOn(EmployeeController.class).findOne(id)).withSelfRel();

			// Define final link as means to find entire collection.
			Link employeesLink = linkTo(methodOn(EmployeeController.class).all()).withRel("employees");

			// Return the affordance + a link back to the entire collection resource.
			return new Resource<>(EMPLOYEES.get(id),
				findOneLink.andAffordance(afford(methodOn(EmployeeController.class).updateEmployee(null, id))) //
					.andAffordance(afford(methodOn(EmployeeController.class).partiallyUpdateEmployee(null, id))),
				employeesLink);
		}

		@PostMapping("/employees")
		public ResponseEntity<?> newEmployee(@RequestBody Employee employee) {

			int newEmployeeId = EMPLOYEES.size();

			EMPLOYEES.put(newEmployeeId, employee);

			try {
				return ResponseEntity.created(new URI(findOne(newEmployeeId).getLink(Link.REL_SELF).map(link -> link.expand().getHref()).orElse("")))
					.build();
			} catch (URISyntaxException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}

		@PutMapping("/employees/{id}")
		public ResponseEntity<?> updateEmployee(@RequestBody Employee employee, @PathVariable Integer id) {

			EMPLOYEES.put(id, employee);

			try {
				return ResponseEntity.noContent().location(new URI(findOne(id).getLink(Link.REL_SELF).map(link -> link.expand().getHref()).orElse("")))
					.build();
			} catch (URISyntaxException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}

		@PatchMapping("/employees/{id}")
		public ResponseEntity<?> partiallyUpdateEmployee(@RequestBody Employee employee, @PathVariable Integer id) {

			Employee oldEmployee = EMPLOYEES.get(id);
			Employee newEmployee = oldEmployee;

			if (employee.getName() != null) {
				newEmployee = newEmployee.withName(employee.getName());
			}

			if (employee.getRole() != null) {
				newEmployee = newEmployee.withRole(employee.getRole());
			}

			EMPLOYEES.put(id, newEmployee);

			try {
				return ResponseEntity.noContent().location(new URI(findOne(id).getLink(Link.REL_SELF).map(link -> link.expand().getHref()).orElse("")))
					.build();
			} catch (URISyntaxException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.HAL_FORMS })
	static class TestConfig {

		@Bean
		EmployeeController employeeController() {
			return new EmployeeController();
		}
	}
}
