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
package org.springframework.hateoas.server;

import java.util.Map;

/**
 * Factory for {@link LinkBuilder} instances.
 * 
 * @author Ricardo Gladwell
 * @author Andrew Naydyonock
 * @author Oliver Gierke
 */
public interface LinkBuilderFactory<T extends LinkBuilder> {

	/**
	 * Creates a new {@link LinkBuilder} with a base of the mapping annotated to the given target class (controller,
	 * service, etc.).
	 * 
	 * @param target must not be {@literal null}.
	 * @return
	 */
	T linkTo(Class<?> target);

	/**
	 * Creates a new {@link LinkBuilder} with a base of the mapping annotated to the given target class (controller,
	 * service, etc.). The additional parameters are used to fill up potentially available path variables in the class
	 * scope request mapping.
	 * 
	 * @param target must not be {@literal null}.
	 * @param parameters must not be {@literal null}.
	 * @return
	 */
	T linkTo(Class<?> target, Object... parameters);

	/**
	 * Creates a new {@link LinkBuilder} with a base of the mapping annotated to the given target class (controller,
	 * service, etc.). Parameter map is used to fill up potentially available path variables in the class scope request
	 * mapping.
	 *
	 * @param target must not be {@literal null}.
	 * @param parameters must not be {@literal null}.
	 * @return
	 */
	T linkTo(Class<?> target, Map<String, ?> parameters);
}
