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
package org.springframework.hateoas.mediatype.problem;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Integration tests for our support for HTTP Problem Details.
 *
 * @author Oliver Drotbohm
 */
@TestInstance(Lifecycle.PER_CLASS)
public class HttpProblemDetailsIntegrationTest {

	MockMvc mvc;

	@BeforeAll
	void setUp() {

		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.setServletContext(new MockServletContext());
		context.register(Config.class);
		context.register(TestController.class);
		context.refresh();

		this.mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test // #786
	void returnsSimpleProblemDetails() throws Exception {

		mvc.perform(get("/problem")) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$.title").value("Title"))
				.andExpect(content().contentType(MediaTypes.HTTP_PROBLEM_DETAILS_JSON)); //
	}

	@Test // #786
	void returnsProblemWrappedInResponseEntity() throws Exception {

		mvc.perform(get("/problemInEntity")) //
				.andExpect(status().isIAmATeapot()) //
				.andExpect(jsonPath("$.title").value("WithinResponseEntity")) //
				.andExpect(content().contentType(MediaTypes.HTTP_PROBLEM_DETAILS_JSON)); //
	}

	@EnableWebMvc
	@EnableHypermediaSupport(type = HypermediaType.HTTP_PROBLEM_DETAILS)
	static class Config {}

	@RestController
	static class TestController {

		@GetMapping("/problem")
		Problem produceProblem() {
			return Problem.create().withTitle("Title");
		}

		@GetMapping("/problemInEntity")
		ResponseEntity<?> produceEntityOfProblem() {
			return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT) //
					.body(Problem.create().withTitle("WithinResponseEntity"));
		}
	}
}
