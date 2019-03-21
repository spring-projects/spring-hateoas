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
package org.springframework.hateoas.mvc;

import static org.springframework.hateoas.mvc.WebMvcLinkBuilder.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.util.Assert;

/**
 * Base class to implement {@link ResourceAssembler}s. Will automate {@link ResourceSupport} instance creation and make
 * sure a self-link is always added.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public abstract class ResourceAssemblerSupport<T, D extends ResourceSupport> implements ResourceAssembler<T, D> {

	private final Class<?> controllerClass;
	private final Class<D> resourceType;

	/**
	 * Creates a new {@link ResourceAssemblerSupport} using the given controller class and resource type.
	 *
	 * @param controllerClass must not be {@literal null}.
	 * @param resourceType must not be {@literal null}.
	 */
	public ResourceAssemblerSupport(Class<?> controllerClass, Class<D> resourceType) {

		Assert.notNull(controllerClass, "ControllerClass must not be null!");
		Assert.notNull(resourceType, "ResourceType must not be null!");

		this.controllerClass = controllerClass;
		this.resourceType = resourceType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceAssembler#toResources(java.lang.Iterable)
	 */
	@Override
	public Resources<D> toResources(Iterable<? extends T> entities) {
		return this.map(entities).toResources();
	}

	public Builder<T, D> map(Iterable<? extends T> entities) {
		return new Builder<>(entities, this);
	}

	/**
	 * Creates a new resource with a self link to the given id.
	 *
	 * @param entity must not be {@literal null}.
	 * @param id must not be {@literal null}.
	 * @return
	 */
	protected D createResourceWithId(Object id, T entity) {
		return createResourceWithId(id, entity, new Object[0]);
	}

	protected D createResourceWithId(Object id, T entity, Object... parameters) {

		Assert.notNull(entity, "Entity must not be null!");
		Assert.notNull(id, "Id must not be null!");

		D instance = instantiateResource(entity);
		instance.add(linkTo(this.controllerClass, parameters).slash(id).withSelfRel());
		return instance;
	}

	/**
	 * Instantiates the resource object. Default implementation will assume a no-arg constructor and use reflection but
	 * can be overridden to manually set up the object instance initially (e.g. to improve performance if this becomes an
	 * issue).
	 *
	 * @param entity
	 * @return
	 */
	protected D instantiateResource(T entity) {
		return BeanUtils.instantiateClass(this.resourceType);
	}

	static class Builder<T, D extends ResourceSupport> {

		private final Iterable<? extends T> entities;
		private final ResourceAssemblerSupport<T, D> resourceAssembler;

		Builder(Iterable<? extends T> entities, ResourceAssemblerSupport<T, D> resourceAssembler) {

			this.entities = Objects.requireNonNull(entities, "entities must not null!");
			this.resourceAssembler = resourceAssembler;
		}

		/**
		 * Transform a list of {@code T}s into a list of {@link ResourceSupport}s.
		 *
		 * @see #toListOfResources() if you need this transformed list rendered as hypermedia
		 * @return
		 */
		public List<D> toListOfResources() {

			List<D> result = new ArrayList<>();

			for (T entity : this.entities) {
				result.add(this.resourceAssembler.toResource(entity));
			}

			return result;
		}

		/**
		 * Converts all given entities into resources and wraps the result in a {@link Resources} instance.
		 *
		 * @see #toListOfResources() and {@link ResourceAssembler#toResource(Object)}
		 * @return
		 */
		public Resources<D> toResources() {
			return new Resources<>(toListOfResources());
		}
	}
}
