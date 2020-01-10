/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.hateoas.mediatype.uber;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.springframework.hateoas.support.MappingUtils.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.support.WebMvcEmployeeController;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Greg Turnquist
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
class UberWebMvcIntegrationTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {

		this.mockMvc = webAppContextSetup(this.context).build();
		WebMvcEmployeeController.reset();
	}

	/**
	 * @see #784
	 */
	@Test
	void singleEmployee() throws Exception {

		this.mockMvc.perform(get("/employees/0").accept(MediaTypes.UBER_JSON)) //
				.andExpect(status().isOk()) //

				.andExpect(jsonPath("$.uber.version", is("1.0")))

				.andExpect(jsonPath("$.uber.data.*", hasSize(5))) //
				.andExpect(jsonPath("$.uber.data[0].name", is("self"))) //
				.andExpect(jsonPath("$.uber.data[0].rel[0]", is("self"))) //
				.andExpect(jsonPath("$.uber.data[0].rel[1]", is("findOne"))) //
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

	/**
	 * @see #784
	 */
	@Test
	void collectionOfEmployees() throws Exception {

		this.mockMvc.perform(get("/employees").accept(MediaTypes.UBER_JSON)) //
				.andExpect(status().isOk()) //

				.andExpect(jsonPath("$.uber.version", is("1.0")))

				.andExpect(jsonPath("$.uber.data.*", hasSize(4)))

				.andExpect(jsonPath("$.uber.data[0].name", is("self"))) //
				.andExpect(jsonPath("$.uber.data[0].rel[0]", is("self")))
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

	/**
	 * @see #784
	 */
	@Test
	void createNewEmployee() throws Exception {

		String input = read(new ClassPathResource("create-employee.json", getClass()));

		this.mockMvc.perform(post("/employees") //
				.content(input) //
				.contentType(MediaTypes.UBER_JSON)) //
				.andExpect(status().isCreated()) //
				.andExpect(header().stringValues(HttpHeaders.LOCATION, "http://localhost/employees/2"));

		this.mockMvc.perform(get("/employees/2").accept(MediaTypes.UBER_JSON)) //
				.andExpect(status().isOk())

				.andExpect(jsonPath("$.uber.version", is("1.0")))

				.andExpect(jsonPath("$.uber.data.*", hasSize(5))) //
				.andExpect(jsonPath("$.uber.data[0].name", is("self"))) //
				.andExpect(jsonPath("$.uber.data[0].rel[0]", is("self"))) //
				.andExpect(jsonPath("$.uber.data[0].rel[1]", is("findOne"))) //
				.andExpect(jsonPath("$.uber.data[0].url", is("http://localhost/employees/2")))

				.andExpect(jsonPath("$.uber.data[1].name", is("updateEmployee"))) //
				.andExpect(jsonPath("$.uber.data[1].rel[0]", is("updateEmployee"))) //
				.andExpect(jsonPath("$.uber.data[1].url", is("http://localhost/employees/2"))) //
				.andExpect(jsonPath("$.uber.data[1].action", is("replace"))) //
				.andExpect(jsonPath("$.uber.data[1].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[2].name", is("partiallyUpdateEmployee"))) //
				.andExpect(jsonPath("$.uber.data[2].rel[0]", is("partiallyUpdateEmployee"))) //
				.andExpect(jsonPath("$.uber.data[2].url", is("http://localhost/employees/2"))) //
				.andExpect(jsonPath("$.uber.data[2].action", is("partial"))) //
				.andExpect(jsonPath("$.uber.data[2].model", is("name={name}&role={role}")))

				.andExpect(jsonPath("$.uber.data[3].name", is("employees"))) //
				.andExpect(jsonPath("$.uber.data[3].rel[0]", is("employees"))) //
				.andExpect(jsonPath("$.uber.data[3].rel[1]", is("all"))) //
				.andExpect(jsonPath("$.uber.data[3].url", is("http://localhost/employees")))

				.andExpect(jsonPath("$.uber.data[4].name", is("employee"))) //
				.andExpect(jsonPath("$.uber.data[4].data.*", hasSize(2))) //
				.andExpect(jsonPath("$.uber.data[4].data[0].name", is("role"))) //
				.andExpect(jsonPath("$.uber.data[4].data[0].value", is("gardener"))) //
				.andExpect(jsonPath("$.uber.data[4].data[1].name", is("name"))) //
				.andExpect(jsonPath("$.uber.data[4].data[1].value", is("Samwise Gamgee")));
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.UBER })
	static class TestConfig {

		@Bean
		WebMvcEmployeeController employeeController() {
			return new WebMvcEmployeeController();
		}
	}
}
