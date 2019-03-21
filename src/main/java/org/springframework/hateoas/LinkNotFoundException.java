/*
 * Copyright 2017 the original author or authors.
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

/**
 * @author Greg Turnquist
 */
public class LinkNotFoundException extends RuntimeException {

	private final String rel;
	private final String responseBody;

	public LinkNotFoundException(String rel, String responseBody) {

		this.rel = rel;
		this.responseBody = responseBody;
	}

	/**
	 * Returns the detail message string of this throwable.
	 *
	 * @return the detail message string of this {@code Throwable} instance
	 * (which may be {@code null}).
	 */
	@Override
	public String getMessage() {
		return String.format("Expected to find link with rel '%s' in response %s!", this.rel, this.responseBody);
	}

	public String getRel() {
		return rel;
	}

	public String getResponseBody() {
		return responseBody;
	}
}
