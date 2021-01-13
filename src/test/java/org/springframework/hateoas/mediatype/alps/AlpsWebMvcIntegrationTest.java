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
package org.springframework.hateoas.mediatype.alps;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.support.WebMvcEmployeeController;
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
public class AlpsWebMvcIntegrationTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		this.mockMvc = webAppContextSetup(this.context).build();
	}

	@Test
	void profileEndpointReturnsAlps() throws Exception {

		this.mockMvc.perform(get("/profile").accept(MediaTypes.ALPS_JSON_VALUE)) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$.version", is("1.0"))) //
				.andExpect(jsonPath("$.doc.format", is("TEXT")))
				.andExpect(jsonPath("$.doc.href", is("https://example.org/samples/full/doc.html")))
				.andExpect(jsonPath("$.doc.value", is("value goes here"))).andExpect(jsonPath("$.descriptor", hasSize(2)))

				.andExpect(jsonPath("$.descriptor[0].id", is("class field [name]")))
				.andExpect(jsonPath("$.descriptor[0].name", is("name")))
				.andExpect(jsonPath("$.descriptor[0].type", is("SEMANTIC")))
				.andExpect(jsonPath("$.descriptor[0].descriptor", hasSize(1)))
				.andExpect(jsonPath("$.descriptor[0].descriptor[0].id", is("embedded")))
				.andExpect(jsonPath("$.descriptor[0].ext.id", is("ext [name]")))
				.andExpect(jsonPath("$.descriptor[0].ext.href", is("https://example.org/samples/ext/name")))
				.andExpect(jsonPath("$.descriptor[0].ext.value", is("value goes here")))
				.andExpect(jsonPath("$.descriptor[0].rt", is("rt for [name]")))

				.andExpect(jsonPath("$.descriptor[1].id", is("class field [role]")))
				.andExpect(jsonPath("$.descriptor[1].name", is("role")))
				.andExpect(jsonPath("$.descriptor[1].type", is("SEMANTIC")))
				.andExpect(jsonPath("$.descriptor[1].descriptor", hasSize(1)))
				.andExpect(jsonPath("$.descriptor[1].descriptor[0].id", is("embedded")))
				.andExpect(jsonPath("$.descriptor[1].ext.id", is("ext [role]")))
				.andExpect(jsonPath("$.descriptor[1].ext.href", is("https://example.org/samples/ext/role")))
				.andExpect(jsonPath("$.descriptor[1].ext.value", is("value goes here")))
				.andExpect(jsonPath("$.descriptor[1].rt", is("rt for [role]")));
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = HAL)
	static class TestConfig {

		@Bean
		WebMvcEmployeeController employeeController() {
			return new WebMvcEmployeeController();
		}
	}

}
