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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;

import org.junit.Test;
import org.springframework.hateoas.VndErrors.VndError;

/**
 * Unit tests for {@link VndErrors}.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class VndErrorsUnitTest {

	/**
	 * @see #775
	 */
	@Test(expected = IllegalArgumentException.class)
	public void vndErrorsDoesntTakeNull() {
		new VndErrors().withErrors(null);
	}

	/**
	 * @see #775
	 */
	@Test(expected = IllegalArgumentException.class)
	public void vndErrorsDoesntTakeEmptyCollection() {
		new VndErrors().withErrors(new ArrayList<>());
	}

	/**
	 * @see #775
	 */
	@Test
	public void vndErrorsUsingSingleErrorArguments() {

		VndErrors errors = new VndErrors().withError(new VndError("message", "/path", 50, new Link("/link").withSelfRel()));

		assertThat(errors.getTotal()).isEqualTo(1);
		assertThat(errors.getContent()).hasSize(1);
		assertThat(errors.getContent())
			.containsExactly(new VndError("message", "/path", 50, new Link("/link").withSelfRel()));
	}

	/**
	 * @see #775
	 */
	@Test
	public void appendingVndErrorsShouldWork() {

		VndErrors errors = new VndErrors().withError(new VndError("message", "/path", 50, new Link("/link").withSelfRel()));

		errors.getContent().add(new VndError("message2", "/path2", 51, new Link("/link2", "link2")));
	}

	/**
	 * @see #775
	 */
	@Test
	public void vndErrorRendersToStringCorrectly() {

		VndError error = new VndErrors.VndError("message", "path", 50, new Link("foo", "bar"));
		assertThat(error.toString()).isEqualTo("VndError{message='message', path='path', logref=50, links=[<foo>;rel=\"bar\"]}");

		VndErrors errors = new VndErrors().withError(error);
		assertThat(errors.toString()) //
				.isEqualTo("VndErrors(errors=[VndError{message='message', path='path', logref=50, links=[<foo>;rel=\"bar\"]}], message=null, logref=null)");
	}
}
