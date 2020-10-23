/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.hateoas.mediatype.problem;

import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Oliver Drotbohm
 */

@RequiredArgsConstructor
// tag::header[]
@RestController
class PaymentController {
	// end::header[]

	private static final URI OUT_OF_CREDIT_URI = null;
	private static final UriTemplate PAYMENT_ERROR_INSTANCE = UriTemplate.of("/incidents/{id}");
	private static final UriTemplate ACCOUNTS = UriTemplate.of("/accounts/{id}");

	private final PaymentService payments;
	private final MessageSourceAccessor messages;
	// tag::method[]

	@PutMapping
	ResponseEntity<?> issuePayment(@RequestBody PaymentRequest request) {

		PaymentResult result = payments.issuePayment(request.orderId, request.amount);

		if (result.isSuccess()) {
			return ResponseEntity.ok(result);
		}

		String title = messages.getMessage("payment.out-of-credit");
		String detail = messages.getMessage("payment.out-of-credit.details", //
				new Object[] { result.getBalance(), result.getCost() });

		Problem problem = Problem.create() // <1>
				.withType(OUT_OF_CREDIT_URI) //
				.withTitle(title) // <2>
				.withDetail(detail) //
				.withInstance(PAYMENT_ERROR_INSTANCE.expand(result.getPaymentId())) //
				.withProperties(map -> { // <3>
					map.put("balance", result.getBalance());
					map.put("accounts", Arrays.asList( //
							ACCOUNTS.expand(result.getSourceAccountId()), //
							ACCOUNTS.expand(result.getTargetAccountId()) //
					));
				});

		return ResponseEntity.status(HttpStatus.FORBIDDEN) //
				.body(problem);
	}
	// end::method[]

	ResponseEntity<?> issuePaymentAlternative() {

		Problem problem = Problem.create();
		PaymentResult result = new PaymentResult();

		// tag::alternative[]

		class AccountDetails {
			int balance;
			List<URI> accounts;
		}

		problem.withProperties(result.getDetails());

		// or

		Problem.create(result.getDetails());
		// end::alternative[]

		return null;
	}

	// tag::footer[]
}
// end::footer[]

class PaymentRequest {
	long orderId;
	int amount;
}
