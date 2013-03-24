/*
 * Copyright 2013 the original author or authors.
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

import org.junit.Test;
import org.springframework.hateoas.TestUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Unit tests for {@link LinkBuilderSupport}.
 * 
 * @author Oliver Gierke
 */
public class LinkBuilderSupportUnitTest extends TestUtils {

	@Test
	public void callingSlashWithEmptyStringIsNoOp() {

		SampleLinkBuilder builder = new SampleLinkBuilder(UriComponentsBuilder.newInstance());
		assertThat(builder.slash(""), is(builder));
	}

	@Test
	public void appendsFragmentCorrectly() {

		SampleLinkBuilder builder = new SampleLinkBuilder(UriComponentsBuilder.newInstance());
		builder = builder.slash("foo#bar");
		assertThat(builder.toString(), endsWith("foo#bar"));
		builder = builder.slash("bar");
		assertThat(builder.toString(), endsWith("foo/bar#bar"));
		builder = builder.slash("#foo");
		assertThat(builder.toString(), endsWith("foo/bar#foo"));
		builder = builder.slash("#");
		assertThat(builder.toString(), endsWith("foo/bar#foo"));
	}

	static class SampleLinkBuilder extends LinkBuilderSupport<SampleLinkBuilder> {

		public SampleLinkBuilder(UriComponentsBuilder builder) {
			super(builder);
		}

		@Override
		protected SampleLinkBuilder getThis() {
			return this;
		}

		@Override
		protected SampleLinkBuilder createNewInstance(UriComponentsBuilder builder) {
			return new SampleLinkBuilder(builder);
		}
	}
}
