/*
 * Copyright 2019-2021 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.support.ContextTester;

/**
 * Integration tests for {@link RestTemplateHateoasConfiguration}.
 *
 * @author Oliver Drotbohm
 */
class RestTemplateHateoasConfigurationIntegrationTest {

	@Test // #1119
	void bootstrapsWithManualImports() {
		ContextTester.withContext(WithImports.class, context -> {});
	}

	@Test // #1119
	void bootstrapsViaAnnotationConfiguration() {
		ContextTester.withContext(AnnotationConfig.class, context -> {});
	}

	@Configuration
	@Import({ HateoasConfiguration.class, RestTemplateHateoasConfiguration.class })
	static class WithImports {}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class AnnotationConfig {}
}
