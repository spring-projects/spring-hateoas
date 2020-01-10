/*
 * Copyright 2013-2020 the original author or authors.
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.LinkRelationProvider.LookupContext;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.lang.Nullable;
import org.springframework.plugin.core.Plugin;

/**
 * API to provide {@link LinkRelation}s for collections and items of the given type. Implementations can be selected
 * based on the {@link LookupContext}, for item resource relations, collection resource relations or both.
 *
 * @author Oliver Gierke
 * @see #supports(LookupContext)
 */
public interface LinkRelationProvider extends Plugin<LookupContext> {

	/**
	 * Returns the relation type to be used to point to an item resource of the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @return
	 */
	LinkRelation getItemResourceRelFor(Class<?> type);

	/**
	 * Returns the relation type to be used to point to a collection resource of the given type.
	 *
	 * @param type must not be {@literal null}.
	 * @return
	 */
	LinkRelation getCollectionResourceRelFor(Class<?> type);

	/**
	 * Callback method to manually select {@link LinkRelationProvider} implementations based on a given
	 * {@link LookupContext}. User code shouldn't need to call this method explicitly but rather use
	 * {@link DelegatingLinkRelationProvider}, equip that with a set of {@link LinkRelationProvider} implementations as
	 * that will perform the selection of the matching one on invocations of {@link #getItemResourceRelFor(Class)} and
	 * {@link #getCollectionResourceRelFor(Class)} transparently.
	 *
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	boolean supports(LookupContext delimiter);

	/**
	 * {@link LinkRelationProvider} selection context for item resource relation lookups
	 * ({@link #forItemResourceRelLookup(Class)}, collection resource relation lookups
	 * {@link #forCollectionResourceRelLookup(Class)} or both {@link #forType(Class)}.
	 *
	 * @author Oliver Drotbohm
	 */
	@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
	@EqualsAndHashCode
	class LookupContext {

		private enum ResourceType {
			ITEM, COLLECTION
		}

		private final @NonNull @Getter Class<?> type;
		private final @Nullable ResourceType resourceType;

		/**
		 * Creates a {@link LookupContext} for the type in general, i.e. both item and collection relation lookups.
		 *
		 * @param type must not be {@literal null}.
		 * @return
		 */
		public static LookupContext forType(Class<?> type) {
			return new LookupContext(type, null);
		}

		/**
		 * Creates a {@link LookupContext} to lookup the item resource relation for the given type.
		 *
		 * @param type must not be {@literal null}.
		 * @return
		 */
		public static LookupContext forItemResourceRelLookup(Class<?> type) {
			return new LookupContext(type, ResourceType.ITEM);
		}

		/**
		 * Creates a {@link LookupContext} to lookup the collection resource relation for the given type.
		 *
		 * @param type must not be {@literal null}.
		 * @return
		 */
		public static LookupContext forCollectionResourceRelLookup(Class<?> type) {
			return new LookupContext(type, ResourceType.COLLECTION);
		}

		/**
		 * Returns whether the current context includes the item relation lookup.
		 *
		 * @return
		 */
		public boolean isItemRelationLookup() {
			return resourceType == null || ResourceType.ITEM.equals(resourceType);
		}

		/**
		 * Returns whether the current context includes the collection relation lookup.
		 *
		 * @return
		 */
		public boolean isCollectionRelationLookup() {
			return resourceType == null || ResourceType.COLLECTION.equals(resourceType);
		}

		/**
		 * Returns whether the lookup is executed for the given type.
		 *
		 * @param type must not be {@literal null}.
		 * @return
		 */
		public boolean handlesType(Class<?> type) {
			return this.type.equals(type);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {

			ResourceType resourceType = this.resourceType;

			return String.format("LookupContext for %s for %s resource relations.", type.getName(),
					resourceType == null ? "ITEM & COLLECTION" : resourceType.name());
		}
	}
}
