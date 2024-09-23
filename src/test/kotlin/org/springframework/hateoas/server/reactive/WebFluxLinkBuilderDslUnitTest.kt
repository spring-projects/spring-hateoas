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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.hateoas.*
import org.springframework.hateoas.IanaLinkRelations.SELF
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for [WebFluxLinkBuilderDsl].
 *
 * @author Christoph Huber
 */
class WebFluxLinkBuilderDslUnitTest {

    @Test
    fun `creates link to controller method`() {

        val self = linkTo<CustomerController> { findById("15") } withRel SELF

        StepVerifier.create(self)
            .expectNextMatches {
                assertThat(it.rel).isEqualTo(SELF)
                assertThat(it.href).isEqualTo("/customers/15")
                true
            }
            .verifyComplete()
    }

    @Test
    fun `adds links to wrapped domain object`() {

        val customer = EntityModel.of(Customer("15", "John Doe"))
            .add(CustomerController::class) { entity ->
                linkTo { findById(entity.content.id) } withRel SELF
                linkTo { findProductsById(entity.content.id) } withRel REL_PRODUCTS
            }

        StepVerifier.create(customer)
            .expectNextMatches {
                assertThat(it.hasLink(SELF)).isTrue()
                assertThat(it.hasLink(REL_PRODUCTS)).isTrue()
                true
            }
            .verifyComplete()
    }

    @Test
    fun `adds links to resourcesupport object`() {

        val customer = CustomerModel("15", "John Doe")
            .add(CustomerController::class) {
                linkTo { findById(it.id) } withRel SELF
                linkTo { findProductsById(it.id) } withRel REL_PRODUCTS
            }

        StepVerifier.create(customer)
            .expectNextMatches {
                assertThat(it.hasLink(SELF)).isTrue()
                assertThat(it.hasLink(REL_PRODUCTS)).isTrue()
                true
            }
            .verifyComplete()
    }

    @Test
    fun `add link to mono`() {
        val customer = Mono.just(CustomerModel("15", "John Doe"))
            .add { linkTo<CustomerController> { findById(it.id) } withRel SELF }
        StepVerifier.create(customer)
            .expectNextMatches {
                assertThat(it.hasLink(SELF)).isTrue()
                true
            }
    }

    @Test
    fun `add links to mono`() {
        val customer = Mono.just(CustomerModel("15", "John Doe"))
            .add {
                Flux.concat(
                    linkTo<CustomerController> { findById(it.id) } withRel SELF,
                    linkTo<CustomerController> { findProductsById(it.id) } withRel REL_PRODUCTS
                )
            }
        StepVerifier.create(customer)
            .expectNextMatches {
                assertThat(it.hasLink(SELF)).isTrue()
                assertThat(it.hasLink(REL_PRODUCTS)).isTrue()
                true
            }
    }

    data class Customer(val id: String, val name: String)
    data class CustomerDTO(val name: String)
    open class CustomerModel(val id: String, val name: String) : RepresentationModel<CustomerModel>()
    open class ProductModel(val id: String) : RepresentationModel<ProductModel>()

    @RequestMapping("/customers")
    interface CustomerController {

        @GetMapping("/{id}")
        fun findById(@PathVariable id: String): Mono<ResponseEntity<CustomerModel>>

        @GetMapping("/{id}/products")
        fun findProductsById(@PathVariable id: String): Mono<PagedModel<ProductModel>>

        @PutMapping("/{id}")
        fun update(@PathVariable id: String, @RequestBody customer: CustomerDTO): Mono<ResponseEntity<CustomerModel>>

        @DeleteMapping("/{id}")
        fun delete(@PathVariable id: String): Mono<ResponseEntity<Unit>>
    }

    companion object {
        private const val REL_PRODUCTS = "products"
    }
}
