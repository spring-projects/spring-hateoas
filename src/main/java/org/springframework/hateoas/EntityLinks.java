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
package org.springframework.hateoas;

import org.springframework.plugin.core.Plugin;

/**
 * Accessor to links pointing to controllers backing an entity type. The {@link IllegalArgumentException} potentially
 * thrown by the declared methods will only appear if the {@link #supports(Class)} method has returned {@literal false}
 * and the method has been invoked anyway, i.e. if {@link #supports(Class)} returns {@literal true} it's safe to invoke
 * the interface methods an the exception will never be thrown.
 * 
 * @author Oliver Gierke
 */
public interface EntityLinks extends Plugin<Class<?>> {

	/**
	 * Returns a {@link LinkBuilder} able to create links to the controller managing the given entity type. Expects a
	 * controller being mapped to a fully expanded URI template (i.e. not path variables being used).
	 * 
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @return the {@link LinkBuilder} pointing to the collection resource. Will never be {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	LinkBuilder linkFor(Class<?> type);

	/**
	 * Returns a {@link LinkBuilder} able to create links to the controller managing the given entity type, unfolding the
	 * given parameters into the URI template the backing controller is mapped to.
	 * 
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @return the {@link LinkBuilder} pointing to the collection resource.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	LinkBuilder linkFor(Class<?> type, Object... parameters);

	/**
	 * Returns a {@link LinkBuilder} able to create links to the controller managing the given entity type and id.
	 * Implementations will know about the URI structure being used to expose single-resource URIs.
	 * 
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @param id the id of the object of the handed type, {@link Identifiable}s will be unwrapped.
	 * @return the {@link LinkBuilder} pointing to the single resource identified by the given type and id. Will never be
	 *         {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	LinkBuilder linkForSingleResource(Class<?> type, Object id);

	/**
	 * Returns a {@link LinkBuilder} able to create links to the controller managing the given entity.
	 * 
	 * @see #linkForSingleResource(Class, Object)
	 * @param entity the entity type to point to, must not be {@literal null}.
	 * @return the {@link LinkBuilder} pointing the given entity. Will never be {@literal null}.
	 * @throws IllegalArgumentException in case the type of the given entity is unknown the entity links infrastructure.
	 */
	LinkBuilder linkForSingleResource(Identifiable<?> entity);

	/**
	 * Creates a {@link Link} pointing to the collection resource of the given type. The relation type of the link will be
	 * determined by the implementation class and should be defaulted to {@link Link#REL_SELF}.
	 * 
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @return the {@link Link} pointing to the collection resource exposed for the given entity. Will never be
	 *         {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	Link linkToCollectionResource(Class<?> type);

	/**
	 * Creates a {@link Link} pointing to single resource backing the given entity type and id. The relation type of the
	 * link will be determined by the implementation class and should be defaulted to {@link Link#REL_SELF}.
	 * 
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @param id the identifier of the entity of the given type
	 * @return the {@link Link} pointing to the resource exposed for the entity with the given type and id. Will never be
	 *         {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	Link linkToSingleResource(Class<?> type, Object id);

	/**
	 * Creates a {@link Link} pointing to single resource backing the given entity. The relation type of the link will be
	 * determined by the implementation class and should be defaulted to {@link Link#REL_SELF}.
	 * 
	 * @param entity the entity type to point to, must not be {@literal null}.
	 * @return the {@link Link} pointing to the resource exposed for the given entity. Will never be {@literal null}.
	 * @throws IllegalArgumentException in case the type of the given entity is unknown the entity links infrastructure.
	 */
	Link linkToSingleResource(Identifiable<?> entity);
}
