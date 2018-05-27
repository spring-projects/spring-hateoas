/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.hateoas

import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.hateoas.mvc.ControllerLinkBuilder.afford
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import kotlin.reflect.KClass

/**
 * Creates the [Link] with the given [rel].
 *
 * @author Roland Kulcsár
 */
infix fun ControllerLinkBuilder.withRel(rel: String): Link = withRel(rel)

/**
 * Create new [Link] with additional [Affordance]s.
 *
 * @author Roland Kulcsár
 */
inline infix fun Link.andAffordances(setup: AffordancesBuilderDsl.() -> Unit): Link {
    val builder = AffordancesBuilderDsl()
    builder.setup()

    return andAffordances(builder.affordances)
}

/**
 * Creates a [ControllerLinkBuilder] pointing to [func] method.
 *
 * @author Roland Kulcsár
 */
inline fun <reified C> linkTo(func: C.() -> Unit): ControllerLinkBuilder = linkTo(methodOn(C::class.java).apply(func))

/**
 * Extract a [Link] from the [ControllerLinkBuilder] and look up the related [Affordance]. Should
 * only be one.
 *
 * @author Roland Kulcsár
 */
inline fun <reified C> afford(func: C.() -> Unit): Affordance = afford(methodOn(C::class.java).apply(func))

/**
 * Adds the [links] to the [R] resource.
 *
 * @author Roland Kulcsár
 */
fun <C, R : ResourceSupport> R.add(controller: Class<C>, links: LinkBuilderDsl<C, R>.(R) -> Unit): R {
    val builder = LinkBuilderDsl(controller, this)
    builder.links(this)

    return this
}

/**
 * Adds the [links] to the [R] resource.
 *
 * @author Roland Kulcsár
 */
fun <C : Any, R : ResourceSupport> R.add(controller: KClass<C>, links: LinkBuilderDsl<C, R>.(R) -> Unit): R {
    return add(controller.java, links)
}

/**
 * Provides a link builder Kotlin DSL in order to be able to write idiomatic Kotlin code.
 *
 * @author Roland Kulcsár
 */
open class LinkBuilderDsl<C, R : ResourceSupport>(val controller: Class<C>, val resource: R) {

    /**
     * Creates a [ControllerLinkBuilder] pointing to [func] method.
     */
    fun <R> linkTo(func: C.() -> R): ControllerLinkBuilder = linkTo(methodOn(controller).run(func))

    /**
     * Adds link with the given [rel] to [resource].
     */
    infix fun ControllerLinkBuilder.withRel(rel: String): Link {
        val link = withRel(rel)
        resource.add(link)

        return link
    }
}

/**
 * Provides an affordances builder Kotlin DSL in order to be able to write idiomatic Kotlin code.
 *
 * @author Roland Kulcsár
 */
open class AffordancesBuilderDsl(val affordances: MutableList<Affordance> = mutableListOf()) {

    inline fun <reified C> afford(func: C.() -> Any) {
        val affordance = afford(methodOn(C::class.java).func())
        affordances.add(affordance)
    }
}
