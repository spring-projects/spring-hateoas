/*
 * Copyright 2012-2018 the original author or authors.
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

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;

/**
 * Interface for components that convert a domain type into a {@link RepresentationModel}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public interface RepresentationModelAssembler<T, D extends RepresentationModel<?>> {

	/**
	 * Converts the given entity into a {@code D}, which extends {@link RepresentationModel}.
	 *
	 * @param entity
	 * @return
	 */
	D toModel(T entity);

	/**
	 * Converts an {@link Iterable} or {@code T}s into an {@link Iterable} of {@link RepresentationModel} and wraps them
	 * in a {@link CollectionModel} instance.
	 *
	 * @param entities must not be {@literal null}.
	 * @return {@link CollectionModel} containing {@code D}.
	 */
	default CollectionModel<D> toCollectionModel(Iterable<? extends T> entities) {

		return StreamSupport.stream(entities.spliterator(), false) //
				.map(this::toModel) //
				.collect(Collectors.collectingAndThen(Collectors.toList(), CollectionModel::new));
	}
}
