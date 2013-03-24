/*
 * Copyright 2012-2013 the original author or authors.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class to ease tesing.
 * 
 * @author Oliver Gierke
 */
public class TestUtils {

	protected MockHttpServletRequest request;

	@Before
	public void setUp() {

		request = new MockHttpServletRequest();
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);
	}

	protected void assertPointsToMockServer(Link link) {
		assertThat(link.getHref(), startsWith("http://localhost"));
	}

	public static void assertEqualAndSameHashCode(Object left, Object right) {

		assertThat(left, is(right));
		assertThat(right, is(left));
		assertThat(left, is(left));
		assertThat(left.hashCode(), is(right.hashCode()));
		assertThat(left.toString(), is(right.toString()));
	}

	public static void assertNotEqualAndDifferentHashCode(Object left, Object right) {

		assertThat(left, is(not(right)));
		assertThat(right, is(not(left)));
		assertThat(left.hashCode(), is(not(right.hashCode())));
		assertThat(left.toString(), is(not(right.toString())));
	}
}
