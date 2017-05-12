/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.UUID;

import lombok.Value;
import org.junit.Before;
import org.junit.Test;

import org.springframework.hateoas.affordance.Suggestions;
import org.springframework.hateoas.affordance.Suggestions.ValueSuggestions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Oliver Gierke
 */
public class SuggestionSerializationTests {

	ObjectMapper mapper;

	@Before
	public void setUp() {

		mapper = new ObjectMapper();
		mapper.registerModule(new Jackson2HalFormsModule());
	}

	@Test
	public void rendersValueSuggestions() throws Exception {

		ValueSuggestions<User> suggestions = Suggestions.values(new User("Dave", "Matthews")).//
				withPromptField("firstname").//
				withValueField("id");

		String result = mapper.writeValueAsString(new Wrapper(suggestions));

		assertThat(result, hasJsonPath("$.suggestions.values[*].id"));
		assertThat(result, hasJsonPath("$.suggestions.values[*].firstname"));
		assertThat(result, hasJsonPath("$.suggestions.values[*].lastname"));
		assertThat(result, hasJsonPath("$.suggestions.prompt-field", is("firstname")));
		assertThat(result, hasJsonPath("$.suggestions.value-field", is("id")));
	}

	@Test
	public void rendersExternalSuggestion() throws Exception {

		String result = mapper.writeValueAsString(new Wrapper(Suggestions.external("some-rel")));

		assertThat(result, hasJsonPath("$.suggestions.embedded", is("some-rel")));
		assertThat(result, hasNoJsonPath("$.suggestions.prompt-field"));
		assertThat(result, hasNoJsonPath("$.suggestions.value-field"));
	}

	@Test
	public void rendersTemplatedRemoteSuggestion() throws JsonProcessingException {

		Wrapper wrapper = new Wrapper(Suggestions.remote("http://localhost:8080/{path}"));

		String result = mapper.writeValueAsString(wrapper);

		assertThat(result, hasJsonPath("$.suggestions.href", is("http://localhost:8080/{path}")));
		assertThat(result, hasJsonPath("$.suggestions.templated", is(true)));
		assertThat(result, hasNoJsonPath("$.suggestions.prompt-field"));
		assertThat(result, hasNoJsonPath("$.suggestions.value-field"));
	}

	@Value
	static class Wrapper {
		Suggestions suggestions;
	}

	@Value
	static class User {
		UUID id = UUID.randomUUID();
		String firstname, lastname;
	}
}
