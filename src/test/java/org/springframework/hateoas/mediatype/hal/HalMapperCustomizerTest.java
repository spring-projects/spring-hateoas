/*
 * Copyright 2020-2026 the original author or authors.
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import tools.jackson.core.json.JsonWriteFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
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
class HalMapperCustomizerTest {

	@Autowired WebApplicationContext context;
	MockMvc mockMvc;
	ContextualMapper $ = MappingTestUtils.createMapper(it -> it.addModule(new HalJacksonModule()));

	@BeforeEach
	void setUp() {

		this.mockMvc = webAppContextSetup(this.context).build();
		WebMvcEmployeeController.reset();
	}

	@Test // #1382
	void mapperCustomizerShouldBeApplied() throws Exception {

		$.assertDeserializesFile("hal-custom.json")
				.into(mockMvc.perform(get("/employees/0")).andReturn().getResponse().getContentAsString());
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
	@Import(WebMvcEmployeeController.class)
	static class TestConfig {

		@Bean
		JsonMapper mapper() {

			return JsonMapper.builder()
					.disable(JsonWriteFeature.ESCAPE_FORWARD_SLASHES)
					.enable(SerializationFeature.INDENT_OUTPUT)
					.build();
		}

		@Bean
		HalConfiguration halConfiguration() {
			return new HalConfiguration();
		}
	}
}
