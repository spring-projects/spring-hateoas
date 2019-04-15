/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.support.WebStack;

/**
 * Unit tests for {@link WebStackImportSelector}.
 *
 * @author Oliver Drotbohm
 */
class WebStackImportSelectorUnitTest {

	WebStackImportSelector selector = new WebStackImportSelector();

	@Test // #973
	void activatesAllWebStacksByDefault() {

		AnnotationMetadata metadata = new StandardAnnotationMetadata(DefaultHypermedia.class);

		assertThat(selector.selectImports(metadata))
				.containsExactlyInAnyOrderElementsOf(WebStackImportSelector.CONFIGS.values());
	}

	@Test // #973
	void activatesWebMvcOnlyifConfigured() {

		AnnotationMetadata metadata = new StandardAnnotationMetadata(WebMvcHypermedia.class);

		assertThat(selector.selectImports(metadata)).containsExactly(WebStackImportSelector.CONFIGS.get(WebStack.WEBMVC));
	}

	@Test // #973
	void activatesWebFluxOnlyIfConfigured() {

		AnnotationMetadata metadata = new StandardAnnotationMetadata(WebFluxHypermedia.class);

		assertThat(selector.selectImports(metadata)).containsExactly(WebStackImportSelector.CONFIGS.get(WebStack.WEBFLUX));
	}

	@Test // #973
	void rejectsNoStacksSelected() {

		AnnotationMetadata metadata = new StandardAnnotationMetadata(NoStacksHypermedia.class);

		assertThatExceptionOfType(IllegalStateException.class) //
				.isThrownBy(() -> selector.selectImports(metadata)) //
				.withMessageContaining(NoStacksHypermedia.class.getName());
	}

	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class DefaultHypermedia {}

	@EnableHypermediaSupport(type = HypermediaType.HAL, stacks = WebStack.WEBMVC)
	static class WebMvcHypermedia {}

	@EnableHypermediaSupport(type = HypermediaType.HAL, stacks = WebStack.WEBFLUX)
	static class WebFluxHypermedia {}

	@EnableHypermediaSupport(type = HypermediaType.HAL, stacks = {})
	static class NoStacksHypermedia {}
}
