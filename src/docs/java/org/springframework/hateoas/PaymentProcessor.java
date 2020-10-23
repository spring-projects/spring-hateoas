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
package org.springframework.hateoas;

import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.support.Order;

/**
 * @author Greg Turnquist
 */
// tag::code[]
public class PaymentProcessor implements RepresentationModelProcessor<EntityModel<Order>> { // <1>

	@Override
	public EntityModel<Order> process(EntityModel<Order> model) {

		model.add( // <2>
				Link.of("/payments/{orderId}").withRel(LinkRelation.of("payments")) //
						.expand(model.getContent().getOrderId()));

		return model; // <3>
	}
}
// end::code[]
