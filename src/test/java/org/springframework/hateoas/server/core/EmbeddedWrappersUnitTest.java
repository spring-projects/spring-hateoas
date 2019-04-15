/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.hateoas.server.core;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.LinkRelation;

/**
 * Unit tests for {@link EmbeddedWrappers}.
 *
 * @author Oliver Gierke
 */
class EmbeddedWrappersUnitTest {

	EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

	/**
	 * @see #286
	 */
	@Test
	void createsWrapperForEmptyCollection() {

		EmbeddedWrapper wrapper = wrappers.emptyCollectionOf(String.class);

		assertEmptyCollectionValue(wrapper);
		assertThat(wrapper.getRel()).isEmpty();
		assertThat(wrapper.getRelTargetType()).isEqualTo(String.class);
	}

	/**
	 * @see #286
	 */
	@Test
	void createsWrapperForEmptyCollectionAndExplicitRel() {

		EmbeddedWrapper wrapper = wrappers.wrap(Collections.emptySet(), LinkRelation.of("rel"));

		assertEmptyCollectionValue(wrapper);
		assertThat(wrapper.getRel()).hasValue(LinkRelation.of("rel"));
		assertThat(wrapper.getRelTargetType()).isNull();
	}

	/**
	 * @see #286
	 */
	@Test
	void rejectsEmptyCollectionWithoutExplicitRel() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			wrappers.wrap(Collections.emptySet());
		});
	}

	@SuppressWarnings("unchecked")
	private static void assertEmptyCollectionValue(EmbeddedWrapper wrapper) {

		assertThat(wrapper.getValue()) //
				.isInstanceOfSatisfying(Collection.class, it -> assertThat(it).isEmpty());
	}
}
