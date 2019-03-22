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

import org.springframework.util.ClassUtils;

/**
 * Utility to glean what web stack is currently available.
 * 
 * @author Greg Turnquist
 */
public enum WebStack {

	WEBMVC("org.springframework.web.servlet.DispatcherServlet"),

	WEBFLUX("org.springframework.web.reactive.DispatcherHandler");

	private final boolean isAvailable;

	/**
	 * Initialize the {@link #isAvailable} based upon a defined signature class.
	 */
	WebStack(String signatureClass) {
		this.isAvailable = ClassUtils.isPresent(signatureClass, null);
	}

	/**
	 * Is this web stack on the classpath?
	 */
	public boolean isAvailable() {
		return this.isAvailable;
	}
}
