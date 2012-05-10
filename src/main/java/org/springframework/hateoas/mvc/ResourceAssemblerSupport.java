/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a coimport javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;


eed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.mvc;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.util.Assert;

/**
 * Base class to implement {@link ResourceAssembler}s. Will automate {@link ResourceSupport} instance creation and make
 * sure a self-link is always added.
 * 
 * @author Oliver Gierke
 */
public abstract class ResourceAssemblerSupport<T extends Identifiable<?>, D extends ResourceSupport> implements
		ResourceAssembler<T, D> {

	public static class EntityId {

		private final Object id;

		public EntityId(Object id) {
			Assert.notNull(id);
			this.id = id;
		}

		public static EntityId id(Object object) {
			return new EntityId(object);
		}

		@Override
		public String toString() {
			return this.id.toString();
		}
	}

	private final Class<?> controllerClass;
	private final Class<D> resourceType;

	/**
	 * Creates a new {@link ResourceAssemblerSupport} using the given controller class and resource type.
	 * 
	 * @param controllerClass must not be {@literal null}.
	 * @param resourceType must not be {@literal null}.
	 */
	public ResourceAssemblerSupport(Class<?> controllerClass, Class<D> resourceType) {

		Assert.notNull(controllerClass);
		Assert.notNull(resourceType);

		this.controllerClass = controllerClass;
		this.resourceType = resourceType;
	}

	/**
	 * Converts all given entities into resources.
	 * 
	 * @see #toResource(Object)
	 * @param entities
	 * @return
	 */
	public List<D> toResources(Iterable<? extends T> entities) {

		List<D> result = new ArrayList<D>();

		for (T entity : entities) {
			result.add(toResource(entity));
		}

		return result;
	}

	/**
	 * Creates a new resource and adds a self link to it consisting using the {@link Identifiable}'s id.
	 * 
	 * @param entity must not be {@literal null}.
	 * @return
	 */
	protected D createResource(T entity) {
		return createResource(entity, new Object[0]);
	}

	protected D createResource(T entity, Object... parameters) {
		return createResource(entity, EntityId.id(entity.getId()), parameters);
	}

	/**
	 * Creates a new resource with a self link to the given id.
	 * 
	 * @param entity
	 * @param id
	 * @return
	 */
	protected D createResource(T entity, EntityId id) {
		return createResource(entity, id, new Object[0]);
	}

	protected D createResource(T entity, EntityId id, Object... parameters) {

		Assert.notNull(entity);
		Assert.notNull(id);

		D instance = instantiateResource(entity);
		instance.add(linkTo(controllerClass, unwrapIdentifyables(parameters)).slash(id).withSelfRel());
		return instance;
	}

	/**
	 * Extracts the ids of the given values in case they're {@link Identifiable}s. Returns all other objects as they are.
	 * 
	 * @param values must not be {@literal null}.
	 * @return
	 */
	private Object[] unwrapIdentifyables(Object[] values) {

		List<Object> result = new ArrayList<Object>(values.length);

		for (Object element : Arrays.asList(values)) {
			result.add((element instanceof Identifiable) ? ((Identifiable<?>) element).getId() : element);
		}

		return result.toArray();
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