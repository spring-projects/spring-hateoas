/*
 * Copyright 2018-2024 the original author or authors.
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

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.ServerWebExchange

/**
 * @author Christoph Huber
 */
internal class SimpleCoroutineRepresentationModelAssemblerTest {
    private val request = MockServerHttpRequest.get("http://localhost:8080/api")
    private val exchange = MockServerWebExchange.from(request)

    @Test
    fun convertingToResourceShouldWork() {
        runBlocking {
            val assembler = TestResourceAssembler()
            val resource = assembler.toModel(Employee("Frodo"), exchange)

            assertThat(resource.content.name).isEqualTo("Frodo")
            assertThat(resource.links).isEmpty()
        }
    }

    @Test
    fun convertingToResourcesShouldWork() {
        runBlocking {
            val assembler = TestResourceAssembler()
            val resources = assembler.toCollectionModel(flowOf(Employee("Frodo")), exchange)

            assertThat(resources.content).containsExactly(EntityModel.of(Employee("Frodo")))
            assertThat(resources.links).isEmpty()
        }
    }

    @Test
    fun convertingToResourceWithCustomLinksShouldWork() {
        runBlocking {
            val assembler = ResourceAssemblerWithCustomLink()
            val resource = assembler.toModel(Employee("Frodo"), exchange)

            assertThat(resource.content.name).isEqualTo("Frodo")
            assertThat(resource.links).containsExactly(Link.of("/employees").withRel("employees"))
        }
    }

    @Test
    fun convertingToResourcesWithCustomLinksShouldWork() {
        runBlocking {
            val assembler = ResourceAssemblerWithCustomLink()
            val resources = assembler.toCollectionModel(flowOf(Employee("Frodo")), exchange)

            assertThat(resources.content).containsExactly(
                EntityModel.of(Employee("Frodo"), Link.of("/employees").withRel("employees"))
            )
            assertThat(resources.links).isEmpty()
        }
    }

    internal inner class TestResourceAssembler : SimpleCoroutineRepresentationModelAssembler<Employee>

    internal inner class ResourceAssemblerWithCustomLink : SimpleCoroutineRepresentationModelAssembler<Employee> {
        override suspend fun addLinks(resource: EntityModel<Employee>, exchange: ServerWebExchange): EntityModel<Employee> {
            return resource.add(Link.of("/employees").withRel("employees"))
        }
    }

    data class Employee(val name: String)
}
