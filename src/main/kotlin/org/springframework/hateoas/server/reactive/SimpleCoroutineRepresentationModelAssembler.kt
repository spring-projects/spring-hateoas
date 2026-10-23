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
import org.springframework.hateoas.EntityModel
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Coroutine variant of [SimpleRepresentationModelAssembler].
 *
 * @author Christoph Huber
 */
interface SimpleCoroutineRepresentationModelAssembler<T : Any> : CoroutineRepresentationModelAssembler<T, EntityModel<T>> {
    /**
     * Converts the given entity into a [EntityModel] wrapped in a [Mono].
     */
    override suspend fun toModel(entity: T, exchange: ServerWebExchange): EntityModel<T> {
        val resource = EntityModel.of(entity)
        return addLinks(resource, exchange)
    }

    /**
     * Define links to add to every individual [EntityModel].
     */
    suspend fun addLinks(resource: EntityModel<T>, exchange: ServerWebExchange): EntityModel<T> {
        return resource
    }

    /**
     * Converts all given entities into resources and wraps the collection as a resource as well.
     *
     * @see toModel
     */
    override suspend fun toCollectionModel(entities: Flow<T>, exchange: ServerWebExchange): CollectionModel<EntityModel<T>> {
        val entityModels = entities
            .map { toModel(it, exchange) }
            .toList()
        return addLinks(CollectionModel.of(entityModels), exchange)
    }

    /**
     * Define links to add to the [CollectionModel] collection.
     *
     * @param resources must not be null.
     * @return will never be null.
     */
    suspend fun addLinks(resources: CollectionModel<EntityModel<T>>, exchange: ServerWebExchange): CollectionModel<EntityModel<T>> {
        return resources
    }
}
