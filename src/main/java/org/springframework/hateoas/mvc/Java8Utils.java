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
package org.springframework.hateoas.mvc;

import java.util.Optional;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Utilities to reflectively work with JDK 8's {@link Optional} on JDK 6.
 * 
 * @author Oliver Gierke
 */
final class Java8Utils {

	private static boolean OPTIONAL_PRESENT = ClassUtils.isPresent("java.util.Optional",
			OptionalValueAccessor.class.getClassLoader());

	/**
	 * Returns whether the given type is the JDK 8's {@link Optional}.
	 * 
	 * @param type must not be {@literal null}.
	 * @return
	 */
	static boolean isJava8Optional(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");

		return OPTIONAL_PRESENT && OptionalValueAccessor.isOptional(type);
	}

	/**
	 * Returns whether the given source value is a JDK 8 {@link Optional}.
	 * 
	 * @param source can be {@literal null}.
	 * @return
	 */
	static boolean isJava8Optional(Object source) {
		return OPTIONAL_PRESENT && OptionalValueAccessor.isOptional(source);
	}

	/**
	 * Unwraps the value contained in a JDK 8 {@link Optional} if the value is one.
	 * 
	 * @param source can be {@literal null}.
	 * @return
	 */
	static Object unwrapJava8Optional(Object source) {
		return OPTIONAL_PRESENT && isJava8Optional(source) ? OptionalValueAccessor.unwrapOptional(source) : source;
	}

	private static class OptionalValueAccessor {

		static boolean isOptional(Class<?> type) {
			return Optional.class.isAssignableFrom(type);
		}

		static boolean isOptional(Object source) {
			return Optional.class.isInstance(source);
		}

		static Object unwrapOptional(Object source) {
			return ((Optional<?>) source).orElse(null);
		}
	}
}
