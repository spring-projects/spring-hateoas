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
package org.springframework.hateoas.mediatype.hal;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import lombok.Data;
import lombok.experimental.Wither;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Greg Turnquist
 * @author Jens Schauder
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
class RenderHypermediaForDefaultAcceptHeadersTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		this.mockMvc = webAppContextSetup(this.context).build();
	}

	/**
	 * Verify the default {@literal Accept} header used by a browser (which favors XML/HTML) still yields HAL-JSON.
	 *
	 * @see #695
	 */
	@Test
	void browserBasedDefaultAcceptHeadersShouldProduceHalJson() throws Exception {

		this.mockMvc.perform(get("/employees").accept(MediaType.APPLICATION_XHTML_XML, MediaType.ALL)) //
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));
	}

	/**
	 * Verify the default {@literal Accept} header used by cURL still yields HAL-JSON.
	 *
	 * @see #695
	 */
	@Test
	void curlBasedDefaultAcceptHeadersShouldProduceHalJson() throws Exception {

		this.mockMvc.perform(get("/employees").accept(MediaType.ALL)) //
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));
	}

	@RestController
	static class EmployeeController {

		private final static List<Employee> EMPLOYEES = new ArrayList<>();

		static {
			EMPLOYEES.add(new Employee("Frodo Baggins", "ring bearer"));
			EMPLOYEES.add(new Employee("Bilbo Baggins", "burglar"));
		}

		@GetMapping("/employees")
		public CollectionModel<EntityModel<Employee>> all() {

			// Create a list of Resource<Employee>'s to return
			List<EntityModel<Employee>> employees = new ArrayList<>();

			// Fetch each Resource<Employee> using the controller's findOne method.
			for (int i = 0; i < EMPLOYEES.size(); i++) {
				employees.add(findOne(i));
			}

			// Generate an "Affordance" based on this method (the "self" link)
			Link selfLink = linkTo(methodOn(EmployeeController.class).all()).withSelfRel();

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
			return new EntityModel<>(EMPLOYEES.get(id), findOneLink, employeesLink);
		}
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.HAL })
	static class TestConfig {

		@Bean
		EmployeeController employeeController() {
			return new EmployeeController();
		}
	}

	@Data
	@Wither
	static class Employee {

		private final String name;
		private final String role;
	}
}
