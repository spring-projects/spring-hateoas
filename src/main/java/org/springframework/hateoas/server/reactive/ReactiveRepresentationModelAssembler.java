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
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.web.server.ServerWebExchange;

/**
 * Reactive variant of {@link RepresentationModelAssembler} combined with {@link SimpleRepresentationModelAssembler}.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public interface ReactiveRepresentationModelAssembler<T, D extends RepresentationModel<D>> {

	/**
	 * Converts the given entity into a {@code D}, which extends {@link RepresentationModel}.
	 *
	 * @param entity
	 * @return
	 */
	Mono<D> toModel(T entity, ServerWebExchange exchange);

	/**
	 * Converts an {@link Iterable} or {@code T}s into an {@link Iterable} of {@link RepresentationModel} and wraps them
	 * in a {@link CollectionModel} instance.
	 *
	 * @param entities must not be {@literal null}.
	 * @return {@link CollectionModel} containing {@code D}.
	 */
	default Mono<CollectionModel<D>> toCollectionModel(Flux<? extends T> entities,
			ServerWebExchange exchange) {

		return entities.flatMap(entity -> toModel(entity, exchange)) //
				.collectList() //
				.map(CollectionModel::new);
	}
}
