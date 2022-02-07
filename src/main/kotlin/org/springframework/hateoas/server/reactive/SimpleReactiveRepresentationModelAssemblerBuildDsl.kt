/*
 * Copyright 2020-2022 the original author or authors.
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
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.asFlux
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.web.server.ServerWebExchange

/**
 * Add support for Kotlin co-routines.
 *
 * @author Greg Turnquist
 * @since 1.1
 */
suspend fun <T : Any> SimpleReactiveRepresentationModelAssembler<T>.toModelAndAwait(
    entity: T,
    exchange: ServerWebExchange): EntityModel<T> = toModel(entity, exchange).awaitFirst()

/**
 * Add support for Kotlin co-routines.
 *
 * @author Juergen Zimmerman
 * @author Greg Turnquist
 * @since 1.1
 *
 */
@Deprecated("use toCollectionModelAndAwait(entites, exchange)",
    replaceWith = ReplaceWith("toCollectionModelAndAwait(entities, exchange)"))
suspend fun <T : Any> SimpleReactiveRepresentationModelAssembler<T>.toCollectionModel(
    entities: Flow<T>,
    exchange: ServerWebExchange): CollectionModel<EntityModel<T>> = toCollectionModelAndAwait(entities, exchange)

/**
 * Add support for Kotlin co-routines.
 *
 * @author Juergen Zimmerman
 * @author Greg Turnquist
 * @since 1.1
 */
suspend fun <T : Any> SimpleReactiveRepresentationModelAssembler<T>.toCollectionModelAndAwait(
    entities: Flow<T>,
    exchange: ServerWebExchange): CollectionModel<EntityModel<T>> = toCollectionModel(entities.asFlux(), exchange).awaitFirst()

