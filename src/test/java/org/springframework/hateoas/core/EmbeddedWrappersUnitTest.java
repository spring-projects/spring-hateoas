/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.hateoas.core;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

/**
 * Unit tests for {@link EmbeddedWrappers}.
 * 
 * @author Oliver Gierke
 */
public class EmbeddedWrappersUnitTest {

	EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

	/**
	 * @see #286
	 */
	@Test
	@SuppressWarnings("rawtypes")
	public void createsWrapperForEmptyCollection() {

		EmbeddedWrapper wrapper = wrappers.emptyCollectionOf(String.class);

		assertEmptyCollectionValue(wrapper);
		assertThat(wrapper.getRel()).isNull();
		assertThat(wrapper.getRelTargetType()).isEqualTo((Class) String.class);
	}

	/**
	 * @see #286
	 */
	@Test
	public void createsWrapperForEmptyCollectionAndExplicitRel() {

		EmbeddedWrapper wrapper = wrappers.wrap(Collections.emptySet(), "rel");

		assertEmptyCollectionValue(wrapper);
		assertThat(wrapper.getRel()).isEqualTo("rel");
		assertThat(wrapper.getRelTargetType()).isNull();
	}

	/**
	 * @see #286
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsEmptyCollectionWithoutExplicitRel() {
		wrappers.wrap(Collections.emptySet());
	}

	@SuppressWarnings("unchecked")
	private static void assertEmptyCollectionValue(EmbeddedWrapper wrapper) {
		assertThat(wrapper.getValue()).isInstanceOfSatisfying(Collection.class, it -> assertThat(it).isEmpty());
	}
}
