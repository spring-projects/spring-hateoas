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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * Utility class to ease testing.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class TestUtils {

	protected MockHttpServletRequest request;

	@BeforeEach
	public void setUp() {

		request = new MockHttpServletRequest();
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);
	}

	protected void assertPointsToMockServer(Link link) {
		assertThat(link.getHref()).startsWith("http://localhost");
	}

	/**
	 * Provide a mechanism to simulate inserting a {@link ForwardedHeaderFilter} into the servlet filter chain, so
	 * {@literal Forwarded} headers are properly inserted into the test web request.
	 *
	 * @see https://jira.spring.io/browse/SPR-16668
	 */
	protected void adaptRequestFromForwardedHeaders() {

		MockFilterChain chain = new MockFilterChain();
		try {
			new ForwardedHeaderFilter().doFilter(this.request, new MockHttpServletResponse(), chain);
		} catch (ServletException | IOException e) {
			throw new RuntimeException(e);
		}
		HttpServletRequest adaptedRequest = (HttpServletRequest) chain.getRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(adaptedRequest));
	}

	public static void assertEqualAndSameHashCode(Object left, Object right) {

		assertThat(left).isEqualTo(right);
		assertThat(right).isEqualTo(left);
		assertThat(left).isEqualTo(left);
		assertThat(left.hashCode()).isEqualTo(right.hashCode());
		assertThat(left.toString()).isEqualTo(right.toString());
	}

	public static void assertNotEqualAndDifferentHashCode(Object left, Object right) {

		assertThat(left).isNotEqualTo(right);
		assertThat(right).isNotEqualTo(left);
		assertThat(left.hashCode()).isNotEqualTo(right.hashCode());
		assertThat(left.toString()).isNotEqualTo(right.toString());
	}
}
