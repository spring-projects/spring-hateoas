/*
 * Copyright 2012-2024 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link AnnotationMappingDiscoverer}.
 *
 * @author Oliver Gierke
 * @author Kevin Conaway
 * @author Mark Paluch
 */
class AnnotationMappingDiscovererUnitTest {

	MappingDiscoverer discoverer = new AnnotationMappingDiscoverer(RequestMapping.class);

	@Test
	void rejectsNullAnnotation() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new AnnotationMappingDiscoverer(null);
		});
	}

	@Test
	void discoversTypeLevelMapping() {
		assertThat(discoverer.getUriMapping(MyController.class)).isEqualTo(UriMapping.of("/type"));
	}

	@Test
	void discoversMethodLevelMapping() throws Exception {
		Method method = MyController.class.getMethod("method");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("/type/method"));
	}

	@Test
	void returnsNullForNonExistentTypeLevelMapping() {
		assertThat(discoverer.getUriMapping(ControllerWithoutTypeLevelMapping.class)).isNull();
	}

	@Test
	void resolvesMethodLevelMappingWithoutTypeLevelMapping() throws Exception {

		Method method = ControllerWithoutTypeLevelMapping.class.getMethod("method");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("/method"));
	}

	@Test
	void resolvesMethodLevelMappingWithSlashRootMapping() throws Exception {

		Method method = SlashRootMapping.class.getMethod("method");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("/method"));
	}

	/**
	 * @see #46
	 */
	@Test
	void treatsMissingMethodMappingAsEmptyMapping() throws Exception {

		Method method = MyController.class.getMethod("noMethodMapping");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("/type"));
	}

	/**
	 * @see #114
	 */
	@Test
	void detectsClassMappingOnSuperType() throws Exception {

		Method method = ChildController.class.getMethod("mapping");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("/parent/child"));
	}

	/**
	 * @see #114
	 */
	@Test
	void includesTypeMappingFromChildClass() throws Exception {

		Method method = ParentWithMethod.class.getMethod("mapping");
		assertThat(discoverer.getUriMapping(ChildWithTypeMapping.class, method)).isEqualTo(UriMapping.of("/child/parent"));
	}

	/**
	 * @see #269
	 */
	@Test
	void handlesSlashes() throws Exception {

		Method method = ControllerWithoutSlashes.class.getMethod("noslash");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("slashes/noslash"));

		method = ControllerWithoutSlashes.class.getMethod("withslash");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("slashes/withslash"));

		method = ControllerWithTrailingSlashes.class.getMethod("noslash");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("trailing/noslash"));

		method = ControllerWithTrailingSlashes.class.getMethod("withslash");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("trailing/withslash"));
	}

	/**
	 * @see #269
	 */
	@Test
	void removesMultipleSlashes() throws Exception {

		Method method = ControllerWithMultipleSlashes.class.getMethod("withslash");

		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("trailing/withslash"));
	}

	/**
	 * @see #186
	 */
	@Test
	void usesFirstMappingInCaseMultipleOnesAreDefined() throws Exception {

		Method method = MultipleMappingsController.class.getMethod("method");

		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("/type/method"));
	}

	/**
	 * @see #471
	 */
	@Test
	void discoversMethodLevelMappingUsingComposedAnnotation() throws Exception {

		Method method = MyController.class.getMethod("methodWithComposedAnnotation");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("/type/otherMethod"));
	}

	@Test // #1412
	void removesMatchingExpressionFromTemplateVariable() throws Exception {

		Method method = MyController.class.getMethod("mappingWithMatchingExpression");
		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("/type/foo/{bar}"));
	}

	@Test // #1442
	void exposesConsumesClause() throws Exception {

		Method method = MyController.class.getMethod("mappingWithConsumesClause");
		assertThat(discoverer.getConsumes(method)).containsExactly(MediaType.APPLICATION_JSON);

		method = MyController.class.getMethod("method");
		assertThat(discoverer.getConsumes(method)).isEmpty();
	}

	@Test // #1450
	void extractsMultipleRegularExpressionVariablesCorrectly() throws Exception {

		Method method = MyController.class.getMethod("multipleRegularExpressions");

		assertThat(discoverer.getUriMapping(method))
				.isEqualTo(UriMapping.of("/type/spring-web/{symbolicName}-{version}{extension}"));
	}

	@Test // #1468
	void keepsTrailingSlash() throws Exception {

		Method method = TrailingSlashes.class.getMethod("trailingSlash");

		assertThat(discoverer.getUriMapping(method)).isEqualTo(UriMapping.of("/api/myentities/"));
	}

	@RequestMapping("/type")
	interface MyController {

		@RequestMapping("/method")
		void method();

		@GetMapping("/otherMethod")
		void methodWithComposedAnnotation();

		@RequestMapping
		void noMethodMapping();

		@RequestMapping("/foo/{bar:[ABC]{1}}")
		void mappingWithMatchingExpression();

		@RequestMapping(path = "/path", consumes = "application/json")
		void mappingWithConsumesClause();

		@GetMapping("/spring-web/{symbolicName:[a-z-]+}-{version:\\d\\.\\d\\.\\d}{extension:\\.[a-z]+}")
		void multipleRegularExpressions();
	}

	interface ControllerWithoutTypeLevelMapping {

		@RequestMapping("/method")
		void method();
	}

	@RequestMapping("/")
	interface SlashRootMapping {

		@RequestMapping("/method")
		void method();
	}

	@RequestMapping("/parent")
	interface ParentController {

	}

	interface ChildController extends ParentController {

		@RequestMapping("/child")
		void mapping();
	}

	interface ParentWithMethod {

		@RequestMapping("/parent")
		void mapping();
	}

	@RequestMapping("/child")
	interface ChildWithTypeMapping extends ParentWithMethod {}

	@RequestMapping("slashes")
	interface ControllerWithoutSlashes {

		@RequestMapping("noslash")
		void noslash();

		@RequestMapping("/withslash")
		void withslash();
	}

	@RequestMapping("trailing/")
	interface ControllerWithTrailingSlashes {

		@RequestMapping("noslash")
		void noslash();

		@RequestMapping("/withslash")
		void withslash();
	}

	@RequestMapping("trailing///")
	interface ControllerWithMultipleSlashes {

		@RequestMapping("////withslash")
		void withslash();
	}

	@RequestMapping({ "/type", "/typeAlias" })
	interface MultipleMappingsController {

		@RequestMapping({ "/method", "/methodAlias" })
		void method();
	}

	// #1468

	interface TrailingSlashes {

		@RequestMapping("/api/myentities/")
		Object trailingSlash();
	}
}
