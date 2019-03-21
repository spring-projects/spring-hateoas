/*
 * Copyright 2012-2018 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for components that convert a domain type into a {@link ResourceSupport}.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public interface ResourceAssembler<T, D extends ResourceSupport> {

	/**
	 * Converts the given entity into a {@code D}, which extends {@link ResourceSupport}.
	 * 
	 * @param entity
	 * @return
	 */
	D toResource(T entity);

	/**
	 * Converts an {@link Iterable} or {@code T}s into an {@link Iterable} of {@link ResourceSupport} and wraps
	 * them in a {@link Resources} instance.
	 * 
	 * @param entities must not be {@literal null}.
	 * @return {@link Resources} containing {@code D}.
	 */
	default Resources<D> toResources(Iterable<? extends T> entities) {
		
		List<D> resources = new ArrayList<>();
		for (T entity : entities) {
			resources.add(toResource(entity));
		}
		return new Resources<>(resources);
	}
}
