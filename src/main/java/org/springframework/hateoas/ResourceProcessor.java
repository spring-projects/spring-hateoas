/*
 * Copyright 2012 the original author or authors.
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


/**
 * SPI to let user code manipulate a {@link Resource} by the framework.
 * 
 * @param T the concrete Resource<T> extension
 * @author Oliver Gierke
 */
public interface ResourceProcessor<T extends Resource<?>> {

	/**
	 * Callback to allow the manipulation of the given {@link Resource} before rendering.
	 * 
	 * @param resource the standard resource prepared by the framework.
	 * @param entity the entity backing the resource
	 * @return a {@link Resource} object (could be a completely different one)
	 */
	Resource<?> process(T resource);
}
