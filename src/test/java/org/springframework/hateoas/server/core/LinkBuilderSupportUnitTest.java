/*
 * Copyright 2013-2014 the original author or authors.
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

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.TestUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Unit tests for {@link LinkBuilderSupport}.
 *
 * @author Oliver Gierke
 * @author Kamill Sokol
 */
class LinkBuilderSupportUnitTest extends TestUtils {

	@Test
	void callingSlashWithEmptyStringIsNoOp() {

		SampleLinkBuilder builder = new SampleLinkBuilder(UriComponentsBuilder.newInstance(), Collections.emptyList());
		assertThat(builder.slash("")).isEqualTo(builder);
	}

	@Test
	void appendsFragmentCorrectly() {

		SampleLinkBuilder builder = new SampleLinkBuilder(UriComponentsBuilder.newInstance(), Collections.emptyList());
		builder = builder.slash("foo#bar");
		assertThat(builder.toString()).endsWith("foo#bar");
		builder = builder.slash("bar");
		assertThat(builder.toString()).endsWith("foo/bar#bar");
		builder = builder.slash("#foo");
		assertThat(builder.toString()).endsWith("foo/bar#foo");
		builder = builder.slash("#");
		assertThat(builder.toString()).endsWith("foo/bar#foo");
		builder = builder.slash("foo bar");
		assertThat(builder.toString()).endsWith("foo%20bar#foo");
	}

	/**
	 * @see #582
	 */
	@Test
	void appendsPathContainingColonsCorrectly() {

		SampleLinkBuilder builder = new SampleLinkBuilder(UriComponentsBuilder.newInstance(), Collections.emptyList());

		builder = builder.slash("47:11");

		assertThat(builder.toString()).endsWith("47:11");
	}

	static class SampleLinkBuilder extends LinkBuilderSupport<SampleLinkBuilder> {

		public SampleLinkBuilder(UriComponentsBuilder builder, List<Affordance> afforances) {
			super(builder, afforances);
		}

		@Override
		protected SampleLinkBuilder getThis() {
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.LinkBuilderSupport#createNewInstance(org.springframework.web.util.UriComponentsBuilder, java.util.List)
		 */
		@Override
		protected SampleLinkBuilder createNewInstance(UriComponentsBuilder builder, List<Affordance> affordances) {
			return new SampleLinkBuilder(builder, affordances);
		}
	}
}
