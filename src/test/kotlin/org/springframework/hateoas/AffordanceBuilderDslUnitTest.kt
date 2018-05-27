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
import org.springframework.web.bind.annotation.*

/**
 * Unit tests for [LinkBuilderDsl] and [AffordanceBuilderDsl].
 *
 * @author Roland Kulcsár
 */
class AffordanceBuilderDslUnitTest : TestUtils() {

    /**
     * @see #715
     */
    @Test
    fun `creates affordance to controller method`() {

        val delete = afford<CustomerController> { delete("15") }

        assertThat(delete.httpMethod).isEqualTo(HttpMethod.DELETE)
        assertThat(delete.name).isEqualTo("delete")
    }
    
    /**
     * @see #715
     */
    @Test
    fun `creates link to controller method with affordances`() {

        val id = "15"
        val self = linkTo<CustomerController> { findById(id) } withRel Link.REL_SELF
        val selfWithAffordances = self andAffordances {
            afford<CustomerController> { update(id, CustomerDTO("John Doe")) }
            afford<CustomerController> { delete(id) }
        }

        assertThat(selfWithAffordances.rel).isEqualTo(Link.REL_SELF)
        assertThat(selfWithAffordances.href).isEqualTo("http://localhost/customers/15")
        
        assertThat(selfWithAffordances.affordances).hasSize(3)
        assertThat(selfWithAffordances.affordances[0].httpMethod).isEqualTo(HttpMethod.GET)
        assertThat(selfWithAffordances.affordances[0].name).isEqualTo("findById")

        assertThat(selfWithAffordances.affordances[1].httpMethod).isEqualTo(HttpMethod.PUT)
        assertThat(selfWithAffordances.affordances[1].name).isEqualTo("update")

        assertThat(selfWithAffordances.affordances[2].httpMethod).isEqualTo(HttpMethod.DELETE)
        assertThat(selfWithAffordances.affordances[2].name).isEqualTo("delete")

        assertThat(selfWithAffordances.hashCode()).isNotEqualTo(self.hashCode())
        assertThat(selfWithAffordances).isNotEqualTo(self)
    }

    data class Customer(val id: String, val name: String)
    data class CustomerDTO(val name: String)
    open class CustomerResource(val id: String, val name: String) : ResourceSupport()
    open class ProductResource(val id: String) : ResourceSupport()

    @RequestMapping("/customers")
    interface CustomerController {

        @GetMapping("/{id}")
        fun findById(@PathVariable id: String): ResponseEntity<CustomerResource>

        @GetMapping("/{id}/products")
        fun findProductsById(@PathVariable id: String): PagedResources<ProductResource>

        @PutMapping("/{id}")
        fun update(@PathVariable id: String, @RequestBody customer: CustomerDTO): ResponseEntity<CustomerResource>

        @DeleteMapping("/{id}")
        fun delete(@PathVariable id: String): ResponseEntity<Unit>
    }
}
