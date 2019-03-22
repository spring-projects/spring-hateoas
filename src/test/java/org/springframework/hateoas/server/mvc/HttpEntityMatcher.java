/*
 * Copyright 2012-2016 the original author or authors.
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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

/**
 * @author Jon Brisbin
 * @author Oliver Gierke
 */
@RequiredArgsConstructor(staticName = "httpEntity")
class HttpEntityMatcher<T> extends BaseMatcher<HttpEntity<T>> {

	private final @NonNull HttpEntity<T> expected;

	/* 
	 * (non-Javadoc)
	 * @see org.hamcrest.Matcher#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object item) {

		if (!(item instanceof HttpEntity)) {
			return false;
		}

		if (item instanceof ResponseEntity && expected instanceof ResponseEntity) {

			ResponseEntity<?> left = (ResponseEntity<?>) expected;
			ResponseEntity<?> right = (ResponseEntity<?>) item;

			if (!left.getStatusCode().equals(right.getStatusCode())) {
				return false;
			}
		}

		HttpEntity<?> left = expected;
		HttpEntity<?> right = (HttpEntity<?>) item;

		return left.getBody().equals(right.getBody()) && left.getHeaders().equals(right.getHeaders());
	}

	/*
	 * (non-Javadoc)
	 * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
	 */
	@Override
	public void describeTo(Description description) {
		description.appendText(expected.toString());
	}
}
