/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.hateoas;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.hateoas.server.core.EmbeddedWrappers;

/**
 * Builder for hypermedia representations.
 *
 * @author Greg Turnquist
 * @since 1.1
 */
public interface Model {

	/**
	 * Helper method to create a basic {@link Builder} that will support adding entities and {@link Link}s.
	 *
	 * @return
	 */
	static Model.Builder builder() {
		return new DefaultModelBuilder();
	}

	/**
	 * Helper method to create a {@link HalModelBuilder} that supports basic and embedded operations.
	 *
	 * @return
	 */
	static Model.HalModelBuilder hal() {
		return new HalModelBuilder();
	}

	/**
	 * The contract for any hypermedia representation builder.
	 *
	 * @author Greg Turnquist
	 * @since 1.1
	 */
	interface Builder {

		/**
		 * Add an entity to the representation.
		 *
		 * @param entity
		 * @return
		 */
		Builder entity(Object entity);

		/**
		 * Add a {@link Link} to the representation.
		 *
		 * @param link
		 * @return
		 */
		Builder link(Link link);

		/**
		 * Transform the collected details into a {@link RepresentationModel}.
		 *
		 * @return
		 */
		RepresentationModel<?> build();
	}

	/**
	 * Default {@link Builder} that assembles simple hypermedia representations with a list of entities and a {@link List}
	 * of {@link Link}s.
	 * <p/>
	 * The {@link RepresentationModel} that gets built should work with any hypermedia type.
	 *
	 * @author Greg Turnquist
	 * @since 1.1
	 */
	final class DefaultModelBuilder implements Builder {

		private final List<Object> entities = new ArrayList<>();
		private final List<Link> links = new ArrayList<>();

		/**
		 * Add an entity. Can be anything, whether a bare domain object or some {@link RepresentationModel}.
		 *
		 * @param entity
		 * @return
		 */
		@Override
		public Builder entity(Object entity) {

			this.entities.add(entity);
			return this;
		}

		/**
		 * Add a {@link Link}.
		 *
		 * @param link
		 * @return
		 */
		@Override
		public Builder link(Link link) {

			this.links.add(link);
			return this;
		}

		/**
		 * Transform the entities and {@link Link}s into a {@link RepresentationModel} with no preferred media type format.
		 *
		 * @return
		 */
		@Override
		public RepresentationModel<?> build() {

			if (this.entities.isEmpty()) {

				return new RepresentationModel<>(this.links);

			} else if (this.entities.size() == 1) {

				Object content = this.entities.get(0);

				if (RepresentationModel.class.isInstance(content)) {
					return (RepresentationModel<?>) content;
				} else {
					return EntityModel.of(content, this.links);
				}

			} else {

				return CollectionModel.of(this.entities, this.links);
			}
		}
	}

	/**
	 * HAL-specific {@link Builder} that assembles a potentially more complex hypermedia representation.
	 * <p/>
	 * The {@link RepresentationModel} that is built, if it has embedded entries, will contain a preferred hypermedia
	 * representation of {@link MediaTypes#HAL_JSON} or {@link MediaTypes#HAL_FORMS_JSON}.
	 *
	 * @author Greg Turnquist
	 * @since 1.1
	 */
	final class HalModelBuilder implements Builder {

		private static final LinkRelation NO_RELATION = LinkRelation.of("___norel___");

		private final Map<LinkRelation, List<Object>> entityModels = new LinkedHashMap<>(); // maintain the original order
		private final List<Link> links = new ArrayList<>();

		/**
		 * Embed the entity, but with no relation.
		 *
		 * @param entity
		 * @return
		 */
		@Override
		public HalModelBuilder entity(Object entity) {
			return embed(NO_RELATION, entity);
		}

		/**
		 * Embed the entity and associate it with the {@link LinkRelation}.
		 *
		 * @param linkRelation
		 * @param entity
		 * @return
		 */
		public HalModelBuilder embed(LinkRelation linkRelation, Object entity) {

			this.entityModels.computeIfAbsent(linkRelation, r -> new ArrayList<>()).add(entity);
			return this;
		}

		/**
		 * A common usage of embedded entries are to define a read-only preview. This method provides syntax sugar for
		 * {@link #embed(LinkRelation, Object)}.
		 *
		 * @param linkRelation
		 * @param entity
		 * @return
		 */
		public HalModelBuilder previewFor(LinkRelation linkRelation, Object entity) {
			return embed(linkRelation, entity);
		}

		/**
		 * Add a {@link Link} to the whole thing.
		 * <p/>
		 * NOTE: This adds it to the top level. If you need a link inside an entity, then use the {@link Model.Builder} to
		 * define it as well.
		 *
		 * @param link
		 * @return
		 */
		@Override
		public HalModelBuilder link(Link link) {

			this.links.add(link);
			return this;
		}

		/**
		 * Transform the entities and {@link Link}s into a {@link RepresentationModel}. If there are embedded entries, add a
		 * preferred mediatype of {@link MediaTypes#HAL_JSON} and {@link MediaTypes#HAL_FORMS_JSON}.
		 *
		 * @return
		 */
		@Override
		public RepresentationModel<?> build() {

			/**
			 * If there are no specific {@link LinkRelation}s, and there is no more than one entity, give a simplified
			 * response.
			 */
			if (hasNoSpecificLinkRelations()) {

				if (noEntities()) {
					return new RepresentationModel<>(this.links);
				}

				if (justOneEntity()) {
					return EntityModel.of(this.entityModels.get(NO_RELATION).get(0), this.links);
				}

				// If there is more, just use the code below.
			}

			EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

			return this.entityModels.keySet().stream() //
					.flatMap(linkRelation -> this.entityModels.get(linkRelation).stream() //
							.map(source -> {
								if (RepresentationModel.class.isInstance(source)) {
									((RepresentationModel<?>) source).addPreferredMediaType(MediaTypes.HAL_JSON,
											MediaTypes.HAL_FORMS_JSON);
								}
								return wrappers.wrap(source, linkRelation);
							})) //
					.collect(Collectors.collectingAndThen(Collectors.toList(),
							embeddedWrappers -> CollectionModel.of(embeddedWrappers, this.links)));
		}

		/**
		 * Are there no specific link relations?
		 *
		 * @return
		 */
		private boolean hasNoSpecificLinkRelations() {
			return this.entityModels.keySet().size() == 1 && this.entityModels.containsKey(NO_RELATION);
		}

		/**
		 * Are there no entities contained in the unrelated {@link #entityModels}?
		 *
		 * @return
		 */
		private boolean noEntities() {
			return this.entityModels.containsKey(NO_RELATION) && this.entityModels.get(NO_RELATION).size() == 0;
		}

		/**
		 * Is there just one entity in the unrelated {@link #entityModels}?
		 *
		 * @return
		 */
		private boolean justOneEntity() {
			return this.entityModels.containsKey(NO_RELATION) && this.entityModels.get(NO_RELATION).size() == 1;
		}

	}
}
