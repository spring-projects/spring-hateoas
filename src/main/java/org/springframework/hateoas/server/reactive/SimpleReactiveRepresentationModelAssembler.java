/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.server.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.web.server.ServerWebExchange;

/**
 * Reactive variant of {@link RepresentationModelAssembler} combined with {@link SimpleRepresentationModelAssembler}.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public interface SimpleReactiveRepresentationModelAssembler<T>
		extends ReactiveRepresentationModelAssembler<T, EntityModel<T>> {

	/**
	 * Converts the given entity into a {@link EntityModel} wrapped in a {@link Mono}.
	 *
	 * @param entity
	 * @return
	 */
	@Override
	default Mono<EntityModel<T>> toModel(T entity, ServerWebExchange exchange) {

		EntityModel<T> resource = new EntityModel<>(entity);
		return Mono.just(addLinks(resource, exchange));
	}

	/**
	 * Define links to add to every individual {@link EntityModel}.
	 *
	 * @param resource
	 */
	default EntityModel<T> addLinks(EntityModel<T> resource, ServerWebExchange exchange) {
		return resource;
	}

	/**
	 * Converts all given entities into resources and wraps the collection as a resource as well.
	 *
	 * @see #toResource(Object, ServerWebExchange)
	 * @param entities must not be {@literal null}.
	 * @return {@link CollectionModel} containing {@link EntityModel} of {@code T}.
	 */
	default Mono<CollectionModel<EntityModel<T>>> toCollectionModel(
			Flux<? extends T> entities, ServerWebExchange exchange) {

		return entities //
				.flatMap(entity -> toModel(entity, exchange)) //
				.collectList() //
				.map(CollectionModel::new) //
				.map(it -> addLinks(it, exchange));
	}

	/**
	 * Define links to add to the {@link CollectionModel} collection.
	 *
	 * @param resources
	 */
	default CollectionModel<EntityModel<T>> addLinks(
			CollectionModel<EntityModel<T>> resources, ServerWebExchange exchange) {
		return resources;
	}
}
