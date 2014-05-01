/*
 * Copyright 2012-2014 the original author or authors.
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
package org.springframework.hateoas.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link AnnotationMappingDiscoverer}.
 * 
 * @author Oliver Gierke
 */
public class AnnotationMappingDiscovererUnitTest {

	MappingDiscoverer discoverer = new AnnotationMappingDiscoverer(RequestMapping.class);

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullAnnotation() {
		new AnnotationMappingDiscoverer(null);
	}

	@Test
	public void discoversTypeLevelMapping() {
		assertThat(discoverer.getMapping(MyController.class), is("/type"));
	}

	@Test
	public void discoversMethodLevelMapping() throws Exception {
		Method method = MyController.class.getMethod("method");
		assertThat(discoverer.getMapping(method), is("/type/method"));
	}

	@Test
	public void returnsNullForNonExistentTypeLevelMapping() {
		assertThat(discoverer.getMapping(ControllerWithoutTypeLevelMapping.class), is(nullValue()));
	}

	@Test
	public void resolvesMethodLevelMappingWithoutTypeLevelMapping() throws Exception {

		Method method = ControllerWithoutTypeLevelMapping.class.getMethod("method");
		assertThat(discoverer.getMapping(method), is("/method"));
	}

	@Test
	public void resolvesMethodLevelMappingWithSlashRootMapping() throws Exception {

		Method method = SlashRootMapping.class.getMethod("method");
		assertThat(discoverer.getMapping(method), is("/method"));
	}

	/**
	 * @see #46
	 */
	@Test
	public void treatsMissingMethodMappingAsEmptyMapping() throws Exception {

		Method method = MyController.class.getMethod("noMethodMapping");
		assertThat(discoverer.getMapping(method), is("/type"));
	}

	/**
	 * @see #114
	 */
	@Test
	public void detectsClassMappingOnSuperType() throws Exception {

		Method method = ChildController.class.getMethod("mapping");
		assertThat(discoverer.getMapping(method), is("/parent/child"));
	}

	/**
	 * @see #114
	 */
	@Test
	public void includesTypeMappingFromChildClass() throws Exception {

		Method method = ParentWithMethod.class.getMethod("mapping");
		assertThat(discoverer.getMapping(ChildWithTypeMapping.class, method), is("/child/parent"));
	}

	@RequestMapping("/type")
	interface MyController {

		@RequestMapping("/method")
		void method();

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
}
