/*
 * Copyright 2019-2021 the original author or authors.
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
package org.springframework.hateoas.config;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.hateoas.support.CustomHypermediaType.*;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.support.CustomHypermediaType;
import org.springframework.hateoas.support.Employee;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Integration test for a custom hypermedia support registration.
 *
 * @author Greg Turnquist
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
class CustomHypermediaWebMvcTest {

	private @Autowired WebApplicationContext context;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		this.mockMvc = webAppContextSetup(this.context).build();
	}

	@Test // #833
	void getUsingCustomMediaType() throws Exception {

		String results = this.mockMvc.perform(get("/employees/1").accept(FRODO_MEDIATYPE)) //
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, FRODO_MEDIATYPE.toString())) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();

		assertThat(results).isEqualTo(read(new ClassPathResource("webmvc-frodo.json", getClass())));
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class TestConfig {

		@Bean
		CustomHypermediaType customHypermediaType() {
			return new CustomHypermediaType();
		}

		@Bean
		EmployeeController employeeController() {
			return new EmployeeController();
		}
	}

	@RestController
	static class EmployeeController {

		@GetMapping("/employees/1")
		public EntityModel<Employee> findOne() {
			return EntityModel.of(new Employee("Frodo Baggins", "ring bearer"),
					linkTo(methodOn(EmployeeController.class).findOne()).withSelfRel());
		}
	}
}
