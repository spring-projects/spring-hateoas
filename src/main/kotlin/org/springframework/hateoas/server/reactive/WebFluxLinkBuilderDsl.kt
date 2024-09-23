/*
 * Copyright 2002-2022 the original author or authors.
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

import org.reactivestreams.Publisher
import org.springframework.hateoas.Link
import org.springframework.hateoas.LinkRelation
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import kotlin.reflect.KClass

/**
 * Create a [WebFluxLinkBuilder.WebFluxBuilder] pointing to a [func] method.
 *
 * @author Christoph Huber
 */
inline fun <reified C> linkTo(func: C.() -> Unit): WebFluxLinkBuilder.WebFluxBuilder = linkTo(methodOn(C::class.java).apply(func))

/**
 * Create a [Link] with the given [rel].
 *
 * @author Christoph Huber
 */
infix fun WebFluxLinkBuilder.WebFluxBuilder.withRel(rel: LinkRelation): Mono<Link> = withRel(rel).toMono()
infix fun WebFluxLinkBuilder.WebFluxBuilder.withRel(rel: String): Mono<Link> = withRel(rel).toMono()

/**
 * Adds the given [links] to this model.
 *
 * @author Christoph Huber
 */
infix fun <R : RepresentationModel<R>> Mono<R>.add(links: (R) -> Publisher<Link>) = flatMap { model ->
    when (val linksToAdd = links(model)) {
        is Flux<Link> -> linksToAdd.collectList().map { model.add(it) }
        is Mono<Link> -> linksToAdd.map { model.add(it) }
        else -> Mono.error(IllegalStateException("Unsupported Publisher $linksToAdd"))
    }
}

/**
 * Add [links] to the [R] resource.
 *
 * @author Christoph Huber
 */
fun <C, R : RepresentationModel<R>> R.add(controller: Class<C>, links: WebFluxLinkBuilderDsl<C, R>.(R) -> Unit): Mono<R> {
    val builder = WebFluxLinkBuilderDsl(controller, this)
    builder.links(this)
    return builder.build()
}

/**
 * Add [links] to the [R] resource.
 *
 * @author Christoph Huber
 */
fun <C : Any, R : RepresentationModel<R>> R.add(controller: KClass<C>, links: WebFluxLinkBuilderDsl<C, R>.(R) -> Unit): Mono<R> {
    return add(controller.java, links)
}

/**
 * Provide a [WebFluxLinkBuilder] DSL to help write idiomatic Kotlin code.
 *
 * @author Christoph Huber
 */
open class WebFluxLinkBuilderDsl<C, R : RepresentationModel<R>>(
    private val controller: Class<C>,
    private val resource: R,
    private val links: MutableList<Mono<Link>> = mutableListOf()
) {

    /**
     * Create a [WebFluxLinkBuilder.WebFluxBuilder] pointing to [func] method.
     */
    fun <R> linkTo(func: C.() -> R): WebFluxLinkBuilder.WebFluxBuilder = linkTo(methodOn(controller).run(func))

    /**
     * Add a link with the given [rel] to the [resource].
     */
    infix fun WebFluxLinkBuilder.WebFluxBuilder.withRel(rel: String): Mono<Link> {
        return this withRel (LinkRelation.of(rel))
    }

    /**
     * Add a link with the given [rel] to the [resource].
     */
    infix fun WebFluxLinkBuilder.WebFluxBuilder.withRel(rel: LinkRelation): Mono<Link> {
        val link = withRel(rel).toMono()
        links.add(link)

        return link
    }

    fun build(): Mono<R> = Flux.concat(links).collectList().map { resource.add(it) }
}
