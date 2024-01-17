/*
 * Copyright 2022-2024 the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.SlicedModel.SliceMetadata;

/**
 * Unit tests for SlicedModel
 *
 * @author Michael Schout
 * @author Oliver Drotbohm
 * @since 2.1
 */
class SlicedModelUnitTest {

	static final SliceMetadata metadata = new SliceMetadata(10, 1);

	SlicedModel<Object> resources;

	@BeforeEach
	void setUp() {
		resources = SlicedModel.of(Collections.emptyList(), metadata);
	}

	@Test // #1856
	void discoversNextLink() {

		resources.add(Link.of("foo", IanaLinkRelations.NEXT.value()));

		assertThat(resources.getNextLink()).isNotNull();
	}

	@Test // #1856
	void discoversPreviousLink() {

		resources.add(Link.of("custom", IanaLinkRelations.PREV.value()));

		assertThat(resources.getPreviousLink()).isNotNull();
	}

	@Test // #1856
	void preventsNegativeSliceSize() {

		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SliceMetadata(-1, 0));
	}

	@Test // #1856
	void preventsNegativeSliceNumber() {

		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SliceMetadata(0, -1));
	}

	@Test // #1856
	void exposesElementTypeForEmpty() {

		var fallbackType = ResolvableType.forClassWithGenerics(EntityModel.class, String.class);
		var model = SlicedModel.empty(fallbackType);

		assertThat(model.getResolvableType().getGeneric(0).resolve()).isEqualTo(EntityModel.class);
	}
}
