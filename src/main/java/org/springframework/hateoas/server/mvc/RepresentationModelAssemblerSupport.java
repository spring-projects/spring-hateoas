/*
 * Copyright 2012-2020 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.util.Assert;

/**
 * Base class to implement {@link RepresentationModelAssembler}s. Will automate {@link RepresentationModel} instance
 * creation and make sure a self-link is always added.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public abstract class RepresentationModelAssemblerSupport<T, D extends RepresentationModel<?>>
		implements RepresentationModelAssembler<T, D> {

	private final Class<?> controllerClass;
	private final Class<D> resourceType;

	/**
	 * Creates a new {@link RepresentationModelAssemblerSupport} using the given controller class and resource type.
	 *
	 * @param controllerClass must not be {@literal null}.
	 * @param resourceType must not be {@literal null}.
	 */
	public RepresentationModelAssemblerSupport(Class<?> controllerClass, Class<D> resourceType) {

		Assert.notNull(controllerClass, "ControllerClass must not be null!");
		Assert.notNull(resourceType, "ResourceType must not be null!");

		this.controllerClass = controllerClass;
		this.resourceType = resourceType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.RepresentationModelAssembler#toCollectionModel(java.lang.Iterable)
	 */
	@Override
	public CollectionModel<D> toCollectionModel(Iterable<? extends T> entities) {
		return this.map(entities).toResources();
	}

	/**
	 * Maps the given {@link Iterable} of entities to either a {@link CollectionModel} or simple List of
	 * {@link RepresentationModel}s.
	 *
	 * @param entities must not be {@literal null}.
	 * @return
	 */
	public Builder<T, D> map(Iterable<? extends T> entities) {

		Assert.notNull(entities, "Entities must not be null!");

		return new Builder<>(entities, this);
	}

	/**
	 * Creates a new resource with a self link to the given id.
	 *
	 * @param entity must not be {@literal null}.
	 * @param id must not be {@literal null}.
	 * @return
	 */
	protected D createModelWithId(Object id, T entity) {
		return createModelWithId(id, entity, new Object[0]);
	}

	protected D createModelWithId(Object id, T entity, Object... parameters) {

		Assert.notNull(entity, "Entity must not be null!");
		Assert.notNull(id, "Id must not be null!");

		D instance = instantiateModel(entity);
		instance.add(linkTo(this.controllerClass, parameters).slash(id).withSelfRel());
		return instance;
	}

	protected Class<?> getControllerClass() {
		return this.controllerClass;
	}

	protected Class<D> getResourceType() {
		return this.resourceType;
	}

	/**
	 * Instantiates the resource object. Default implementation will assume a no-arg constructor and use reflection but
	 * can be overridden to manually set up the object instance initially (e.g. to improve performance if this becomes an
	 * issue).
	 *
	 * @param entity
	 * @return
	 */
	protected D instantiateModel(T entity) {
		return BeanUtils.instantiateClass(this.resourceType);
	}

	/**
	 * Intermediate type to allow the creation of either a {@link CollectionModel} or List of
	 * {@link RepresentationModel}s.
	 *
	 * @author Greg Turnquist
	 * @author Oliver Drotbohm
	 */
	static class Builder<T, D extends RepresentationModel<?>> {

		private final Iterable<? extends T> entities;
		private final RepresentationModelAssemblerSupport<T, D> resourceAssembler;

		Builder(Iterable<? extends T> entities, RepresentationModelAssemblerSupport<T, D> resourceAssembler) {

			this.entities = Objects.requireNonNull(entities, "entities must not null!");
			this.resourceAssembler = resourceAssembler;
		}

		/**
		 * Transform a list of {@code T}s into a list of {@link RepresentationModel}s.
		 *
		 * @see #toListOfResources() if you need this transformed list rendered as hypermedia
		 * @return
		 */
		public List<D> toListOfResources() {

			List<D> result = new ArrayList<>();

			for (T entity : this.entities) {
				result.add(this.resourceAssembler.toModel(entity));
			}

			return result;
		}

		/**
		 * Converts all given entities into resources and wraps the result in a {@link CollectionModel} instance.
		 *
		 * @see #toListOfResources() and {@link RepresentationModelAssembler#toModel(Object)}
		 * @return
		 */
		public CollectionModel<D> toResources() {
			return CollectionModel.of(toListOfResources());
		}
	}
}
