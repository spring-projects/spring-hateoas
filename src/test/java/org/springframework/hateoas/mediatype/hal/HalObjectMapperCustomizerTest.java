/*
 * Copyright 2020-2021 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.support.WebMvcEmployeeController;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
class HalObjectMapperCustomizerTest {

	@Autowired WebApplicationContext context;

	MockMvc mockMvc;

	MappingTestUtils.ContextualMapper mapper = MappingTestUtils.createMapper(getClass());

	@BeforeEach
	void setUp() {

		this.mockMvc = webAppContextSetup(this.context).build();
		WebMvcEmployeeController.reset();
	}

	@Test // #1382
	void objectMapperCustomizerShouldBeApplied() throws Exception {

		String actualHalJson = this.mockMvc.perform(get("/employees/0")).andReturn().getResponse().getContentAsString();
		String expectedHalJson = this.mapper.readFileContent("hal-custom.json");

		assertThat(actualHalJson).isEqualTo(expectedHalJson);
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
	@Import(WebMvcEmployeeController.class)
	static class TestConfig {

		@Bean
		HalConfiguration halConfiguration() {
			return new HalConfiguration()
					.withObjectMapperCustomizer(objectMapper -> objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true));
		}
	}
}
