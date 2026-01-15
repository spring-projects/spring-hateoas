/*
 * Copyright 2023-2026 the original author or authors.
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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.JdkProxyHint;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;
import org.springframework.core.DecoratingProxy;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.server.core.LastInvocationAware;

/**
 * Unit tests for {@link HateoasTypesRuntimeHints}.
 *
 * @author Oliver Drotbohm
 */
class HateoasTypesRuntimeHintsUnitTests {

	@Test // GH-1981
	void registersHintsForHateoasTypes() {

		assertThat(createHints().reflection().typeHints())
				.extracting(TypeHint::getType)
				.extracting(TypeReference::getSimpleName)
				.contains("MapSuppressingUnwrappingSerializer", //
						Link.class.getSimpleName(), //
						Links.class.getSimpleName());
	}

	@Test // GH-2384
	void registersProxyHintsForLastInvocationAware() {

		assertThat(createHints().proxies().jdkProxyHints())
				.extracting(JdkProxyHint::getProxiedInterfaces)
				.contains(List.of(TypeReference.of(LastInvocationAware.class), TypeReference.of(Map.class),
						TypeReference.of(SpringProxy.class), TypeReference.of(Advised.class),
						TypeReference.of(DecoratingProxy.class)));
	}

	private static RuntimeHints createHints() {

		var registrar = new HateoasTypesRuntimeHints();
		var hints = new RuntimeHints();

		registrar.registerHints(hints, HateoasTypesRuntimeHintsUnitTests.class.getClassLoader());

		return hints;
	}
}
