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

import org.springframework.hateoas.Resource;

/**
 * Simple helper class.
 * 
 * @author Oliver Gierke
 */
public class ObjectUtils {

	/**
	 * Returns the resource type of the given object. In case a {@link Resource} is given, it will return the
	 * {@link Resource} content's type.
	 * 
	 * @param object can be {@literal null}.
	 * @return
	 */
	public static Class<?> getResourceType(Object object) {

		if (object instanceof Resource) {
			return nullSafeType(((Resource<?>) object).getContent());
		}

		return nullSafeType(object);
	}

	private static Class<?> nullSafeType(Object object) {
		return object == null ? null : object.getClass();
	}
}
