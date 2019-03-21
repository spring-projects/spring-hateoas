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
package org.springframework.hateoas.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.SimpleResourceAssembler;
import org.springframework.web.server.ServerWebExchange;

/**
 * Reactive variant of {@link ResourceAssembler} combined with {@link SimpleResourceAssembler}.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
public interface ReactiveResourceAssembler<T, D extends ResourceSupport> {

	/**
	 * Converts the given entity into a {@code D}, which extends {@link ResourceSupport}.
	 *
	 * @param entity
	 * @return
	 */
	Mono<D> toResource(T entity, ServerWebExchange exchange);

	/**
	 * Converts an {@link Iterable} or {@code T}s into an {@link Iterable} of {@link ResourceSupport} and wraps
	 * them in a {@link Resources} instance.
	 *
	 * @param entities must not be {@literal null}.
	 * @return {@link Resources} containing {@code D}.
	 */
	default Mono<Resources<D>> toResources(Flux<? extends T> entities, ServerWebExchange exchange) {

		return entities
			.flatMap(entity -> toResource(entity, exchange))
			.collectList()
			.map(listOfResources -> new Resources<>(listOfResources));
	}

}
