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

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * @author Oliver Drotbohm
 */
class PaymentResult {

	UUID getPaymentId() {
		return UUID.randomUUID();
	}

	boolean isSuccess() {
		return false;
	}

	int getBalance() {
		return 30;
	}

	UUID getSourceAccountId() {
		return UUID.randomUUID();
	}

	UUID getTargetAccountId() {
		return UUID.randomUUID();
	}

	AccountDetails getDetails() {
		return new AccountDetails();
	}

	static class AccountDetails {
		int balance;
		List<URI> accounts;
	}

	int getCost() {
		return 50;
	}
}
