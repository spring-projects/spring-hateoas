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
package org.springframework.hateoas.mediatype.hal.forms;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.support.ContextTester.*;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.mediatype.MediaTypeTestUtils;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Integration tests for HAL media type configuration.
 *
 * @author Oliver Drotbohm
 */
class HalFormsMediaTypeConfigurationIntegrationTest {

	static final MediaType CUSTOM_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.my-custom-mediatype");

	@Test // #1591
	void includesCustomMediaTypeFromConfiguration() {

		withServletContext(ConfigurationWithCustomMediaType.class, it -> {
			assertThat(MediaTypeTestUtils.getSupportedHypermediaTypes(it)).contains(CUSTOM_MEDIA_TYPE);
		});
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = HypermediaType.HAL_FORMS)
	static class ConfigurationWithCustomMediaType {

		@Bean
		HalFormsConfiguration halConfiguration() {
			return new HalFormsConfiguration().withMediaType(CUSTOM_MEDIA_TYPE);
		}
	}
}
