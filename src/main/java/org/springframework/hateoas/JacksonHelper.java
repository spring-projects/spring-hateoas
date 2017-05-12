/*
 * Copyright 2017 the original author or authors.
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

import com.fasterxml.jackson.databind.JavaType;

/**
 * Utilities to help with interacting with Jackson.
 * 
 * @author Greg Turnquist
 */
public final class JacksonHelper {

	public static JavaType findParentType(JavaType contentType) {

		if (contentType.hasGenericTypes()) {
			return contentType.containedType(0);
		} else {
			return contentType;
		}
	}
}
