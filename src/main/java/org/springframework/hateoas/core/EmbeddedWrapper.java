/*
 * Copyright 2014 the original author or authors.
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
 * A wrapper to handle values to be embedded into a {@link Resource}.
 * 
 * @author Oliver Gierke
 */
public interface EmbeddedWrapper {

	/**
	 * Returns the rel to be used when embedding. If this returns {@literal null}, the rel will be calculated based on the
	 * type of the embedded value.
	 * 
	 * @return
	 */
	String getRel();

	/**
	 * Returns whether the wrapper hast the given rel.
	 * 
	 * @param rel can be {@literal null}.
	 * @return
	 */
	boolean hasRel(String rel);

	/**
	 * Returns whether the wrapper is a collection value.
	 * 
	 * @return
	 */
	boolean isCollectionValue();

	/**
	 * Returns the actual value to embed.
	 * 
	 * @return
	 */
	Object getValue();

	/**
	 * Returns the type to be used to calculate a type based rel.
	 * 
	 * @return
	 */
	Class<?> getRelTargetType();
}
