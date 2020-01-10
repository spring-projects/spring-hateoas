/*
 * Copyright 2012-2020 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Oliver Gierke
 */
class DummyInvocationUtilsUnitTest extends TestUtils {

	@Test
	void pathVariableWithDefaultParameter() {

		Link link = linkTo(methodOn(SampleController.class).someMethod(1L)).withSelfRel();

		assertThat(link.getHref()).isEqualTo("http://localhost/sample/1/foo");
	}

	@Test
	void pathVariableWithNameParameter() {

		Link link = linkTo(methodOn(SampleController.class).someOtherMethod(2L)).withSelfRel();

		assertThat(link.getHref()).isEqualTo("http://localhost/sample/2/bar");
	}

	@RequestMapping("/sample")
	static class SampleController {

		@RequestMapping("/{id}/foo")
		HttpEntity<Void> someMethod(@PathVariable("id") Long id) {
			return new ResponseEntity<>(HttpStatus.OK);
		}

		@RequestMapping("/{otherName}/bar")
		HttpEntity<Void> someOtherMethod(@PathVariable(name = "otherName") Long id) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}
}
