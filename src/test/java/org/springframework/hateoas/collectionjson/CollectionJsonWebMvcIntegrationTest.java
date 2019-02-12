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
package org.springframework.hateoas.collectionjson;

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
public class CollectionJsonWebMvcIntegrationTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	@Before
	public void setUp() {

		this.mockMvc = webAppContextSetup(this.context).build();
		WebMvcEmployeeController.reset();
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

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.COLLECTION_JSON })
	static class TestConfig {

		@Bean
		WebMvcEmployeeController employeeController() {
			return new WebMvcEmployeeController();
		}
	}
}
