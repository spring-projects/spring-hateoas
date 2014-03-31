/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.hateoas;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.hateoas.VndErrors.VndError;

/**
 * Unit tests for {@link VndErrors}.
 * 
 * @author Oliver Gierke
 */
public class VndErrorsUnitTest {

	@Test
	public void rendersToStringCorrectly() {

		VndError error = new VndErrors.VndError("logref", "message", new Link("foo", "bar"));
		assertThat(error.toString(), is("VndError[logref: logref, message: message, links: [<foo>;rel=\"bar\"]]"));

		VndErrors errors = new VndErrors(error);
		assertThat(errors.toString(),
				is("VndErrors[VndError[logref: logref, message: message, links: [<foo>;rel=\"bar\"]]]"));
	}
}
