/*
 * Copyright 2013-2020 the original author or authors.
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
import org.springframework.hateoas.PagedModel.PageMetadata;

/**
 * Unit tests for {@link PagedModel}.
 * 
 * @author Oliver Gierke
 */
class PagedModelUnitTest {

	static final PageMetadata metadata = new PagedModel.PageMetadata(10, 1, 200);

	PagedModel<Object> resources;

	@BeforeEach
	void setUp() {
		resources = PagedModel.of(Collections.emptyList(), metadata);
	}

	@Test
	void discoversNextLink() {

		resources.add(Link.of("foo", IanaLinkRelations.NEXT.value()));

		assertThat(resources.getNextLink()).isNotNull();
	}

	@Test
	void discoversPreviousLink() {

		resources.add(Link.of("custom", IanaLinkRelations.PREV.value()));

		assertThat(resources.getPreviousLink()).isNotNull();
	}

	/**
	 * @see #89
	 */
	@Test
	void preventsNegativePageSize() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new PageMetadata(-1, 0, 0);
		});
	}

	/**
	 * @see #89
	 */
	@Test
	void preventsNegativePageNumber() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new PageMetadata(0, -1, 0);
		});
	}

	/**
	 * @see #89
	 */
	@Test
	void preventsNegativeTotalElements() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new PageMetadata(0, 0, -1);
		});
	}

	/**
	 * @see #89
	 */
	@Test
	void preventsNegativeTotalPages() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new PageMetadata(0, 0, 0, -1);
		});
	}

	/**
	 * @see #89
	 */
	@Test
	void allowsOneIndexedPages() {
		new PageMetadata(10, 1, 0);
	}

	/**
	 * @see #309
	 */
	@Test
	void calculatesTotalPagesCorrectly() {
		assertThat(new PageMetadata(5, 0, 16).getTotalPages()).isEqualTo(4L);
	}
}
