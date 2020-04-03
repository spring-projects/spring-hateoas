/*
 * Copyright 2019-2020 the original author or authors.
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

import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.LinkRelation
import org.springframework.hateoas.TestUtils
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * @author Greg Turnquist
 * @since 1.1
 */
class ReactiveRepresentationModelAssemblerBuilderDslUnitTest : TestUtils() {

    private val EMPLOYEES_RELATION = LinkRelation.of("employees")

    private val employees = flow {
        emit(Employee("Frodo"))
        emit(Employee("Bilbo"))
    }

    @Test // #1213
    fun `Kotlin Flows should render as CollectionModels`() {

        val testResourceAssembler = TestResourceAssembler()
        val exchange = mockk<ServerWebExchange>()

        runBlocking {
            val collectionModel = testResourceAssembler.toCollectionModel(employees, exchange)

            assertThat(collectionModel.content.flatMap { entityModel -> entityModel.links })
                .containsExactlyInAnyOrder(Link.of("/employees", EMPLOYEES_RELATION), Link.of("/employees", EMPLOYEES_RELATION))
        }
    }

    class TestResourceAssembler : ReactiveRepresentationModelAssembler<Employee, EntityModel<Employee>> {
        override fun toModel(entity: Employee, exchange: ServerWebExchange): Mono<EntityModel<Employee>> {
            return Mono.just(EntityModel.of(entity, Link.of("/employees", LinkRelation.of("employees"))))
        }
    }

    data class Employee(val name: String)

}
