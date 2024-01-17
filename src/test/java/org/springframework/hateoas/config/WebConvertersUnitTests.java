/*
 * Copyright 2021-2024 the original author or authors.
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
package org.springframework.hateoas.config;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link WebConverters}.
 *
 * @author Oliver Drotbohm
 */
class WebConvertersUnitTests {

	@Test // #1657
	void augmentsConvertersWithoutHypermediaInformationsRegistered() {

		WebConverters converters = WebConverters.of(new ObjectMapper(), Collections.emptyList());

		assertThatNoException() //
				.isThrownBy(() -> converters.augmentClient(Collections.emptyList()));
	}
}
