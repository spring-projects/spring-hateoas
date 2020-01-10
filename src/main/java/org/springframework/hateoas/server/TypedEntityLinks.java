/*
 * Copyright 2019-2020 the original author or authors.
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

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.util.Assert;

/**
 * Entity links API to create {@link Link}s and {@link LinkBuilder} instances based on an identifier function.
 *
 * @author Oliver Drotbohm
 * @see EntityLinks#forType(Function)
 * @see EntityLinks#forType(Class, Function)
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TypedEntityLinks<T> {

	private final @NonNull Function<T, ?> identifierExtractor;
	private final @NonNull EntityLinks entityLinks;

	/**
	 * Returns a {@link LinkBuilder} able to create links to the controller managing the given entity. Implementations
	 * will know about the URI structure being used to expose item-resource URIs.
	 *
	 * @param type the entity to point to, must not be {@literal null}.
	 * @return the {@link LinkBuilder} pointing to the item resource identified by the given entity. Will never be
	 *         {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	public LinkBuilder linkForItemResource(T entity) {
		return entityLinks.linkForItemResource(entity.getClass(), identifierExtractor.apply(entity));
	}

	/**
	 * Creates a {@link Link} pointing to item resource backing the given entity. The relation type of the link will be
	 * determined by the implementation class and should be defaulted to {@link IanaLinkRelations#SELF}.
	 *
	 * @param type the entity to point to, must not be {@literal null}.
	 * @return the {@link Link} pointing to the resource exposed for the given entity. Will never be {@literal null}.
	 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
	 */
	public Link linkToItemResource(T entity) {
		return entityLinks.linkToItemResource(entity.getClass(), identifierExtractor.apply(entity));
	}

	/**
	 * Extension of {@link TypedEntityLinks} that exposes the ability to create links to collection resources as well.
	 *
	 * @author Oliver Drotbohm
	 */
	public static class ExtendedTypedEntityLinks<T> extends TypedEntityLinks<T> {

		private final Class<T> type;
		private final EntityLinks delegate;

		ExtendedTypedEntityLinks(Function<T, ?> identifierExtractor, EntityLinks delegate, Class<T> type) {

			super(identifierExtractor, delegate);

			Assert.notNull(type, "Type must not be null!");

			this.type = type;
			this.delegate = delegate;
		}

		/**
		 * Creates a {@link Link} pointing to the collection resource of the configured type. The relation type of the link
		 * will be determined by the implementation class and should be defaulted to {@link IanaLinkRelations#SELF}.
		 *
		 * @param type the entity type to point to, must not be {@literal null}.
		 * @return the {@link Link} pointing to the collection resource exposed for the configured entity type. Will never
		 *         be {@literal null}.
		 * @throws IllegalArgumentException in case the given type is unknown the entity links infrastructure.
		 */
		public Link linkToCollectionResource() {
			return delegate.linkToCollectionResource(type);
		}
	}
}
