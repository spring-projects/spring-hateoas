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
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Unit tests for [WebMvcLinkBuilderDsl] and [WebMvcAffordanceBuilderDsl].
 *
 * @author Roland Kulcsár
 * @author Greg Turnquist
 */
class WebMvcAffordanceBuilderDslUnitTest : TestUtils() {

    /**
     * @see #715
     */
    @Test
    fun `creates affordance to controller method`() {

        val delete = afford<CustomerController> { delete("15") }

        val affordanceModel = delete.getAffordanceModel<AffordanceModel>(MediaTypes.HAL_FORMS_JSON)

        assertThat(affordanceModel.httpMethod).isEqualTo(HttpMethod.DELETE)
        assertThat(affordanceModel.name).isEqualTo("delete")
    }
    
    /**
     * @see #715
     */
    @Test
    fun `creates link to controller method with affordances`() {

        val id = "15"
        val self = linkTo<CustomerController> { findById(id) } withRel IanaLinkRelations.SELF
        val selfWithAffordances = self andAffordances {
            afford<CustomerController> { update(id, CustomerDTO("John Doe")) }
            afford<CustomerController> { delete(id) }
        }

        assertThat(selfWithAffordances.rel).isEqualTo(IanaLinkRelations.SELF)
        assertThat(selfWithAffordances.href).isEqualTo("http://localhost/customers/15")
        
        assertThat(selfWithAffordances.affordances).hasSize(3)
        assertThat(selfWithAffordances.affordances[0].getAffordanceModel<AffordanceModel>(MediaTypes.HAL_FORMS_JSON).httpMethod).isEqualTo(HttpMethod.GET)
        assertThat(selfWithAffordances.affordances[0].getAffordanceModel<AffordanceModel>(MediaTypes.HAL_FORMS_JSON).name).isEqualTo("findById")

        assertThat(selfWithAffordances.affordances[1].getAffordanceModel<AffordanceModel>(MediaTypes.HAL_FORMS_JSON).httpMethod).isEqualTo(HttpMethod.PUT)
        assertThat(selfWithAffordances.affordances[1].getAffordanceModel<AffordanceModel>(MediaTypes.HAL_FORMS_JSON).name).isEqualTo("update")

        assertThat(selfWithAffordances.affordances[2].getAffordanceModel<AffordanceModel>(MediaTypes.HAL_FORMS_JSON).httpMethod).isEqualTo(HttpMethod.DELETE)
        assertThat(selfWithAffordances.affordances[2].getAffordanceModel<AffordanceModel>(MediaTypes.HAL_FORMS_JSON).name).isEqualTo("delete")

        assertThat(selfWithAffordances.hashCode()).isNotEqualTo(self.hashCode())
        assertThat(selfWithAffordances).isNotEqualTo(self)
    }

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
