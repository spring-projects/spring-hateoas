/*
 * Copyright 2020-2021 the original author or authors.
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
package org.springframework.hateoas.server.core;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.TestUtils;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Unit tests for {@link PropertyResolvingMappingDiscoverer}.
 *
 * @author Lars Michele
 * @author Oliver Drotbohm
 */
@SpringJUnitWebConfig(classes = PropertyResolvingMappingDiscovererUnitTest.Config.class)
@TestPropertySource(properties = { "test.parent=resolvedparent", "test.child=resolvedchild" })
class PropertyResolvingMappingDiscovererUnitTest extends TestUtils {

	@Autowired WebApplicationContext context;

	@BeforeEach
	void contextLoading() {
		new ContextLoader(context).initWebApplicationContext(new MockServletContext());
	}

	/**
	 * @see #361
	 */
	@Test
	void resolvesVariablesInMappings() throws NoSuchMethodException {

		Method method = ResolveMethodEndpointController.class.getMethod("method");
		AnnotationMappingDiscoverer annotationMappingDiscoverer = new AnnotationMappingDiscoverer(RequestMapping.class);

		// Test plain AnnotationMappingDiscoverer first
		assertThat(annotationMappingDiscoverer.getMapping(ResolveEndpointController.class)).isEqualTo("/${test.parent}");
		assertThat(annotationMappingDiscoverer.getMapping(ResolveMethodEndpointController.class, method))
				.isEqualTo("/${test.parent}/${test.child}");

		PropertyResolvingMappingDiscoverer propertyResolvingMappingDiscoverer = new PropertyResolvingMappingDiscoverer(
				annotationMappingDiscoverer);

		assertThat(propertyResolvingMappingDiscoverer.getMapping(ResolveEndpointController.class))
				.isEqualTo("/resolvedparent");
		assertThat(propertyResolvingMappingDiscoverer.getMapping(method)).isEqualTo("/resolvedparent/resolvedchild");
		assertThat(propertyResolvingMappingDiscoverer.getMapping(ResolveMethodEndpointController.class, method))
				.isEqualTo("/resolvedparent/resolvedchild");
	}

	@RequestMapping("/${test.parent}")
	interface ResolveEndpointController {}

	@RequestMapping("/${test.parent}")
	interface ResolveMethodEndpointController {

		@RequestMapping("/${test.child}")
		void method();
	}

	@Configuration
	public static class Config {}
}
