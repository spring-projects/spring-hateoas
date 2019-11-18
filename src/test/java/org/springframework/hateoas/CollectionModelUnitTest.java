/*
 * Copyright 2012-2020 the original author or authors.
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

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

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
}
