/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.springframework.hateoas.config.EnableHypermediaSupport.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Greg Turnquist
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class DynamicLinkBuilderTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = webAppContextSetup(this.context).build();
	}


	@Test
	public void noop() throws Exception {

		this.mockMvc.perform(get("/employees").accept(MediaTypes.HAL_JSON))
			.andDo(print());
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class Employee {

		private Integer id;
		private String name;
		private String role;
	}

	private static final HashMap<Integer, Employee> EMPLOYEES = new HashMap<>();

	static {
		EMPLOYEES.put(0, new Employee(0, "Frodo Baggins", "ring bearer"));
		EMPLOYEES.put(1, new Employee(1, "Bilbo Baggins", "burglar"));
	}

	@RestController
	static class EmployeeController {

		private DynamicLinkBuilder L;

		EmployeeController(DynamicLinkBuilder linkBuilder) {
			this.L = linkBuilder;
		}

		@GetMapping("/employees")
		Resources<Resource<Employee>> all() {
			return new Resources<>(
				EMPLOYEES.values().stream()
					.map(this::toResource)
					.collect(Collectors.toList()),
				L.linkTo(L.methodOn(EmployeeController.class).all()).withSelfRel());
		}

		@GetMapping("/employees/{id}")
		Resource<Employee> findOne(@PathVariable int id) {
			return toResource(EMPLOYEES.get(id));
		}

		private Resource<Employee> toResource(Employee employee) {
			return new Resource<>(employee,
				L.linkTo(L.methodOn(EmployeeController.class).findOne(employee.getId())).withSelfRel(),
				L.linkTo(L.methodOn(EmployeeController.class).all()).withRel("employees"));
		}
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.HAL })
	static class TestConfig {

		@Bean
		DynamicLinkBuilder dynamicLinkBuilder() {
			return new DynamicLinkBuilder(UriComponentsBuilder.newInstance());
		}

		@Bean
		EmployeeController employeeController(DynamicLinkBuilder linkBuilder) {
			return new EmployeeController(linkBuilder);
		}
	}
}
