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

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.SimpleResourceAssembler;
import org.springframework.web.server.ServerWebExchange;

/**
 * Reactive variant of {@link ResourceAssembler} combined with {@link SimpleResourceAssembler}.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
public interface SimpleReactiveResourceAssembler<T> extends ReactiveResourceAssembler<T, Resource<T>> {

	/**
	 * Converts the given entity into a {@link Resource} wrapped in a {@link Mono}.
	 *
	 * @param entity
	 * @return
	 */
	default Mono<Resource<T>> toResource(T entity, ServerWebExchange exchange) {

		Resource<T> resource = new Resource<>(entity);
		addLinks(resource, exchange);
		return Mono.just(resource);
	}

	/**
	 * Define links to add to every individual {@link Resource}.
	 *
	 * @param resource
	 */
	void addLinks(Resource<T> resource, ServerWebExchange exchange);

	/**
	 * Converts all given entities into resources and wraps the collection as a resource as well.
	 *
	 * @see #toResource(Object, ServerWebExchange)
	 * @param entities must not be {@literal null}.
	 * @return {@link Resources} containing {@link Resource} of {@code T}.
	 */
	default Mono<Resources<Resource<T>>> toResources(Flux<? extends T> entities, ServerWebExchange exchange) {

		return entities //
				.flatMap(entity -> toResource(entity, exchange)) //
				.collectList() //
				.map(listOfResources -> {
					Resources<Resource<T>> resources = new Resources<>(listOfResources);
					addLinks(resources, exchange);
					return resources;
				});
	}

	/**
	 * Define links to add to the {@link Resources} collection.
	 *
	 * @param resources
	 */
	void addLinks(Resources<Resource<T>> resources, ServerWebExchange exchange);
}
