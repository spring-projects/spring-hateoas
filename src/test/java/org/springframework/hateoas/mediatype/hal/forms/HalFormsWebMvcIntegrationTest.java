/*
 * Copyright 2017-2019 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.support.MappingUtils;
import org.springframework.hateoas.support.WebMvcEmployeeController;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
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
		WebMvcEmployeeController.reset();
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

		this.mockMvc.perform(post("/employees") //
				.content(specBasedJson) //
				.contentType(MediaTypes.HAL_FORMS_JSON_VALUE)) //
				.andExpect(status().isCreated())
				.andExpect(header().stringValues(HttpHeaders.LOCATION, "http://localhost/employees/2"));
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.HAL_FORMS })
	static class TestConfig {

		@Bean
		WebMvcEmployeeController employeeController() {
			return new WebMvcEmployeeController();
		}
	}
}
