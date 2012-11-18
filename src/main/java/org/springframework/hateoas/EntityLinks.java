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

import org.springframework.plugin.core.Plugin;

/**
 * Accessor to links pointing to controllers backing an entity type.
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
	 */
	LinkBuilder linkFor(Class<?> type);

	/**
	 * Returns a {@link LinkBuilder} able to create links to the controller managing the given entity type, unfolding the
	 * given parameters into the URI template the backing controller is mapped to.
	 * 
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @return the {@link LinkBuilder} pointing to the collection resource. Will never be {@literal null}.
	 */
	LinkBuilder linkFor(Class<?> type, Object... parameters);

	/**
	 * Returns a {@link LinkBuilder} able to create links to the controller managing the given entity type and id.
	 * Implementations will know about the URI structure being used to expose single-resource URIs.
	 * 
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @param id the id of the object of the handed type, {@link Identifiable}s will be unwrapped.
	 * @return
	 */
	LinkBuilder linkForSingleResource(Class<?> type, Object id);

	/**
	 * Returns a {@link LinkBuilder} able to create links to the controller managing the given entity.
	 * 
	 * @see #linkForSingleResource(Class, Object)
	 * @param entity the entity type to point to, must not be {@literal null}.
	 * @return
	 */
	LinkBuilder linkForSingleResource(Identifiable<?> entity);

	/**
	 * Creates a {@link Link} pointing to the collection resource of the given type. The relation type of the link will be
	 * determined by the implementation class and should be defaulted to {@link Link#REL_SELF}.
	 * 
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @return
	 */
	Link linkToCollectionResource(Class<?> type);

	/**
	 * Creates a {@link Link} pointing to single resource backing the given entity type and id. The relation type of the
	 * link will be determined by the implementation class and should be defaulted to {@link Link#REL_SELF}.
	 * 
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @param id
	 * @return
	 */
	Link linkToSingleResource(Class<?> type, Object id);

	/**
	 * Creates a {@link Link} pointing to single resource backing the given entity. The relation type of the link will be
	 * determined by the implementation class and should be defaulted to {@link Link#REL_SELF}.
	 * 
	 * @param entity the entity type to point to, must not be {@literal null}.
	 * @return
	 */
	Link linkToSingleResource(Identifiable<?> entity);
}
