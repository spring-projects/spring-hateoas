/*
 * Copyright 2012-2024 the original author or authors.
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

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Unit tests for {@link EntityModel}.
 *
 * @author Oliver Gierke
 */
class EntityModelUnitTest {

	@Test
	void equalsForSelfReference() {

		var resource = EntityModel.of("foo");
		assertThat(resource).isEqualTo(resource);
	}

	@Test
	void equalsWithEqualContent() {

		var left = EntityModel.of("foo");
		var right = EntityModel.of("foo");

		assertThat(left).isEqualTo(right);
		assertThat(right).isEqualTo(left);
	}

	@Test
	void notEqualForDifferentContent() {

		var left = EntityModel.of("foo");
		var right = EntityModel.of("bar");

		assertThat(left).isNotEqualTo(right);
		assertThat(right).isNotEqualTo(left);
	}

	@Test
	void notEqualForDifferentLinks() {

		var left = EntityModel.of("foo");
		var right = EntityModel.of("foo")
				.add(Link.of("localhost"));

		assertThat(left).isNotEqualTo(right);
		assertThat(right).isNotEqualTo(left);
	}

	@Test
	void rejectsCollectionContent() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			EntityModel.of(Collections.emptyList());
		});
	}

	@Test // #1371
	void producesProperExceptionWhenRenderingAJsonValue() throws Exception {

		var model = EntityModel.of(new ValueType());

		assertThatExceptionOfType(DatabindException.class)
				.isThrownBy(() -> new JsonMapper().writeValueAsString(model))
				.withMessageContaining("@JsonValue");
	}

	@Test // #1371
	void rendersTypeIdentifiersCorrectly() throws Exception {

		var model = EntityModel.of(new TypeWithId());

		assertThatNoException()
				.isThrownBy(() -> new JsonMapper().writeValueAsString(model));
	}

	// #1371

	static class ValueType {
		@JsonValue String type;
	}

	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@jsonObjectId")
	static class TypeWithId {}
}
