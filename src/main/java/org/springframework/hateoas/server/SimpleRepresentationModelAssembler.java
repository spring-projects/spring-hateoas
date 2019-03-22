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
package org.springframework.hateoas.server;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.util.Assert;

/**
 * A {@link RepresentationModelAssembler} based purely on the domain type, using {@code EntityRepresentationModel<T>} as
 * the enclosing representation model type.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
public interface SimpleRepresentationModelAssembler<T>
		extends RepresentationModelAssembler<T, EntityModel<T>> {

	/**
	 * Converts the given entity into a {@link EntityModel}.
	 *
	 * @param entity
	 * @return
	 */
	default EntityModel<T> toModel(T entity) {

		EntityModel<T> resource = new EntityModel<>(entity);
		addLinks(resource);
		return resource;
	}

	/**
	 * Define links to add to every individual {@link EntityModel}.
	 *
	 * @param resource
	 */
	void addLinks(EntityModel<T> resource);

	/**
	 * Converts all given entities into resources and wraps the collection as a resource as well.
	 *
	 * @see #toModel(Object)
	 * @param entities must not be {@literal null}.
	 * @return {@link CollectionModel} containing {@link EntityModel} of {@code T}.
	 */
	@Override
	default CollectionModel<EntityModel<T>> toCollectionModel(
			Iterable<? extends T> entities) {

		Assert.notNull(entities, "entities must not be null!");
		List<EntityModel<T>> resourceList = new ArrayList<>();

		for (T entity : entities) {
			resourceList.add(toModel(entity));
		}

		CollectionModel<EntityModel<T>> resources = new CollectionModel<>(
				resourceList);
		addLinks(resources);
		return resources;
	}

	/**
	 * Define links to add to the {@link CollectionModel} collection.
	 *
	 * @param resources
	 */
	void addLinks(CollectionModel<EntityModel<T>> resources);
}
