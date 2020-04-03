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
import org.springframework.hateoas.*
import org.springframework.web.server.ServerWebExchange

class SimpleReactiveRepresentationModelAssemblerBuilderDslKtTest : TestUtils() {

    private val testResourceAssembler = TestResourceAssemblerSimple()

    private val employees = flow {
        emit(Employee("Frodo"))
        emit(Employee("Bilbo"))
    }

    @Test // #1255
    fun `passing in a Kotlin Flow should get converted to a Reactor Flow before resource assembly`() {
        val exchange = mockk<ServerWebExchange>()
        runBlocking {
            val collectionModel = testResourceAssembler.toCollectionModel(employees, exchange)

            assertThat(collectionModel.links).containsExactly(Link.of("/employees"))
        }
    }

    class TestResourceAssemblerSimple : SimpleReactiveRepresentationModelAssembler<Employee> {
        override fun addLinks(resource: EntityModel<Employee>, exchange: ServerWebExchange): EntityModel<Employee> {
            resource.add(Link.of("/employees/1"))
            return resource
        }

        override fun addLinks(resources: CollectionModel<EntityModel<Employee>>, exchange: ServerWebExchange): CollectionModel<EntityModel<Employee>> {
            resources.add(Link.of("/employees"))
            return resources
        }
    }

    data class Employee(val name: String)

}
