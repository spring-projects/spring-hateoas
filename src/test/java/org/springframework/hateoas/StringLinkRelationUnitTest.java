/*
 * Copyright 2019-2026 the original author or authors.
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

import lombok.Value;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * Unit tests for {@link StringLinkRelation}.
 *
 * @author Oliver Gierke
 */
class StringLinkRelationUnitTest {

	ContextualMapper $ = MappingTestUtils.createMapper();

	@Test
	void serializesAsPlainString() throws Exception {

		$.assertSerializes(new Sample(StringLinkRelation.of("foo")))
				.into("{\"relation\":\"foo\"}")
				.andBack();
	}

	@Value
	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	static class Sample {
		StringLinkRelation relation;
	}
}
