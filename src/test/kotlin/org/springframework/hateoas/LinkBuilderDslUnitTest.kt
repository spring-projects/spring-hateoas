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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Unit tests for [LinkBuilderDsl].
 *
 * @author Roland Kulcsár
 */
class LinkBuilderDslUnitTest : TestUtils() {

    private val REL_PRODUCTS = "products"

    @Test
    fun `creates link to controller method`() {
        val self = linkTo<CustomerController> { findById("15") } withRel Link.REL_SELF

        assertPointsToMockServer(self)
        assertThat(self.rel).isEqualTo(Link.REL_SELF)
        assertThat(self.href).endsWith("/customers/15")
    }

    @Test
    fun `creates affordance to controller method`() {
        val delete = afford<CustomerController> { delete("15") }

        assertThat(delete.httpMethod).isEqualTo(HttpMethod.DELETE)
        assertThat(delete.name).isEqualTo("delete")
    }

    @Test
    fun `creates link to controller method with affordances`() {
        val id = "15"
        val self = linkTo<CustomerController> { findById(id) } withRel Link.REL_SELF
        val selfWithAffordances = self andAffordances {
            afford<CustomerController> { update(id, CustomerDto("John Doe")) }
            afford<CustomerController> { delete(id) }
        }

        assertThat(selfWithAffordances.affordances).hasSize(3)
        assertThat(selfWithAffordances.hashCode()).isNotEqualTo(self.hashCode())
        assertThat(selfWithAffordances).isNotEqualTo(self)
    }

    @Test
    fun `adds links to wrapped domain object`() {
        val customer = Resource(Customer("15", "John Doe"))

        customer.add(CustomerController::class) {
            linkTo { findById(it.content.id) } withRel Link.REL_SELF
            linkTo { findProductsById(it.content.id) } withRel REL_PRODUCTS
        }

        customer.links.forEach { assertPointsToMockServer(it) }
        assertThat(customer.hasLink(Link.REL_SELF)).isTrue()
        assertThat(customer.hasLink(REL_PRODUCTS)).isTrue()
    }

    @Test
    fun `adds links to resource`() {
        val customer = CustomerResource("15", "John Doe")

        customer.add(CustomerController::class) {
            linkTo { findById(it.id) } withRel Link.REL_SELF
            linkTo { findProductsById(it.id) } withRel REL_PRODUCTS
        }

        customer.links.forEach { assertPointsToMockServer(it) }
        assertThat(customer.hasLink(Link.REL_SELF)).isTrue()
        assertThat(customer.hasLink(REL_PRODUCTS)).isTrue()
    }

    data class Customer(val id: String, val name: String)
    data class CustomerDto(val name: String)
    open class CustomerResource(val id: String, val name: String) : ResourceSupport()
    open class ProductResource(val id: String) : ResourceSupport()

    @RequestMapping("/customers")
    interface CustomerController {

        @GetMapping("/{id}")
        fun findById(@PathVariable id: String): ResponseEntity<CustomerResource>

        @GetMapping("/{id}/products")
        fun findProductsById(@PathVariable id: String): PagedResources<ProductResource>

        @PutMapping("/{id}")
        fun update(@PathVariable id: String, @RequestBody customer: CustomerDto): ResponseEntity<CustomerResource>

        @DeleteMapping("/{id}")
        fun delete(@PathVariable id: String): ResponseEntity<Unit>
    }
}
