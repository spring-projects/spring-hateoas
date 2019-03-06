/*
 * Copyright 2019 the original author or authors.
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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.hateoas.server.core.EmbeddedWrappers;

/**
 * @author Greg Turnquist
 */
public class ModelBuilder {

	/**
	 * Helper method to construct an {@link EntityModel}.
	 *
	 * @param domainObject
	 * @param <T>
	 * @return
	 */
	public static <T> EntityModelBuilder<T> entity(T domainObject) {
		return new EntityModelBuilder<>(domainObject);
	}

	/**
	 * Helper method to construct an embedded {@link CollectionModel}.
	 *
	 * @return
	 */
	public static EmbeddedModelBuilder embed() {
		return new EmbeddedModelBuilder();
	}

	/**
	 * Helper method when you already know the first embedded {@link LinkRelation}.
	 * 
	 * @param linkRelation
	 * @return
	 */
	public static EmbeddedModelBuilder embed(LinkRelation linkRelation) {
		return new EmbeddedModelBuilder().embed(linkRelation);
	}

	public static <T> CollectionModelBuilder<T> collection() {
		return new CollectionModelBuilder<>();
	}

	/**
	 * Builder to fluently assemble an embedded {@link CollectionModel} representation.
	 */
	public static class EmbeddedModelBuilder {

		private final EmbeddedWrappers wrappers;
		private final Map<LinkRelation, List<EntityModel<?>>> entityModels;
		private final List<Link> rootLinks;

		private LinkRelation currentLinkRelation;

		public EmbeddedModelBuilder() {

			this.wrappers = new EmbeddedWrappers(false);
			this.entityModels = new LinkedHashMap<>();
			this.rootLinks = new ArrayList<>();
		}

		public EmbeddedModelBuilder embed(LinkRelation linkRelation) {

			currentLinkRelation = linkRelation;
			entityModels.putIfAbsent(currentLinkRelation, new ArrayList<>());

			return this;
		}

		public <T> EmbeddedModelBuilder entity(T domainObject) {

			EntityModel<T> value = new EntityModel<>(domainObject);
			entityModels.get(currentLinkRelation).add(value);

			return this;
		}

		public <T> EmbeddedModelBuilder entityModel(EntityModel<T> model) {

			entity(model.getContent()).links(model.getLinks());
			return this;
		}

		public <T> EmbeddedModelBuilder link(Link link) {

			List<EntityModel<?>> entities = entityModels.get(currentLinkRelation);
			entities.get(entities.size() - 1).add(link);
			return this;
		}

		public EmbeddedModelBuilder links(Links links) {

			links.forEach(this::link);
			return this;
		}

		public EmbeddedModelBuilder links(Link... links) {

			Arrays.asList(links).forEach(this::link);
			return this;
		}

		public EmbeddedModelBuilder rootLink(Link link) {

			rootLinks.add(link);
			return this;
		}

		public EmbeddedModelBuilder rootLinks(Links links) {

			links.forEach(this::link);
			return this;
		}

		public EmbeddedModelBuilder rootLinks(Link... links) {

			Arrays.asList(links).forEach(this::link);
			return this;
		}

		public CollectionModel<?> build() {

			return entityModels.keySet().stream() //
					.flatMap(linkRelation -> entityModels.get(linkRelation).stream() //
							.map(entityModel -> wrappers.wrap(entityModel, linkRelation)) //
							.collect(Collectors.toList()).stream())
					.collect(Collectors.collectingAndThen(Collectors.toList(),
							embeddedWrappers -> new CollectionModel<>(embeddedWrappers, rootLinks)));
		}
	}

	/**
	 * Builder to fluently assemble an {@link EntityModel} representation.
	 */
	public static class EntityModelBuilder<T> {

		private EntityModel<T> entityModel;

		public EntityModelBuilder(T domainObject) {
			this.entityModel = new EntityModel<>(domainObject);
		}

		public EntityModelBuilder<T> link(Link link) {

			entityModel.add(link);
			return this;
		}

		public EntityModel<T> build() {
			return entityModel;
		}

	}

	public static class CollectionModelBuilder<T> {

		private final List<EntityModel<T>> items;
		private final List<Link> rootLinks;

		public CollectionModelBuilder() {

			this.items = new ArrayList<>();
			this.rootLinks = new ArrayList<>();
		}

		public CollectionModelBuilder<T> entity(T domainObj) {

			items.add(new EntityModel<T>(domainObj));
			return this;
		}

		public CollectionModelBuilder<T> link(Link link) {

			items.get(items.size() - 1).add(link);
			return this;
		}

		public CollectionModelBuilder<T> rootLink(Link link) {

			rootLinks.add(link);
			return this;
		}

		public CollectionModelBuilder<T> rootLinks(Links rootLinks) {

			rootLinks.forEach(this.rootLinks::add);
			return this;
		}

		public CollectionModel<EntityModel<T>> build() {
			return new CollectionModel<>(items, rootLinks);
		}
	}
}
