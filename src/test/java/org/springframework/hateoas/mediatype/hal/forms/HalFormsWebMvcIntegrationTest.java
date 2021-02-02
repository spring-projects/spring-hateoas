/*
 * Copyright 2017-2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
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
class HalFormsWebMvcIntegrationTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {

		this.mockMvc = webAppContextSetup(this.context).build();
		WebMvcEmployeeController.reset();
	}

	@Test
	void singleEmployee() throws Exception {

		this.mockMvc.perform(get("/employees/0").accept(MediaTypes.HAL_FORMS_JSON)) //

				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$.name", is("Frodo Baggins"))) //
				.andExpect(jsonPath("$.role", is("ring bearer")))

				.andExpect(jsonPath("$._links.*", hasSize(2)))
				.andExpect(jsonPath("$._links['self'].href", is("http://localhost/employees/0")))
				.andExpect(jsonPath("$._links['employees'].href", is("http://localhost/employees")))

				.andExpect(jsonPath("$._templates.*", hasSize(2)))
				.andExpect(jsonPath("$._templates['default'].method", is("put")))
				.andExpect(jsonPath("$._templates['default'].properties[0].name", is("name")))
				.andExpect(jsonPath("$._templates['default'].properties[0].required").value(true))
				.andExpect(jsonPath("$._templates['default'].properties[1].name", is("role")))
				.andExpect(jsonPath("$._templates['default'].properties[1].required").doesNotExist())

				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].method", is("patch")))
				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].properties[0].name", is("name")))
				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].properties[0].required").doesNotExist())
				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].properties[1].name", is("role")))
				.andExpect(jsonPath("$._templates['partiallyUpdateEmployee'].properties[1].required").doesNotExist());
	}

	@Test
	void collectionOfEmployees() throws Exception {

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
				.andExpect(jsonPath("$._templates['default'].properties[0].required").value(true))
				.andExpect(jsonPath("$._templates['default'].properties[1].name", is("role")))
				.andExpect(jsonPath("$._templates['default'].properties[1].required").doesNotExist());
	}

	@Test
	void createNewEmployee() throws Exception {

		String specBasedJson = MappingTestUtils.createMapper(getClass()).readFileContent("new-employee.json");

		this.mockMvc.perform(post("/employees") //
				.content(specBasedJson) //
				.contentType(MediaTypes.HAL_FORMS_JSON_VALUE)) //
				.andExpect(status().isCreated())
				.andExpect(header().stringValues(HttpHeaders.LOCATION, "http://localhost/employees/2"));
	}

	@Test // #832
	public void usesRegisteredHalFormsConfiguration() {
		assertInstanceUsed(WithHalFormsConfiguration.class, WithHalFormsConfiguration.CONFIG);
	}

	@Test // #832
	public void usesRegisteredHalConfiguration() {
		assertInstanceUsed(WithHalConfiguration.class, WithHalConfiguration.CONFIG);
	}

	private static void assertInstanceUsed(Class<?> configurationClass, HalConfiguration configuration) {

		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(configurationClass)) {

			HalFormsMediaTypeConfiguration mediaTypeConfiguration = context.getBean(HalFormsMediaTypeConfiguration.class);
			HalFormsConfiguration actual = mediaTypeConfiguration.getResolvedConfiguration();

			assertThat(actual.getHalConfiguration()).isSameAs(configuration);
		}
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

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL_FORMS)
	static class WithHalFormsConfiguration {

		static final HalConfiguration CONFIG = new HalConfiguration();

		@Bean
		public HalFormsConfiguration halFormsConfiguration() {
			return new HalFormsConfiguration(CONFIG);
		}
	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL_FORMS)
	static class WithHalConfiguration {

		static final HalConfiguration CONFIG = new HalConfiguration();

		@Bean
		public HalConfiguration halConfiguration() {
			return CONFIG;
		}
	}
}
