/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.support;

import lombok.RequiredArgsConstructor;

import org.assertj.core.matcher.AssertionMatcher;
import org.hamcrest.Matcher;
import org.springframework.lang.Nullable;
import org.springframework.test.util.JsonPathExpectationsHelper;

/**
 * @author Greg Turnquist
 */
public class JsonPathUtils {

	public static <T> AssertionMatcher<String> jsonPath(String expression, Matcher<T> matcher) {

		return new AssertionMatcher<String>() {
			@Override
			public void assertion(String actual) throws AssertionError {
				new JsonPathExpectationsHelper(expression).assertValue(actual, matcher);
			}
		};
	}

	public static AssertionMatcher<String> jsonPath(String expression, @Nullable Object expectedValue) {
		return new JsonPathAssertionMatcher(expression, expectedValue);
	}

	@RequiredArgsConstructor
	public static class JsonPathAssertionMatcher extends AssertionMatcher<String> {

		private final String expression;
		private final @Nullable Object expected;

		/*
		 * (non-Javadoc)
		 * @see org.assertj.core.matcher.AssertionMatcher#assertion(java.lang.Object)
		 */
		@Override
		public void assertion(String actual) throws AssertionError {

			JsonPathExpectationsHelper helper = new JsonPathExpectationsHelper(expression);

			if (expected == null) {
				helper.doesNotHaveJsonPath(actual);
			} else {
				helper.assertValue(actual, expected);
			}
		}

		public JsonPathAssertionMatcher doesNotExist() {
			return new JsonPathAssertionMatcher(this.expression, null);
		}
	}

	public static JsonPathAssertionMatcher jsonPath(String expression) {
		return new JsonPathAssertionMatcher(expression, null);
	}
}
