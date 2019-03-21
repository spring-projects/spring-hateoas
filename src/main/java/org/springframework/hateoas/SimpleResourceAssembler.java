/*
 * Copyright 2018 the original author or authors.
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

import org.springframework.util.Assert;

/**
 * A {@link ResourceAssembler} based purely on the domain type, using {@code Resource<T>} as the enclosing
 * "resource" type.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
public interface SimpleResourceAssembler<T> extends ResourceAssembler<T, Resource<T>> {

	/**
	 * Converts the given entity into a {@link Resource}.
	 *
	 * @param entity
	 * @return
	 */
	default Resource<T> toResource(T entity) {
		
		Resource<T> resource = new Resource<>(entity);
		addLinks(resource);
		return resource;
	}

	/**
	 * Define links to add to every individual {@link Resource}.
	 *
	 * @param resource
	 */
	void addLinks(Resource<T> resource);

	/**
	 * Converts all given entities into resources and wraps the collection as a resource as well.
	 *
	 * @see #toResource(Object)
	 * @param entities must not be {@literal null}.
	 * @return {@link Resources} containing {@link Resource} of {@code T}.
	 */
	@Override
	default Resources<Resource<T>> toResources(Iterable<? extends T> entities) {

		Assert.notNull(entities, "entities must not be null!");
		List<Resource<T>> resourceList = new ArrayList<>();

		for (T entity : entities) {
			resourceList.add(toResource(entity));
		}

		Resources<Resource<T>> resources = new Resources<>(resourceList);
		addLinks(resources);
		return resources;
	}

	/**
	 * Define links to add to the {@link Resources} collection.
	 *
	 * @param resources
	 */
	void addLinks(Resources<Resource<T>> resources);
}
