/*
 * Copyright 2023-2024 the original author or authors.
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
package org.springframework.hateoas.aot;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

/**
 * Unit tests for {@link RepresentationModelRuntimeHints}.
 *
 * @author Oliver Drotbohm
 */
class HateoasTypesRuntimeHintsUnitTests {

	@Test // GH-1981
	void registersHintsForHateoasTypes() {

		var registrar = new HateoasTypesRuntimeHints();
		var hints = new RuntimeHints();

		registrar.registerHints(hints, getClass().getClassLoader());

		assertThat(hints.reflection().typeHints())
				.extracting(TypeHint::getType)
				.extracting(TypeReference::getSimpleName)
				.contains("MapSuppressingUnwrappingSerializer", //
						Link.class.getSimpleName(), //
						Links.class.getSimpleName());
	}
}
