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
package org.springframework.hateoas.server.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.server.mvc.MvcLink.*;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.TestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link MvcLink}.
 *
 * @author Oliver Drotbohm
 */
class MvcLinkUnitTests extends TestUtils {

	@Test
	void createsLinkPointingToController() {

		Object invocation = on(MyController.class).get("4711");
		String expected = "http://localhost/4711";

		assertThat(MvcLink.of(invocation, IanaLinkRelations.SELF).getHref()).isEqualTo(expected);
		assertThat(MvcLink.of(() -> invocation, IanaLinkRelations.SELF).getHref()).isEqualTo(expected);
	}

	static class MyController {

		@RequestMapping("/{id}")
		HttpEntity<?> get(@PathVariable String id) {
			return ResponseEntity.ok().build();
		}
	}
}
