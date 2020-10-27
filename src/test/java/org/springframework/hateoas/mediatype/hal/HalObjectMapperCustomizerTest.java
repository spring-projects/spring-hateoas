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
public class HalObjectMapperCustomizerTest {

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
		String expectedHalJson = this.mapper.readFile("hal-custom.json");

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
