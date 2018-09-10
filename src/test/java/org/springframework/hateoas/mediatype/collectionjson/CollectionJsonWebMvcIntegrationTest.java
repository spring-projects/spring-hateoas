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
package org.springframework.hateoas.mediatype.collectionjson;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.MediaTypes;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Greg Turnquist
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class CollectionJsonWebMvcIntegrationTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	private static Map<Integer, Employee> EMPLOYEES;

	@Before
	public void setUp() {

		this.mockMvc = webAppContextSetup(this.context).build();

		EMPLOYEES = new TreeMap<>();

		EMPLOYEES.put(0, new Employee("Frodo Baggins", "ring bearer"));
		EMPLOYEES.put(1, new Employee("Bilbo Baggins", "burglar"));
	}

	@Test
	public void singleEmployee() throws Exception {

		this.mockMvc.perform(get("/employees/0") //
				.accept(MediaTypes.COLLECTION_JSON_VALUE)) //
				.andExpect(status().isOk()) //

				.andExpect(jsonPath("$.collection.version", is("1.0")))
				.andExpect(jsonPath("$.collection.href", is("http://localhost/employees/0")))

				.andExpect(jsonPath("$.collection.links.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.links[0].rel", is("employees")))
				.andExpect(jsonPath("$.collection.links[0].href", is("http://localhost/employees")))

				.andExpect(jsonPath("$.collection.items.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.items[0].data[1].name", is("name")))
				.andExpect(jsonPath("$.collection.items[0].data[1].value", is("Frodo Baggins")))
				.andExpect(jsonPath("$.collection.items[0].data[0].name", is("role")))
				.andExpect(jsonPath("$.collection.items[0].data[0].value", is("ring bearer")))

				.andExpect(jsonPath("$.collection.items[0].links.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.items[0].links[0].rel", is("employees")))
				.andExpect(jsonPath("$.collection.items[0].links[0].href", is("http://localhost/employees")))

				.andExpect(jsonPath("$.collection.template.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.template.data[0].name", is("name")))
				.andExpect(jsonPath("$.collection.template.data[0].value", is("")))
				.andExpect(jsonPath("$.collection.template.data[1].name", is("role")))
				.andExpect(jsonPath("$.collection.template.data[1].value", is("")));
	}

	@Test
	public void collectionOfEmployees() throws Exception {

		this.mockMvc.perform(get("/employees") //
				.accept(MediaTypes.COLLECTION_JSON_VALUE)) //
				.andExpect(status().isOk()) //

				.andExpect(jsonPath("$.collection.version", is("1.0")))
				.andExpect(jsonPath("$.collection.href", is("http://localhost/employees")))

				.andExpect(jsonPath("$.collection.items.*", hasSize(2)))
				.andExpect(jsonPath("$.collection.items[0].data[1].name", is("name")))
				.andExpect(jsonPath("$.collection.items[0].data[1].value", is("Frodo Baggins")))
				.andExpect(jsonPath("$.collection.items[0].data[0].name", is("role")))
				.andExpect(jsonPath("$.collection.items[0].data[0].value", is("ring bearer")))

				.andExpect(jsonPath("$.collection.items[0].links.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.items[0].links[0].rel", is("employees")))
				.andExpect(jsonPath("$.collection.items[0].links[0].href", is("http://localhost/employees")))

				.andExpect(jsonPath("$.collection.items[1].data[1].name", is("name")))
				.andExpect(jsonPath("$.collection.items[1].data[1].value", is("Bilbo Baggins")))
				.andExpect(jsonPath("$.collection.items[1].data[0].name", is("role")))
				.andExpect(jsonPath("$.collection.items[1].data[0].value", is("burglar")))

				.andExpect(jsonPath("$.collection.items[1].links.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.items[1].links[0].rel", is("employees")))
				.andExpect(jsonPath("$.collection.items[1].links[0].href", is("http://localhost/employees")))

				.andExpect(jsonPath("$.collection.template.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.template.data[0].name", is("name")))
				.andExpect(jsonPath("$.collection.template.data[0].value", is("")))
				.andExpect(jsonPath("$.collection.template.data[1].name", is("role")))
				.andExpect(jsonPath("$.collection.template.data[1].value", is("")));
	}

	@Test
	public void createNewEmployee() throws Exception {

		String specBasedJson = MappingUtils.read(new ClassPathResource("spec-part7-adjusted.json", getClass()));

		this.mockMvc.perform(post("/employees") //
				.content(specBasedJson) //
				.contentType(MediaTypes.COLLECTION_JSON_VALUE)) //
				.andExpect(status().isCreated()) //
				.andExpect(header().stringValues(HttpHeaders.LOCATION, "http://localhost/employees/2"));

		this.mockMvc.perform(get("/employees/2").accept(MediaTypes.COLLECTION_JSON)).andExpect(status().isOk()) //

				.andExpect(jsonPath("$.collection.version", is("1.0")))
				.andExpect(jsonPath("$.collection.href", is("http://localhost/employees/2")))

				.andExpect(jsonPath("$.collection.links.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.links[0].rel", is("employees")))
				.andExpect(jsonPath("$.collection.links[0].href", is("http://localhost/employees")))

				.andExpect(jsonPath("$.collection.items.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.items[0].data[1].name", is("name")))
				.andExpect(jsonPath("$.collection.items[0].data[1].value", is("W. Chandry")))
				.andExpect(jsonPath("$.collection.items[0].data[0].name", is("role")))
				.andExpect(jsonPath("$.collection.items[0].data[0].value", is("developer")))

				.andExpect(jsonPath("$.collection.items[0].links.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.items[0].links[0].rel", is("employees")))
				.andExpect(jsonPath("$.collection.items[0].links[0].href", is("http://localhost/employees")))

				.andExpect(jsonPath("$.collection.template.*", hasSize(1)))
				.andExpect(jsonPath("$.collection.template.data[0].name", is("name")))
				.andExpect(jsonPath("$.collection.template.data[0].value", is("")))
				.andExpect(jsonPath("$.collection.template.data[1].name", is("role")))
				.andExpect(jsonPath("$.collection.template.data[1].value", is("")));
	}

	@RestController
	static class EmployeeController {

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
					.andAffordance(afford(methodOn(EmployeeController.class).newEmployee(null)))
					.andAffordance(afford(methodOn(EmployeeController.class).search(null, null)));

			// Return the collection of employee resources along with the composite affordance
			return new Resources<>(employees, selfLink);
		}

		@GetMapping("/employees/search")
		public Resources<Resource<Employee>> search(@RequestParam(value = "name", required = false) String name,
				@RequestParam(value = "role", required = false) String role) {

			// Create a list of Resource<Employee>'s to return
			List<Resource<Employee>> employees = new ArrayList<>();

			// Fetch each Resource<Employee> using the controller's findOne method.
			for (int i = 0; i < EMPLOYEES.size(); i++) {
				Resource<Employee> employeeResource = findOne(i);

				boolean nameMatches = Optional.ofNullable(name).map(s -> employeeResource.getContent().getName().contains(s))
						.orElse(true);

				boolean roleMatches = Optional.ofNullable(role).map(s -> employeeResource.getContent().getRole().contains(s))
						.orElse(true);

				if (nameMatches && roleMatches) {
					employees.add(employeeResource);
				}
			}

			// Generate an "Affordance" based on this method (the "self" link)
			Link selfLink = linkTo(methodOn(EmployeeController.class).all()).withSelfRel()
					.andAffordance(afford(methodOn(EmployeeController.class).newEmployee(null)))
					.andAffordance(afford(methodOn(EmployeeController.class).search(null, null)));

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
		public ResponseEntity<?> newEmployee(@RequestBody Resource<Employee> employee) {

			int newEmployeeId = EMPLOYEES.size();

			EMPLOYEES.put(newEmployeeId, employee.getContent());

			try {
				return ResponseEntity
						.created(
								new URI(findOne(newEmployeeId).getLink(Link.REL_SELF).map(link -> link.expand().getHref()).orElse("")))
						.build();
			} catch (URISyntaxException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}

		@PutMapping("/employees/{id}")
		public ResponseEntity<?> updateEmployee(@RequestBody Resource<Employee> employee, @PathVariable Integer id) {

			EMPLOYEES.put(id, employee.getContent());

			try {
				return ResponseEntity.noContent()
						.location(new URI(findOne(id).getLink(Link.REL_SELF).map(link -> link.expand().getHref()).orElse("")))
						.build();
			} catch (URISyntaxException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}

		@PatchMapping("/employees/{id}")
		public ResponseEntity<?> partiallyUpdateEmployee(@RequestBody Resource<Employee> employee,
				@PathVariable Integer id) {

			Employee oldEmployee = EMPLOYEES.get(id);
			Employee newEmployee = oldEmployee;

			if (employee.getContent().getName() != null) {
				newEmployee = newEmployee.withName(employee.getContent().getName());
			}

			if (employee.getContent().getRole() != null) {
				newEmployee = newEmployee.withRole(employee.getContent().getRole());
			}

			EMPLOYEES.put(id, newEmployee);

			try {
				return ResponseEntity.noContent()
						.location(new URI(findOne(id).getLink(Link.REL_SELF).map(link -> link.expand().getHref()).orElse("")))
						.build();
			} catch (URISyntaxException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.COLLECTION_JSON })
	static class TestConfig {

		@Bean
		EmployeeController employeeController() {
			return new EmployeeController();
		}
	}
}
