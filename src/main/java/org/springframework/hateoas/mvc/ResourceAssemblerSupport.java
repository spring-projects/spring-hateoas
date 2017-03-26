/*
 * Copyright 2012-2013 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.util.Assert;

/**
 * Base class to implement {@link ResourceAssembler}s. Will automate {@link ResourceSupport} instance creation and make
 * sure a self-link is always added.
 * 
 * @author Oliver Gierke
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

	/**
	 * Converts all given entities into resources.
	 * 
	 * @see #toResource(Object)
	 * @param entities must not be {@literal null}.
	 * @return
	 */
	public List<D> toResources(Iterable<? extends T> entities) {

		Assert.notNull(entities, "Entities must not be null!");
		List<D> result = new ArrayList<D>();

		for (T entity : entities) {
			result.add(toResource(entity));
		}

		return result;
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
		instance.add(linkTo(controllerClass, parameters).slash(id).withSelfRel());
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
		return BeanUtils.instantiateClass(resourceType);
	}
}
