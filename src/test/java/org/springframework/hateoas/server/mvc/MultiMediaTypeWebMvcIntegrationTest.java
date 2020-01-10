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
package org.springframework.hateoas.server.mvc;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsLinkDiscoverer;
import org.springframework.hateoas.mediatype.uber.UberLinkDiscoverer;
import org.springframework.hateoas.support.Employee;
import org.springframework.hateoas.support.MappingUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
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
 * Test one controller, rendered into multiple media types.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
class MultiMediaTypeWebMvcIntegrationTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	private static Map<Integer, Employee> EMPLOYEES;

	@BeforeEach
	void setUp() {

		this.mockMvc = webAppContextSetup(this.context).build();

		EMPLOYEES = new TreeMap<>();

		EMPLOYEES.put(0, new Employee("Frodo Baggins", "ring bearer"));
		EMPLOYEES.put(1, new Employee("Bilbo Baggins", "burglar"));
	}

	@Test
	void singleEmployeeCollectionJson() throws Exception {

		this.mockMvc.perform(get("/employees/0").accept(MediaTypes.COLLECTION_JSON_VALUE)) //
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
	void collectionOfEmployeesCollectionJson() throws Exception {

		this.mockMvc.perform(get("/employees").accept(MediaTypes.COLLECTION_JSON_VALUE)) //
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
	void createNewEmployeeCollectionJson() throws Exception {

		String specBasedJson = MappingUtils
				.read(new ClassPathResource("spec-part7-adjusted.json", CollectionJsonLinkDiscoverer.class));

		this.mockMvc.perform(post("/employees").content(specBasedJson).contentType(MediaTypes.COLLECTION_JSON_VALUE))
				.andExpect(status().isCreated())
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

	@Test
	void singleEmployeeHalForms() throws Exception {

		ResultActions actions = this.mockMvc.perform(get("/employees/0").accept(MediaTypes.HAL_FORMS_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$.name", is("Frodo Baggins"))).andExpect(jsonPath("$.role", is("ring bearer")))

				.andExpect(jsonPath("$._links.*", hasSize(2)))
				.andExpect(jsonPath("$._links['self'].href", is("http://localhost/employees/0")))
				.andExpect(jsonPath("$._links['employees'].href", is("http://localhost/employees")));

		expectEmployeeProperties(actions, "default", "partiallyUpdateEmployee") //
				.andExpect(jsonPath("$._templates['default'].method", is("put")))
				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].method", is("patch")));
	}

	@Test
	void collectionOfEmployeesHalForms() throws Exception {

		ResultActions actions = this.mockMvc.perform(get("/employees").accept(MediaTypes.HAL_FORMS_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$._embedded.employees[0].name", is("Frodo Baggins")))
				.andExpect(jsonPath("$._embedded.employees[0].role", is("ring bearer")))
				.andExpect(jsonPath("$._embedded.employees[0]._links['self'].href", is("http://localhost/employees/0")))
				.andExpect(jsonPath("$._embedded.employees[1].name", is("Bilbo Baggins")))
				.andExpect(jsonPath("$._embedded.employees[1].role", is("burglar")))
				.andExpect(jsonPath("$._embedded.employees[1]._links['self'].href", is("http://localhost/employees/1")))

				.andExpect(jsonPath("$._links.*", hasSize(1)))
				.andExpect(jsonPath("$._links['self'].href", is("http://localhost/employees")))

				.andExpect(jsonPath("$._templates['default'].method", is("post")));

		expectEmployeeProperties(actions, "default");
	}

	@Test
	void createNewEmployeeHalForms() throws Exception {

		String specBasedJson = MappingUtils.read(new ClassPathResource("new-employee.json", HalFormsLinkDiscoverer.class));

		this.mockMvc.perform(post("/employees").content(specBasedJson).contentType(MediaTypes.HAL_FORMS_JSON_VALUE))
				.andExpect(status().isCreated())
				.andExpect(header().stringValues(HttpHeaders.LOCATION, "http://localhost/employees/2"));

		ResultActions actions = this.mockMvc.perform(get("/employees/2").accept(MediaTypes.HAL_FORMS_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$.name", is("Samwise Gamgee"))).andExpect(jsonPath("$.role", is("gardener")))

				.andExpect(jsonPath("$._links.*", hasSize(2)))
				.andExpect(jsonPath("$._links['self'].href", is("http://localhost/employees/2")))
				.andExpect(jsonPath("$._links['employees'].href", is("http://localhost/employees")));

		expectEmployeeProperties(actions, "default", "partiallyUpdateEmployee") //

				.andExpect(jsonPath("$._templates['default'].method", is("put")))
				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].method", is("patch")));
	}

	@Test
	void singleEmployeeUber() throws Exception {

		this.mockMvc.perform(get("/employees/0").accept(MediaTypes.UBER_JSON)) //
				.andExpect(status().isOk()) //

				.andExpect(jsonPath("$.uber.version", is("1.0")))

				.andExpect(jsonPath("$.uber.data.*", hasSize(5))).andExpect(jsonPath("$.uber.data[0].name", is("self")))
				.andExpect(jsonPath("$.uber.data[0].rel[0]", is("self")))
				.andExpect(jsonPath("$.uber.data[0].rel[1]", is("findOne")))
				.andExpect(jsonPath("$.uber.data[0].url", is("http://localhost/employees/0")))

				.andExpect(jsonPath("$.uber.data[1].name", is("updateEmployee")))
				.andExpect(jsonPath("$.uber.data[1].rel[0]", is("updateEmployee")))
				.andExpect(jsonPath("$.uber.data[1].url", is("http://localhost/employees/0")))
				.andExpect(jsonPath("$.uber.data[1].action", is("replace")))
				.andExpect(jsonPath("$.uber.data[1].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[2].name", is("partiallyUpdateEmployee")))
				.andExpect(jsonPath("$.uber.data[2].rel[0]", is("partiallyUpdateEmployee")))
				.andExpect(jsonPath("$.uber.data[2].url", is("http://localhost/employees/0")))
				.andExpect(jsonPath("$.uber.data[2].action", is("partial")))
				.andExpect(jsonPath("$.uber.data[2].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[3].name", is("employees")))
				.andExpect(jsonPath("$.uber.data[3].rel[0]", is("employees")))
				.andExpect(jsonPath("$.uber.data[3].rel[1]", is("all")))
				.andExpect(jsonPath("$.uber.data[3].url", is("http://localhost/employees")))

				.andExpect(jsonPath("$.uber.data[4].name", is("employee")))
				.andExpect(jsonPath("$.uber.data[4].data.*", hasSize(2)))
				.andExpect(jsonPath("$.uber.data[4].data[0].name", is("role")))
				.andExpect(jsonPath("$.uber.data[4].data[0].value", is("ring bearer")))
				.andExpect(jsonPath("$.uber.data[4].data[1].name", is("name")))
				.andExpect(jsonPath("$.uber.data[4].data[1].value", is("Frodo Baggins")));
	}

	@Test
	void collectionOfEmployeesUber() throws Exception {

		this.mockMvc.perform(get("/employees").accept(MediaTypes.UBER_JSON)) //
				.andExpect(status().isOk()) //

				.andExpect(jsonPath("$.uber.version", is("1.0")))

				.andExpect(jsonPath("$.uber.data.*", hasSize(4)))

				.andExpect(jsonPath("$.uber.data[0].name", is("self"))).andExpect(jsonPath("$.uber.data[0].rel[0]", is("self")))
				.andExpect(jsonPath("$.uber.data[0].rel[1]", is("all")))
				.andExpect(jsonPath("$.uber.data[0].url", is("http://localhost/employees")))

				.andExpect(jsonPath("$.uber.data[1].name", is("newEmployee")))
				.andExpect(jsonPath("$.uber.data[1].rel[0]", is("newEmployee")))
				.andExpect(jsonPath("$.uber.data[1].url", is("http://localhost/employees")))
				.andExpect(jsonPath("$.uber.data[1].action", is("append")))
				.andExpect(jsonPath("$.uber.data[1].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[2].data[0].name", is("self")))
				.andExpect(jsonPath("$.uber.data[2].data[0].rel[0]", is("self")))
				.andExpect(jsonPath("$.uber.data[2].data[0].rel[1]", is("findOne")))
				.andExpect(jsonPath("$.uber.data[2].data[0].url", is("http://localhost/employees/0")))

				.andExpect(jsonPath("$.uber.data[2].data[1].name", is("updateEmployee")))
				.andExpect(jsonPath("$.uber.data[2].data[1].rel[0]", is("updateEmployee")))
				.andExpect(jsonPath("$.uber.data[2].data[1].url", is("http://localhost/employees/0")))
				.andExpect(jsonPath("$.uber.data[2].data[1].action", is("replace")))
				.andExpect(jsonPath("$.uber.data[2].data[1].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[2].data[2].name", is("partiallyUpdateEmployee")))
				.andExpect(jsonPath("$.uber.data[2].data[2].rel[0]", is("partiallyUpdateEmployee")))
				.andExpect(jsonPath("$.uber.data[2].data[2].url", is("http://localhost/employees/0")))
				.andExpect(jsonPath("$.uber.data[2].data[2].action", is("partial")))
				.andExpect(jsonPath("$.uber.data[2].data[2].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[2].data[3].rel[0]", is("employees")))
				.andExpect(jsonPath("$.uber.data[2].data[3].rel[1]", is("all")))
				.andExpect(jsonPath("$.uber.data[2].data[3].url", is("http://localhost/employees")))

				.andExpect(jsonPath("$.uber.data[2].data[4].name", is("employee")))
				.andExpect(jsonPath("$.uber.data[2].data[4].data[0].name", is("role")))
				.andExpect(jsonPath("$.uber.data[2].data[4].data[0].value", is("ring bearer")))
				.andExpect(jsonPath("$.uber.data[2].data[4].data[1].name", is("name")))
				.andExpect(jsonPath("$.uber.data[2].data[4].data[1].value", is("Frodo Baggins")))

				.andExpect(jsonPath("$.uber.data[3].data[0].name", is("self")))
				.andExpect(jsonPath("$.uber.data[3].data[0].rel[0]", is("self")))
				.andExpect(jsonPath("$.uber.data[3].data[0].rel[1]", is("findOne")))
				.andExpect(jsonPath("$.uber.data[3].data[0].url", is("http://localhost/employees/1")))

				.andExpect(jsonPath("$.uber.data[3].data[1].name", is("updateEmployee")))
				.andExpect(jsonPath("$.uber.data[3].data[1].rel[0]", is("updateEmployee")))
				.andExpect(jsonPath("$.uber.data[3].data[1].url", is("http://localhost/employees/1")))
				.andExpect(jsonPath("$.uber.data[3].data[1].action", is("replace")))
				.andExpect(jsonPath("$.uber.data[3].data[1].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[3].data[2].name", is("partiallyUpdateEmployee")))
				.andExpect(jsonPath("$.uber.data[3].data[2].rel[0]", is("partiallyUpdateEmployee")))
				.andExpect(jsonPath("$.uber.data[3].data[2].url", is("http://localhost/employees/1")))
				.andExpect(jsonPath("$.uber.data[3].data[2].action", is("partial")))
				.andExpect(jsonPath("$.uber.data[3].data[2].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[3].data[3].rel[0]", is("employees")))
				.andExpect(jsonPath("$.uber.data[3].data[3].rel[1]", is("all")))
				.andExpect(jsonPath("$.uber.data[3].data[3].url", is("http://localhost/employees")))

				.andExpect(jsonPath("$.uber.data[3].data[4].name", is("employee")))
				.andExpect(jsonPath("$.uber.data[3].data[4].data[0].name", is("role")))
				.andExpect(jsonPath("$.uber.data[3].data[4].data[0].value", is("burglar")))
				.andExpect(jsonPath("$.uber.data[3].data[4].data[1].name", is("name")))
				.andExpect(jsonPath("$.uber.data[3].data[4].data[1].value", is("Bilbo Baggins")));
	}

	@Test
	void createNewEmployeeUber() throws Exception {

		String input = MappingUtils.read(new ClassPathResource("create-employee.json", UberLinkDiscoverer.class));

		this.mockMvc.perform(post("/employees").content(input).contentType(MediaTypes.UBER_JSON))
				.andExpect(status().isCreated())
				.andExpect(header().stringValues(HttpHeaders.LOCATION, "http://localhost/employees/2"));

		this.mockMvc.perform(get("/employees/2").accept(MediaTypes.UBER_JSON)).andExpect(status().isOk()) //

				.andExpect(jsonPath("$.uber.version", is("1.0")))

				.andExpect(jsonPath("$.uber.data.*", hasSize(5))).andExpect(jsonPath("$.uber.data[0].name", is("self")))
				.andExpect(jsonPath("$.uber.data[0].rel[0]", is("self")))
				.andExpect(jsonPath("$.uber.data[0].rel[1]", is("findOne")))
				.andExpect(jsonPath("$.uber.data[0].url", is("http://localhost/employees/2")))

				.andExpect(jsonPath("$.uber.data[1].name", is("updateEmployee")))
				.andExpect(jsonPath("$.uber.data[1].rel[0]", is("updateEmployee")))
				.andExpect(jsonPath("$.uber.data[1].url", is("http://localhost/employees/2")))
				.andExpect(jsonPath("$.uber.data[1].action", is("replace")))
				.andExpect(jsonPath("$.uber.data[1].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[2].name", is("partiallyUpdateEmployee")))
				.andExpect(jsonPath("$.uber.data[2].rel[0]", is("partiallyUpdateEmployee")))
				.andExpect(jsonPath("$.uber.data[2].url", is("http://localhost/employees/2")))
				.andExpect(jsonPath("$.uber.data[2].action", is("partial")))
				.andExpect(jsonPath("$.uber.data[2].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[3].name", is("employees")))
				.andExpect(jsonPath("$.uber.data[3].rel[0]", is("employees")))
				.andExpect(jsonPath("$.uber.data[3].rel[1]", is("all")))
				.andExpect(jsonPath("$.uber.data[3].url", is("http://localhost/employees")))

				.andExpect(jsonPath("$.uber.data[4].name", is("employee")))
				.andExpect(jsonPath("$.uber.data[4].data.*", hasSize(2)))
				.andExpect(jsonPath("$.uber.data[4].data[0].name", is("role")))
				.andExpect(jsonPath("$.uber.data[4].data[0].value", is("gardener")))
				.andExpect(jsonPath("$.uber.data[4].data[1].name", is("name")))
				.andExpect(jsonPath("$.uber.data[4].data[1].value", is("Samwise Gamgee")));
	}

	private static final ResultActions expectEmployeeProperties(ResultActions actions, String... templates)
			throws Exception {

		for (String template : templates) {

			JsonPathResultMatchers namePropertyMatcher = jsonPath("$._templates['%s'].properties[0].required", template);

			actions = actions //
					.andExpect(jsonPath("$._templates['%s'].properties[0].name", template).value("name"))
					.andExpect(jsonPath("$._templates['%s'].properties[1].name", template).value("role"))
					.andExpect(jsonPath("$._templates['%s'].properties[1].required", template).doesNotExist());

			actions = template.equals("partiallyUpdateEmployee") //
					? actions.andExpect(namePropertyMatcher.doesNotExist()) //
					: actions.andExpect(namePropertyMatcher.value(true));
		}

		return actions.andExpect(jsonPath("$._templates.*", hasSize(templates.length)));
	}

	@RestController
	static class EmployeeController {

		@GetMapping("/employees")
		public CollectionModel<EntityModel<Employee>> all() {

			// Create a list of Resource<Employee>'s to return
			List<EntityModel<Employee>> employees = new ArrayList<>();

			// Fetch each Resource<Employee> using the controller's findOne method.
			for (int i = 0; i < EMPLOYEES.size(); i++) {
				employees.add(findOne(i));
			}

			// Generate an "Affordance" based on this method (the "self" link)
			Link selfLink = linkTo(methodOn(EmployeeController.class).all()).withSelfRel()
					.andAffordance(afford(methodOn(EmployeeController.class).newEmployee(null)))
					.andAffordance(afford(methodOn(EmployeeController.class).search(null, null)));

			// Return the collection of employee resources along with the composite affordance
			return new CollectionModel<>(employees, selfLink);
		}

		@GetMapping("/employees/search")
		public CollectionModel<EntityModel<Employee>> search(@RequestParam(value = "name", required = false) String name,
				@RequestParam(value = "role", required = false) String role) {

			// Create a list of Resource<Employee>'s to return
			List<EntityModel<Employee>> employees = new ArrayList<>();

			// Fetch each Resource<Employee> using the controller's findOne method.
			for (int i = 0; i < EMPLOYEES.size(); i++) {
				EntityModel<Employee> employeeResource = findOne(i);

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
			return new CollectionModel<>(employees, selfLink);
		}

		@GetMapping("/employees/{id}")
		public EntityModel<Employee> findOne(@PathVariable Integer id) {

			// Start the affordance with the "self" link, i.e. this method.
			Link findOneLink = linkTo(methodOn(EmployeeController.class).findOne(id)).withSelfRel();

			// Define final link as means to find entire collection.
			Link employeesLink = linkTo(methodOn(EmployeeController.class).all()).withRel("employees");

			// Return the affordance + a link back to the entire collection resource.
			return new EntityModel<>(EMPLOYEES.get(id),
					findOneLink.andAffordance(afford(methodOn(EmployeeController.class).updateEmployee(null, id))) //
							.andAffordance(afford(methodOn(EmployeeController.class).partiallyUpdateEmployee(null, id))),
					employeesLink);
		}

		@PostMapping("/employees")
		public ResponseEntity<?> newEmployee(@RequestBody EntityModel<Employee> employee) {

			int newEmployeeId = EMPLOYEES.size();

			EMPLOYEES.put(newEmployeeId, employee.getContent());

			return ResponseEntity.created(toUri(newEmployeeId)).build();
		}

		@PutMapping("/employees/{id}")
		public ResponseEntity<?> updateEmployee(@RequestBody EntityModel<Employee> employee, @PathVariable Integer id) {

			EMPLOYEES.put(id, employee.getContent());

			return ResponseEntity.noContent().location(toUri(id)).build();
		}

		@PatchMapping("/employees/{id}")
		public ResponseEntity<?> partiallyUpdateEmployee(@RequestBody EntityModel<Employee> employee,
				@PathVariable Integer id) {

			Employee newEmployee = EMPLOYEES.get(id);

			if (employee.getContent().getName() != null) {
				newEmployee = newEmployee.withName(employee.getContent().getName());
			}

			if (employee.getContent().getRole() != null) {
				newEmployee = newEmployee.withRole(employee.getContent().getRole());
			}

			EMPLOYEES.put(id, newEmployee);

			return ResponseEntity.noContent().location(toUri(id)).build();
		}

		private URI toUri(Integer id) {

			return findOne(id) //
					.getRequiredLink(IanaLinkRelations.SELF) //
					.toUri();
		}
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(
			type = { HypermediaType.HAL, HypermediaType.COLLECTION_JSON, HypermediaType.HAL_FORMS, HypermediaType.UBER })
	static class TestConfig {

		@Bean
		EmployeeController employeeController() {
			return new EmployeeController();
		}
	}
}
