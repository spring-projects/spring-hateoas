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
import static org.junit.Assert.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test that when an {@link org.springframework.hateoas.Affordance} is included that does NOT match the self link, an
 * exception is thrown.
 * 
 * @author Greg Turnquist
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class HalFormsValidationIntegrationTest {

	@Autowired WebApplicationContext context;

	@Autowired ObjectMapper mapper;

	MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = webAppContextSetup(this.context).build();
	}

	@Test
	public void singleEmployee() throws Exception {

		Exception exception = this.mockMvc.perform(get("/employees/0").accept(MediaTypes.HAL_FORMS_JSON))
				.andExpect(status().is5xxServerError()) //
				.andReturn() //
				.getResolvedException();

		assertThat(exception.getMessage(), containsString("Affordance's URI /employees"));
		assertThat(exception.getMessage(), containsString("doesn't match self link /employees/0"));
	}

	@Test
	public void collectionOfEmployees() throws Exception {

		Exception exception = this.mockMvc.perform(get("/employees").accept(MediaTypes.HAL_FORMS_JSON)) //
				.andExpect(status().is5xxServerError()) //
				.andReturn().getResolvedException();

		assertThat(exception.getMessage(), containsString("Affordance's URI /employees/0"));
		assertThat(exception.getMessage(), containsString("doesn't match self link /employees"));
	}

	/**
	 * This controller violates HAL-FORMS spec requirements. We use it to verify the serializers can catch it.
	 */
	@RestController
	static class BadController {

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
			Link selfLink = linkTo(methodOn(BadController.class).all()).withSelfRel()
					.andAffordance(afford(methodOn(BadController.class).updateEmployee(null, 0)));

			// Return the collection of employee resources along with the composite affordance
			return new Resources<>(employees, selfLink);
		}

		@GetMapping("/employees/{id}")
		public Resource<Employee> findOne(@PathVariable Integer id) {

			// Start the affordance with the "self" link, i.e. this method.
			Link findOneLink = linkTo(methodOn(BadController.class).findOne(id)).withSelfRel();

			// Define final link as means to find entire collection.
			Link employeesLink = linkTo(methodOn(BadController.class).all()).withRel("employees")
					.andAffordance(afford(methodOn(BadController.class).newEmployee(null)));

			// Return the affordance + a link back to the entire collection resource.
			return new Resource<>(EMPLOYEES.get(id), findOneLink.andAffordances(employeesLink.getAffordances()),
				employeesLink);
		}

		@PostMapping("/employees")
		public ResponseEntity<?> newEmployee(@RequestBody Employee employee) {

			int newEmployeeId = EMPLOYEES.size();

			EMPLOYEES.put(newEmployeeId, employee);

			try {
				return ResponseEntity.noContent().location(new URI(findOne(newEmployeeId).getLink(Link.REL_SELF).map(link -> link.expand().getHref()).orElse("")))
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
		BadController employeeController() {
			return new BadController();
		}
	}
}
