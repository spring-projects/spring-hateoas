/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.hateoas.mediatype.vnderror;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors.VndError;

/**
 * Unit tests for {@link VndErrors}.
 * 
 * @author Oliver Gierke
 */
class VndErrorsUnitTest {

	@Test
	void rendersToStringCorrectly() {

		VndError error = new VndErrors.VndError("logref", "message", new Link("foo", "bar"));
		assertThat(error.toString()).isEqualTo("VndError[logref: logref, message: message, links: [<foo>;rel=\"bar\"]]");

		VndErrors errors = new VndErrors(error);
		assertThat(errors.toString()) //
				.isEqualTo("VndErrors[VndError[logref: logref, message: message, links: [<foo>;rel=\"bar\"]]]");
	}
}
