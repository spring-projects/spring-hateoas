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
package org.springframework.hateoas.affordance;

import static org.springframework.hateoas.affordance.springmvc.AffordanceBuilder.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
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

import lombok.Data;
import lombok.experimental.Wither;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.affordance.formaction.Action;
import org.springframework.hateoas.affordance.formaction.Input;
import org.springframework.hateoas.affordance.springmvc.AffordanceBuilder;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.JUnitRestDocumentation;
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Greg Turnquist
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class AffordanceDocumentationTest {

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	@Autowired
	WebApplicationContext context;

	MockMvc mockMvc;

	@Before
	public void setUp() {

		this.mockMvc = webAppContextSetup(this.context)
			.apply(documentationConfiguration(this.restDocumentation))
			.alwaysDo(document("{method-name}/{step}/", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
			.build();
	}

	@Test
	public void basic() throws Exception {

		this.mockMvc.perform(get("/employees").accept(MediaTypes.HAL_JSON))
			.andDo(print())
			.andExpect(status().isOk());

		this.mockMvc.perform(get("/employees").accept(MediaTypes.HAL_FORMS_JSON))
			.andDo(print())
			.andExpect(status().isOk());

		this.mockMvc.perform(get("/employees/0").accept(MediaTypes.HAL_JSON))
			.andDo(print())
			.andExpect(status().isOk());

		this.mockMvc.perform(get("/employees/0").accept(MediaTypes.HAL_FORMS_JSON))
			.andDo(print())
			.andExpect(status().isOk());
	}

	// tag::employee[]
	@Data
	@Wither
	static class Employee {
		private final String name;
		private final String role;

		@JsonCreator
		public Employee(@Input(required = true) @JsonProperty("name") String name,
						@Input(required = true) @JsonProperty("role") String role) {

			this.name = name;
			this.role = role;
		}
	}
	// end::employee[]

	// tag::employee-controller[]
	@RestController
	static class EmployeeController {
	// end::employee-controller[]

		private final static Map<Integer, Employee> EMPLOYEES = new TreeMap<Integer, Employee>();

		static {
			EMPLOYEES.put(0, new Employee("Frodo Baggins", "ring bearer"));
			EMPLOYEES.put(1, new Employee("Bilbo Baggins", "burglar"));
		}


		// tag::find-all[]
		@Action("find-all")
		@GetMapping("/employees")
		public Resources<Resource<Employee>> all() {

			// Create a list of Resource<Employee>'s to return
			List<Resource<Employee>> employees = new ArrayList<Resource<Employee>>();

			// Fetch each Resource<Employee> using the controller's findOne method.
			for (int i=0; i < EMPLOYEES.size(); i++) {
				employees.add(findOne(String.valueOf(i)));
			}

			// Generate an "Affordance" based on this method (the "self" link)
			AffordanceBuilder builder = linkTo(methodOn(EmployeeController.class).all());

			// Add additional operations, i.e. affordances
			builder.and(linkTo(methodOn(EmployeeController.class).newEmployee(null)).rel("create"));

			// Return the collection of employee resources along with the composite affordance
			return new Resources<Resource<Employee>>(employees, builder.withSelfRel());
		}
		// end::find-all[]

		// tag::find-one[]
		@Action("find-one")
		@GetMapping("/employees/{id}")
		public Resource<Employee> findOne(@PathVariable String id) {

			// Start the affordance with the "self" link, i.e. this method.
			AffordanceBuilder affordanceBuilder =
				linkTo(methodOn(EmployeeController.class).findOne(id));

			// Define another affordance for PUT
			AffordanceBuilder putAffordance =
				linkTo(methodOn(EmployeeController.class).updateEmployee(null, id));

			// Define a third affordance for PATCH
			AffordanceBuilder patchAffordance =
				linkTo(methodOn(EmployeeController.class).partiallyUpdateEmployee(null, id));

			// Alter PATCH's affordance by flipping every
			// ActionDescriptor's input parameter to required=false
			for (ActionDescriptor actionDescriptor : patchAffordance.getActionDescriptors()) {
				for (ActionInputParameter actionInputParameter : actionDescriptor.getActionInputParameters()) {
					actionInputParameter.setRequired(false);
				}
			}

			// Using the "self" affordance, add PUT and PATCH, and turn it into a "self" rel.
			Affordance affordance = affordanceBuilder
				.and(putAffordance)
				.and(patchAffordance).withSelfRel();

			// Return the affordance + a link back to the entire collection resource.
			return new Resource<Employee>(
				EMPLOYEES.get(Integer.parseInt(id)),
				affordance,
				linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
		}
		// end::find-one[]

		@Action("create")
		@PostMapping("/employees")
		public ResponseEntity<?> newEmployee(@RequestBody @Input(required = true) Employee employee) {

			int newEmployeeId = EMPLOYEES.size();

			EMPLOYEES.put(newEmployeeId, employee);

			try {
				return ResponseEntity
					.noContent()
					.location(new URI(findOne(String.valueOf(newEmployeeId)).getLink(Link.REL_SELF).expand().getHref()))
					.build();
			} catch (URISyntaxException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}

		@Action("replace")
		@PutMapping("/employees/{id}")
		public ResponseEntity<?> updateEmployee(@RequestBody @Input(required = true) Employee employee, @PathVariable String id) {

			EMPLOYEES.put(Integer.parseInt(id), employee);
			try {
				return ResponseEntity
					.noContent()
					.location(new URI(findOne(id).getLink(Link.REL_SELF).expand().getHref()))
					.build();
			} catch (URISyntaxException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}

		@Action("partial-update")
		@PatchMapping("/employees/{id}")
		public ResponseEntity<?> partiallyUpdateEmployee(@RequestBody @Input(required = true) Employee employee, @PathVariable String id) {

			Employee oldEmployee = EMPLOYEES.get(id);

			Employee newEmployee = oldEmployee;

			if (employee.getName() != null) {
				newEmployee = newEmployee.withName(employee.getName());
			}

			if (employee.getRole() != null) {
				newEmployee = newEmployee.withRole(employee.getRole());
			}

			EMPLOYEES.put(Integer.parseInt(id), newEmployee);
			try {
				return ResponseEntity
					.noContent()
					.location(new URI(findOne(id).getLink(Link.REL_SELF).expand().getHref()))
					.build();
			} catch (URISyntaxException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}
	}
	// end::employee-controller[]

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = {HypermediaType.HAL, HypermediaType.HAL_FORMS})
	static class TestConfig {

		@Bean
		EmployeeController employeeController() {
			return new EmployeeController();
		}

	}

}
