/*
 * Copyright 2012-2014 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.mvc.ControllerLinkBuilderFactory;
import org.springframework.util.Assert;

/**
 * Base class to implement {@link ResourceAssembler}s. Will automate {@link ResourceSupport} instance creation and make
 * sure a self-link is always added.
 *
 * @author Oliver Gierke
 */
public abstract class ResourceAssemblerSupport<T, D extends ResourceSupport> implements ResourceAssembler<T, D> {

	private static final ControllerLinkBuilderFactory DEFAULT_FACTORY = new ControllerLinkBuilderFactory();

	protected final LinkBuilderFactory<? extends LinkBuilder> linkBuilderFactory;
	private final Class<?> controllerClass;
	private final Class<D> resourceType;

	/**
	 * Creates a new {@link ResourceAssemblerSupport} using the given controller class and resource type.
	 * A {@link ControllerLinkBuilderFactory} will be used for link creation.
	 *
	 * @param controllerClass must not be {@literal null}.
	 * @param resourceType must not be {@literal null}.
	 */
	public ResourceAssemblerSupport(Class<?> controllerClass, Class<D> resourceType) {

		this(DEFAULT_FACTORY, controllerClass, resourceType);
	}

	/**
	 * Creates a new {@link ResourceAssemblerSupport} using the given link builder factory, controller class and resource type.
	 *
	 * @param linkBuilderFactory must not be {@literal null}.
	 * @param controllerClass must not be {@literal null}.
	 * @param resourceType must not be {@literal null}.
	 */
	public ResourceAssemblerSupport(LinkBuilderFactory<? extends LinkBuilder> linkBuilderFactory, Class<?> controllerClass, Class<D> resourceType) {

		Assert.notNull(linkBuilderFactory);
		Assert.notNull(controllerClass);
		Assert.notNull(resourceType);

		this.linkBuilderFactory = linkBuilderFactory;
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

		Assert.notNull(entities);
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

		Assert.notNull(entity);
		Assert.notNull(id);

		D instance = instantiateResource(entity);
		instance.add(linkBuilderFactory.linkTo(controllerClass, parameters).slash(id).withSelfRel());
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
