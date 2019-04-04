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

import org.springframework.hateoas.Affordance
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn

/**
 * Create a new [Link] with additional [Affordance]s.
 *
 * @author Roland Kulcsár
 * @author Greg Turnquist
 * @since 1.0
 */
inline infix fun Link.andAffordances(setup: WebMvcAffordanceBuilderDsl.() -> Unit): Link {

    val builder = WebMvcAffordanceBuilderDsl()
    builder.setup()

    return andAffordances(builder.affordances)
}

/**
 * Extract a [Link] from the [WebMvcLinkBuilder] and look up the related [Affordance]. Should
 * only be one.
 *
 * @author Roland Kulcsár
 * @since 1.0
 */
inline fun <reified C> afford(func: C.() -> Unit): Affordance = afford(methodOn(C::class.java).apply(func))

/**
 * Provide an [Affordance]s DSL to help write idiomatic Kotlin code.
 *
 * @author Roland Kulcsár
 * @since 1.0
 */
open class WebMvcAffordanceBuilderDsl(val affordances: MutableList<Affordance> = mutableListOf()) {

    inline fun <reified C> afford(func: C.() -> Any) {
        
        val affordance = afford(methodOn(C::class.java).func())
        affordances.add(affordance)
    }
}
