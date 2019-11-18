/*
 * Copyright 2014-2020 the original author or authors.
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

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors.VndError;

/**
 * Unit tests for {@link VndErrors}.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class VndErrorsUnitTest {

	/**
	 * @see #775
	 */
	@Test
	void vndErrorsDoesntTakeNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> new VndErrors().withErrors(null));
	}

	/**
	 * @see #775
	 */
	@Test
	public void vndErrorsDoesntTakeEmptyCollection() {
		assertThatIllegalArgumentException().isThrownBy(() -> new VndErrors().withErrors(new ArrayList<>()));
	}

	/**
	 * @see #775
	 */
	@Test
	public void vndErrorsUsingSingleErrorArguments() {

		VndErrors errors = new VndErrors().withError(new VndError("message", "/path", 50, Link.of("/link").withSelfRel()));

		assertThat(errors.getTotal()).isNull();
		assertThat(errors.getContent()).hasSize(1);
		assertThat(errors.getContent())
				.containsExactly(new VndError("message", "/path", 50, Link.of("/link").withSelfRel()));
	}

	/**
	 * @see #775
	 */
	@Test
	public void appendingVndErrorsShouldWork() {

		VndErrors errors = new VndErrors().withError(new VndError("message", "/path", 50, Link.of("/link").withSelfRel()));
		assertThat(errors.getContent()).hasSize(1);

		errors.getContent().add(new VndError("message2", "/path2", 51, Link.of("/link2", "link2")));
		assertThat(errors.getContent()).hasSize(2);
	}

	@Test
	void vndErrorRendersToStringCorrectly() {

		VndError error = new VndError("message", "path", 42, Link.of("foo", "bar"));
		assertThat(error.toString()).isEqualTo("VndError[logref: 42, message: message, links: [<foo>;rel=\"bar\"]]");

	}

	@Test
	void vndErrorsRendersToStringCorrectly() {

		VndErrors errors = new VndErrors(new VndError("message", "path", 42, Link.of("foo", "bar")));
		assertThat(errors.toString())
				.isEqualTo("VndErrors[VndError[logref: 42, message: message, links: [<foo>;rel=\"bar\"]]]");
	}
}
