/*
 * Copyright 2019-2024 the original author or authors.
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
package org.springframework.hateoas.server.reactive

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.RepresentationModel
import org.springframework.web.server.ServerWebExchange

/**
 * Coroutine variant of [RepresentationModelAssembler].
 *
 * @author Christoph Huber
 */
interface CoroutineRepresentationModelAssembler<T, D : RepresentationModel<*>> {
    /**
     * Converts the given entity into a `D`, which extends [RepresentationModel].
     */
    suspend fun toModel(entity: T, exchange: ServerWebExchange): D

    /**
     * Converts an [Iterable] or `T`s into an [Iterable] of [RepresentationModel] and wraps them
     * in a [CollectionModel] instance.
     */
    suspend fun toCollectionModel(entities: Flow< T>, exchange: ServerWebExchange): CollectionModel<D> {
        val entities = entities.map { toModel(it, exchange) }.toList()
        return CollectionModel.of(entities)
    }
}
