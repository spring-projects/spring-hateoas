/*
 * Copyright 2019-2020 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Integration tests for {@link HateoasConfiguration}.
 *
 * @author Oliver Drotbohm
 */
class HateoasConfigurationIntegrationTest {

	@Test // #1075
	void loadsLanguageSpecificResourceBundle() throws Exception {

		ReflectionTestUtils.setField(HateoasConfiguration.class, "I18N_BASE_NAME",
				"org/springframework/hateoas/config/rest-messages");

		try (AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext()) {

			context.register(HateoasConfiguration.class);
			context.setServletContext(new MockServletContext());
			context.refresh();

			MessageResolver resolver = context.getBean(MessageResolver.class);

			assertThat(resolver.resolve(() -> new String[] { "key" })).isEqualTo("Schlüssel");
		}
	}
}
