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
package org.springframework.hateoas.server;

import java.util.function.Function;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.TypedEntityLinks.ExtendedTypedEntityLinks;
import org.springframework.plugin.core.Plugin;
import org.springframework.util.Assert;

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
	 * Implementations will know about the URI structure being used to expose item-resource URIs.
	 *
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @param id the id of the object of the handed type, must not be {@literal null}.
	 * @return the {@link LinkBuilder} pointing to the item resource identified by the given type and id. Will never be
	 *         {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	LinkBuilder linkForItemResource(Class<?> type, Object id);

	/**
	 * Returns a {@link LinkBuilder} able to create links to the controller managing the given entity type and identifier
	 * extractor. Implementations will know about the URI structure being used to expose item-resource URIs.
	 *
	 * @param type the entity to point to, must not be {@literal null}.
	 * @param identifierExtractor an extractor function to determine the id of the given entity, must not be
	 *          {@literal null}.
	 * @return the {@link LinkBuilder} pointing to the item resource identified by the given entity. Will never be
	 *         {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	default <T> LinkBuilder linkForItemResource(T entity, Function<T, Object> identifierExtractor) {

		Assert.notNull(identifierExtractor, "Identifier extractor must not be null!");

		return linkForItemResource(entity.getClass(), identifierExtractor.apply(entity));
	}

	/**
	 * Creates a {@link Link} pointing to the collection resource of the given type. The relation type of the link will be
	 * determined by the implementation class and should be defaulted to {@link IanaLinkRelations#SELF}.
	 *
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @return the {@link Link} pointing to the collection resource exposed for the given entity. Will never be
	 *         {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	Link linkToCollectionResource(Class<?> type);

	/**
	 * Creates a {@link Link} pointing to item resource backing the given entity type and id. The relation type of the
	 * link will be determined by the implementation class and should be defaulted to {@link IanaLinkRelations#SELF}.
	 *
	 * @param type the entity type to point to, must not be {@literal null}.
	 * @param id the identifier of the entity of the given type
	 * @return the {@link Link} pointing to the resource exposed for the entity with the given type and id. Will never be
	 *         {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	Link linkToItemResource(Class<?> type, Object id);

	/**
	 * Creates a {@link Link} pointing to item resource backing the given entity and identifier extractor. The relation
	 * type of the link will be determined by the implementation class and should be defaulted to
	 * {@link IanaLinkRelations#SELF}.
	 *
	 * @param type the entity to point to, must not be {@literal null}.
	 * @param identifierExtractor an extractor function to determine the id of the given entity.
	 * @return the {@link Link} pointing to the resource exposed for the given entity. Will never be {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	default <T> Link linkToItemResource(T entity, Function<T, Object> identifierExtractor) {
		return linkToItemResource(entity.getClass(), identifierExtractor.apply(entity));
	}

	/**
	 * Creates a {@link TypedEntityLinks} instance using the given identifier extractor function.
	 *
	 * @param <T> the type of entity to be handled.
	 * @param extractor the extractor to use to derive an identifier from the given entity.
	 * @return
	 */
	default <T> TypedEntityLinks<T> forType(Function<T, ?> extractor) {
		return new TypedEntityLinks<>(extractor, this);
	}

	/**
	 * Creates a {@link TypedEntityLinks} instance using the given type and identifier extractor function.
	 *
	 * @param <T> the type of entity to be handled.
	 * @param type the type of entity.
	 * @param extractor the extractor to use to derive an identifier from the given entity.
	 * @return
	 */
	default <T> ExtendedTypedEntityLinks<T> forType(Class<T> type, Function<T, Object> extractor) {
		return new ExtendedTypedEntityLinks<>(extractor, this, type);
	}
}
