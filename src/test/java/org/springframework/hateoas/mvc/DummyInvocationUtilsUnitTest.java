/*
 * Copyright 2012 the original author or authors.
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
package org.springframework.hateoas.mvc;

import org.junit.Test;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Oliver Gierke
 */
public class DummyInvocationUtilsUnitTest extends TestUtils {

	@Test
	public void test() {

		ControllerLinkBuilder.linkTo(DummyInvocationUtils.methodOn(SampleController.class).someMethod(1L));

	}

	@RequestMapping("/sample")
	static class SampleController {

		@RequestMapping("/{id}/foo")
		HttpEntity<Void> someMethod(@PathVariable("id") Long id) {
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}
}
