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
package org.springframework.hateoas.mvc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.hateoas.*
import org.springframework.hateoas.server.mvc.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Unit tests for [WebMvcLinkBuilderDsl] and [WebMvcAffordanceBuilderDsl].
 *
 * @author Roland Kulcsár
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
class WebMvcLinkBuilderDslUnitTest : TestUtils() {

    private val REL_PRODUCTS = "products"

    /**
     * @see #715
     */
    @Test
    fun `creates link to controller method`() {

        val self = linkTo<CustomerController> { findById("15") } withRel IanaLinkRelations.SELF

        assertPointsToMockServer(self)
        assertThat(self.rel).isEqualTo(IanaLinkRelations.SELF)
        assertThat(self.href).endsWith("/customers/15")
    }

    /**
     * @see #715
     */
    @Test
    fun `adds links to wrapped domain object`() {

        val customer = EntityModel(Customer("15", "John Doe"))

        customer.add(CustomerController::class) {
            linkTo { findById(it.content.id) } withRel IanaLinkRelations.SELF
            linkTo { findProductsById(it.content.id) } withRel REL_PRODUCTS
        }

        customer.links.forEach { assertPointsToMockServer(it) }
        assertThat(customer.hasLink(IanaLinkRelations.SELF)).isTrue()
        assertThat(customer.hasLink(REL_PRODUCTS)).isTrue()
    }

    /**
     * @see #715
     */
    @Test
    fun `adds links to resourcesupport object`() {

        val customer = CustomerModel("15", "John Doe")

        customer.add(CustomerController::class) {
            linkTo { findById(it.id) } withRel IanaLinkRelations.SELF
            linkTo { findProductsById(it.id) } withRel REL_PRODUCTS
        }

        customer.links.forEach { assertPointsToMockServer(it) }
        assertThat(customer.hasLink(IanaLinkRelations.SELF)).isTrue()
        assertThat(customer.hasLink(REL_PRODUCTS)).isTrue()
    }

    data class Customer(val id: String, val name: String)
    data class CustomerDTO(val name: String)
    open class CustomerModel(val id: String, val name: String) : RepresentationModel<CustomerModel>()
    open class ProductModel(val id: String) : RepresentationModel<ProductModel>()

    @RequestMapping("/customers")
    interface CustomerController {

        @GetMapping("/{id}")
        fun findById(@PathVariable id: String): ResponseEntity<CustomerModel>

        @GetMapping("/{id}/products")
        fun findProductsById(@PathVariable id: String): PagedModel<ProductModel>

        @PutMapping("/{id}")
        fun update(@PathVariable id: String, @RequestBody customer: CustomerDTO): ResponseEntity<CustomerModel>

        @DeleteMapping("/{id}")
        fun delete(@PathVariable id: String): ResponseEntity<Unit>
    }
}
