/*
 * Copyright 2025 the original author or authors.
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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @author Oliver Drotbohm
 */
public class CustomizationTest {

	@Test
	void usesFieldForDeserialization() {

		ObjectMapper mapper = JsonMapper.builder().addMixIn(Sample.class, SampleMixin.class).build();

		Sample result = mapper.readValue("{\"value\" : [ \"Value\" ] }", Sample.class);

		assertThat(result.getValue()).containsExactly("FIXED!");
	}

	public static class Sample {

		private List<String> values;

		public List<String> getValue() {
			return values;
		}
	}

	public static abstract class SampleMixin extends Sample {

		@Override
		@JsonDeserialize(using = CustomDeserializer.class)
		public abstract List<String> getValue();
	}

	public static class CustomDeserializer extends ValueDeserializer<List<String>> {

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser, tools.jackson.databind.DeserializationContext)
		 */
		@Override
		public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
			return List.of("FIXED!");
		}
	}
}
