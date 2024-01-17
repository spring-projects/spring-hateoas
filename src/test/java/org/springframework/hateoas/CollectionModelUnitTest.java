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

import lombok.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.lang.Nullable;

/**
 * Unit tests for {@link CollectionModel}.
 *
 * @author Oliver Gierke
 */
class CollectionModelUnitTest {

	Set<EntityModel<String>> foo = Collections.singleton(EntityModel.of("foo"));
	Set<EntityModel<String>> bar = Collections.singleton(EntityModel.of("bar"));

	@Test
	void equalsForSelfReference() {

		CollectionModel<EntityModel<String>> resource = CollectionModel.of(foo);
		assertThat(resource).isEqualTo(resource);
	}

	@Test
	void equalsWithEqualContent() {

		CollectionModel<EntityModel<String>> left = CollectionModel.of(foo);
		CollectionModel<EntityModel<String>> right = CollectionModel.of(foo);

		assertThat(left).isEqualTo(right);
		assertThat(right).isEqualTo(left);
	}

	@Test
	void notEqualForDifferentContent() {

		CollectionModel<EntityModel<String>> left = CollectionModel.of(foo);
		CollectionModel<EntityModel<String>> right = CollectionModel.of(bar);

		assertThat(left).isNotEqualTo(right);
		assertThat(right).isNotEqualTo(left);
	}

	@Test
	void notEqualForDifferentLinks() {

		CollectionModel<EntityModel<String>> left = CollectionModel.of(foo);
		CollectionModel<EntityModel<String>> right = CollectionModel.of(bar);
		right.add(Link.of("localhost"));

		assertThat(left).isNotEqualTo(right);
		assertThat(right).isNotEqualTo(left);
	}

	@TestFactory // #1590
	Stream<DynamicTest> exposesElementTypeForCollection() {
		return DynamicTest.stream(Fixture.probes(), Fixture::toString, Fixture::verify);
	}

	@Value(staticConstructor = "$")
	static class Fixture {

		CollectionModel<?> model;
		@Nullable Class<?> expectedElementType;

		static Stream<Fixture> probes() {

			return Stream.of(
					$(CollectionModel.empty(), null),
					$(CollectionModel.empty(String.class), String.class),
					$(CollectionModel.of(Arrays.asList(new Person())).withFallbackType(Contact.class), Person.class),
					$(CollectionModel.of(Arrays.asList(new Person(), new Company())).withFallbackType(Object.class),
							Contact.class));
		}

		void verify() {
			assertThat(model.getResolvableType().getGeneric(0).resolve()).isEqualTo(expectedElementType);
		}

		@Override
		public String toString() {
			return String.format("Expect element type %s for collection model %s.", expectedElementType, model);
		}
	}

	static class Contact {}

	static class Person extends Contact {}

	static class Company extends Contact {}

}
