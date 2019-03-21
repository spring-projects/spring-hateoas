/*
 * Copyright 2012-2019 the original author or authors.
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
package org.springframework.hateoas.core;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link AnnotationMappingDiscoverer}.
 * 
 * @author Oliver Gierke
 * @author Kevin Conaway
 * @author Mark Paluch
 * @author Josh Ghiloni
 * @author Greg Turnquist
 */
public class AnnotationMappingDiscovererUnitTest {

	AnnotationMappingDiscoverer discoverer = new AnnotationMappingDiscoverer(RequestMapping.class);

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullAnnotation() {
		new AnnotationMappingDiscoverer(null);
	}

	@Test
	public void discoversTypeLevelMapping() {
		assertThat(discoverer.getMapping(MyController.class)).isEqualTo("/type");
	}

	@Test
	public void discoversMethodLevelMapping() throws Exception {
		Method method = MyController.class.getMethod("method");
		assertThat(discoverer.getMapping(method)).isEqualTo("/type/method");
	}

	@Test
	public void returnsNullForNonExistentTypeLevelMapping() {
		assertThat(discoverer.getMapping(ControllerWithoutTypeLevelMapping.class)).isNull();
	}

	@Test
	public void resolvesMethodLevelMappingWithoutTypeLevelMapping() throws Exception {

		Method method = ControllerWithoutTypeLevelMapping.class.getMethod("method");
		assertThat(discoverer.getMapping(method)).isEqualTo("/method");
	}

	@Test
	public void resolvesMethodLevelMappingWithSlashRootMapping() throws Exception {

		Method method = SlashRootMapping.class.getMethod("method");
		assertThat(discoverer.getMapping(method)).isEqualTo("/method");
	}

	/**
	 * @see #46
	 */
	@Test
	public void treatsMissingMethodMappingAsEmptyMapping() throws Exception {

		Method method = MyController.class.getMethod("noMethodMapping");
		assertThat(discoverer.getMapping(method)).isEqualTo("/type");
	}

	/**
	 * @see #114
	 */
	@Test
	public void detectsClassMappingOnSuperType() throws Exception {

		Method method = ChildController.class.getMethod("mapping");
		assertThat(discoverer.getMapping(method)).isEqualTo("/parent/child");
	}

	/**
	 * @see #114
	 */
	@Test
	public void includesTypeMappingFromChildClass() throws Exception {

		Method method = ParentWithMethod.class.getMethod("mapping");
		assertThat(discoverer.getMapping(ChildWithTypeMapping.class, method)).isEqualTo("/child/parent");
	}

	/**
	 * @see #269
	 */
	@Test
	public void handlesSlashes() throws Exception {

		Method method = ControllerWithoutSlashes.class.getMethod("noslash");
		assertThat(discoverer.getMapping(method)).isEqualTo("slashes/noslash");

		method = ControllerWithoutSlashes.class.getMethod("withslash");
		assertThat(discoverer.getMapping(method)).isEqualTo("slashes/withslash");

		method = ControllerWithTrailingSlashes.class.getMethod("noslash");
		assertThat(discoverer.getMapping(method)).isEqualTo("trailing/noslash");

		method = ControllerWithTrailingSlashes.class.getMethod("withslash");
		assertThat(discoverer.getMapping(method)).isEqualTo("trailing/withslash");
	}

	/**
	 * @see #269
	 */
	@Test
	public void removesMultipleSlashes() throws Exception {

		Method method = ControllerWithMultipleSlashes.class.getMethod("withslash");

		assertThat(discoverer.getMapping(method)).isEqualTo("trailing/withslash");
	}

	/**
	 * @see #186
	 */
	@Test
	public void usesFirstMappingInCaseMultipleOnesAreDefined() throws Exception {

		Method method = MultipleMappingsController.class.getMethod("method");

		assertThat(discoverer.getMapping(method)).isEqualTo("/type/method");
	}

	/**
	 * @see #471
	 */
	@Test
	public void discoversMethodLevelMappingUsingComposedAnnotation() throws Exception {

		Method method = MyController.class.getMethod("methodWithComposedAnnotation");
		assertThat(discoverer.getMapping(method)).isEqualTo("/type/otherMethod");
	}

	/**
	 * @see #361
	 */
	@Test
	public void resolvesVariablesInMappings() throws NoSuchMethodException {

		Method method = DynamicEndpointControllerWithMethod.class.getMethod("method");

		// Test regression first
		assertThat(discoverer.getMapping(DynamicEndpointController.class)).isEqualTo("/${test.variable}");
		assertThat(discoverer.getMapping(DynamicEndpointControllerWithMethod.class, method))
			.isEqualTo("/${test.variable}/${test.child}");

		// Test property substitution
		Map<String, Object> source = new HashMap<>();
		source.put("test.variable", "dynamicparent");
		source.put("test.child", "dynamicchild");

		StandardEnvironment env = new StandardEnvironment();
		env.getPropertySources().addLast(new MapPropertySource("mapping-env", source));
		
		PropertyResolvingDiscoverer propertyResolvingDiscoverer = new PropertyResolvingDiscoverer(discoverer, env);

		assertThat(propertyResolvingDiscoverer.getMapping(DynamicEndpointController.class)).isEqualTo("/dynamicparent");
		assertThat(propertyResolvingDiscoverer.getMapping(method)).isEqualTo("/dynamicparent/dynamicchild");
		assertThat(propertyResolvingDiscoverer.getMapping(DynamicEndpointControllerWithMethod.class, method))
			.isEqualTo("/dynamicparent/dynamicchild");
	}

	@RequestMapping("/type")
	interface MyController {

		@RequestMapping("/method")
		void method();

		@GetMapping("/otherMethod")
		void methodWithComposedAnnotation();

		@RequestMapping
		void noMethodMapping();
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

	@RequestMapping("/${test.variable}")
	interface DynamicEndpointController {}

	@RequestMapping("/${test.variable}")
	interface DynamicEndpointControllerWithMethod {

		@RequestMapping("/${test.child}")
		void method();
	}
}
