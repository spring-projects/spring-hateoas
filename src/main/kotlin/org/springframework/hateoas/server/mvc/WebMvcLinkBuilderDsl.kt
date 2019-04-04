/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.hateoas.server.mvc

import org.springframework.hateoas.Link
import org.springframework.hateoas.LinkRelation
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn

import kotlin.reflect.KClass

/**
 * Create a [WebMvcLinkBuilder] pointing to a [func] method.
 *
 * @author Roland Kulcsár
 * @author Oliver Drotbohm
 * @author Greg Turnquist
 * @since 1.0
 */
inline fun <reified C> linkTo(func: C.() -> Unit): WebMvcLinkBuilder = linkTo(methodOn(C::class.java).apply(func))

/**
 * Create a [Link] with the given [rel].
 *
 * @author Roland Kulcsár
 * @since 1.0
 */
infix fun WebMvcLinkBuilder.withRel(rel: LinkRelation): Link = withRel(rel)
infix fun WebMvcLinkBuilder.withRel(rel: String): Link = withRel(rel)

/**
 * Add [links] to the [R] resource.
 *
 * @author Roland Kulcsár
 * @since 1.0
 */
fun <C, R : RepresentationModel<R>> R.add(controller: Class<C>, links: WebMvcLinkBuilderDsl<C, R>.(R) -> Unit): R {
    
    val builder = WebMvcLinkBuilderDsl(controller, this)
    builder.links(this)

    return this
}

/**
 * Add [links] to the [R] resource.
 *
 * @author Roland Kulcsár
 * @since 1.0
 */
fun <C : Any, R : RepresentationModel<R>> R.add(controller: KClass<C>, links: WebMvcLinkBuilderDsl<C, R>.(R) -> Unit): R {
    return add(controller.java, links)
}

/**
 * Provide a [LinkBuilder] DSL to help write idiomatic Kotlin code.
 *
 * @author Roland Kulcsár
 * @since 1.0
 */
open class WebMvcLinkBuilderDsl<C, R : RepresentationModel<R>>(val controller: Class<C>, val resource: R) {

    /**
     * Create a [WebMvcLinkBuilder] pointing to [func] method.
     */
    fun <R> linkTo(func: C.() -> R): WebMvcLinkBuilder = linkTo(methodOn(controller).run(func))

    /**
     * Add a link with the given [rel] to the [resource].
     */
    infix fun WebMvcLinkBuilder.withRel(rel: String): Link {
        return this withRel(LinkRelation.of(rel))
    }

    /**
     * Add a link with the given [rel] to the [resource].
     */
    infix fun WebMvcLinkBuilder.withRel(rel: LinkRelation): Link {

        val link = withRel(rel)
        resource.add(link)

        return link
    }
}
