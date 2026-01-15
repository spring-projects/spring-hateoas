/*
 * Copyright 2019-2026 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;
import tools.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.mediatype.problem.Problem.ExtendedProblem;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson serialization tests for the HTTP Problem support.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class HttpProblemJacksonSerializationTest {

	ContextualMapper $ = MappingTestUtils.createMapper(builder -> builder.enable(SerializationFeature.INDENT_OUTPUT));

	@Test // #786
	void httpStatusProblemSerialize() throws IOException {

		var problem = Problem.statusOnly(HttpStatus.NOT_FOUND);

		$.assertSerializes(problem)
				.intoContentOf("http-status-problem.json")
				.andBack(actual -> {
					assertThat(actual.getType()).isEqualTo(URI.create("about:blank"));
					assertThat(actual.getTitle()).isEqualTo("Not Found");
					assertThat(actual.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
					assertThat(actual.getDetail()).isNull();
					assertThat(actual.getInstance()).isNull();
				});
	}

	@Test // #786
	void typeOnlySerialize() throws IOException {

		$.assertSerializes(Problem.create().withType(URI.create("http://example.com/problem-details")))
				.intoContentOf("type-only.json")
				.andBack(actual -> {
					assertThat(actual.getType()).isEqualTo(URI.create("http://example.com/problem-details"));
					assertThat(actual.getTitle()).isNull();
					assertThat(actual.getStatus()).isNull();
					assertThat(actual.getDetail()).isNull();
					assertThat(actual.getInstance()).isNull();
				});
	}

	@Test // #786
	void titleOnlySerialize() throws IOException {

		$.assertSerializes(Problem.create().withTitle("test title"))
				.intoContentOf("title-only.json")
				.andBack(actual -> {
					assertThat(actual.getType()).isNull();
					assertThat(actual.getTitle()).isEqualTo("test title");
					assertThat(actual.getStatus()).isNull();
					assertThat(actual.getDetail()).isNull();
					assertThat(actual.getInstance()).isNull();
				});
	}

	@Test // #786
	void statusOnlySerialize() throws IOException {

		$.assertSerializes(Problem.create().withStatus(HttpStatus.BAD_GATEWAY))
				.intoContentOf("status-only.json")
				.andBack(actual -> {
					assertThat(actual.getType()).isNull();
					assertThat(actual.getTitle()).isNull();
					assertThat(actual.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
					assertThat(actual.getDetail()).isNull();
					assertThat(actual.getInstance()).isNull();
				});
	}

	@Test // #786
	void detailOnlySerialize() throws IOException {

		$.assertSerializes(Problem.create().withDetail("test detail"))
				.intoContentOf("detail-only.json")
				.andBack(actual -> {
					assertThat(actual.getType()).isNull();
					assertThat(actual.getTitle()).isNull();
					assertThat(actual.getStatus()).isNull();
					assertThat(actual.getDetail()).isEqualTo("test detail");
					assertThat(actual.getInstance()).isNull();
				});
	}

	@Test // #786
	void instanceOnlySerialize() throws IOException {

		$.assertSerializes(Problem.create().withInstance(URI.create("http://example.com/employees/1471")))
				.intoContentOf("instance-only.json")
				.andBack(actual -> {
					assertThat(actual.getType()).isNull();
					assertThat(actual.getTitle()).isNull();
					assertThat(actual.getStatus()).isNull();
					assertThat(actual.getDetail()).isNull();
					assertThat(actual.getInstance()).isEqualTo(URI.create("http://example.com/employees/1471"));
				});
	}

	@Test // #786
	void extensionSerialize() throws IOException {

		var details = AccountProblemDetails.empty() //
				.withBalance(30) //
				.withAccounts("/account/12345", "/account/67890");

		var problem = Problem.create(details)
				.withType(URI.create("https://example.com/probs/out-of-credit")) //
				.withTitle("You do not have enough credit.") //
				.withStatus(HttpStatus.BAD_REQUEST) //
				.withDetail("Your current balance is 30, but that costs 50.") //
				.withInstance(URI.create("/account/12345/msgs/abc"));

		$.assertSerializes(problem)
				.intoContentOf("extension.json")
				.andBack(new ParameterizedTypeReference<ExtendedProblem<AccountProblemDetails>>() {})
				.matching(actual -> {

					assertThat(actual.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
					assertThat(actual.getTitle()).isEqualTo("You do not have enough credit.");
					assertThat(actual.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
					assertThat(actual.getDetail()).isEqualTo("Your current balance is 30, but that costs 50.");
					assertThat(actual.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));
					assertThat(actual.getProperties().getBalance()).isEqualTo(30);
					assertThat(actual.getProperties().getAccounts()).containsExactlyInAnyOrder("/account/12345",
							"/account/67890");
				});
	}

	@Test // #786
	void reference1Deserialize() throws IOException {

		$.assertDeserializesFile("reference-1.json")
				.into(new ParameterizedTypeReference<ExtendedProblem<AccountProblemDetails>>() {})
				.matching(result -> {

					assertThat(result.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
					assertThat(result.getTitle()).isEqualTo("You do not have enough credit.");
					assertThat(result.getDetail()).isEqualTo("Your current balance is 30, but that costs 50.");
					assertThat(result.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));

					var details = result.getProperties();

					assertThat(details.getBalance()).isEqualTo(30);
					assertThat(details.getAccounts()).containsExactlyInAnyOrder("/account/12345", "/account/67890");
				});
	}

	@Test // #786
	void reference2Deserialize() throws IOException {

		$.assertDeserializesFile("reference-2.json")
				.into(new ParameterizedTypeReference<ExtendedProblem<InvalidParameters>>() {})
				.matching(result -> {

					assertThat(result.getType()).isEqualTo(URI.create("https://example.net/validation-error"));
					assertThat(result.getTitle()).isEqualTo("Your request parameters didn't validate.");
					assertThat(result.getDetail()).isNull();
					assertThat(result.getInstance()).isNull();

					var parameters = result.getProperties().getInvalidParameters();

					assertThat(parameters).hasSize(2);
					assertThat(parameters).containsExactly( //
							new InvalidParameter("age", "must be a positive integer"), //
							new InvalidParameter("color", "must be 'green', 'red' or 'blue'") //
					);
				});
	}

	@Test // #786
	public void addsPropertiesViaCallback() {

		var problem = Problem.create() //
				.withStatus(HttpStatus.BAD_GATEWAY) //
				.withProperties(map -> {
					map.put("key", "value");
				});

		$.assertSerializes(problem).into(document -> {
			assertThat(document.read("$.status", int.class)).isEqualTo(502);
			assertThat(document.read("$.key", String.class)).isEqualTo("value");
		});

	}

	@Test // #786
	void deserializesIntoExtendedProblemWithMap() throws Exception {

		$.assertDeserializes("{ \"balance\" : 30, \"accounts\" : [ \"/first\", \"/second\" ] }")
				.into(new ParameterizedTypeReference<ExtendedProblem<Map<String, Object>>>() {})
				.matching(result -> {
					assertThat(result.getProperties()).containsEntry("balance", 30);
					assertThat(result.getProperties()).containsEntry("accounts", Arrays.asList("/first", "/second"));
				});
	}

	@Value
	@Getter(onMethod = @__(@JsonProperty))
	@NoArgsConstructor(force = true)
	static class Sample {
		String name;
	}

	/**
	 * First reference domain definition.
	 *
	 * @see https://tools.ietf.org/html/rfc7807#section-3
	 */
	@Value
	@Getter(onMethod = @__(@JsonProperty))
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@JsonCreator))
	@NoArgsConstructor(staticName = "empty", force = true)
	@With
	private static class AccountProblemDetails {

		int balance;
		String[] accounts;

		public AccountProblemDetails withAccounts(String... accounts) {
			return new AccountProblemDetails(balance, accounts);
		}
	}

	/**
	 * Second reference domain definition.
	 *
	 * @see https://tools.ietf.org/html/rfc7807#section-3
	 */
	@JsonAutoDetect
	private static class InvalidParameters {

		private List<InvalidParameter> invalidParameters;

		@JsonCreator
		public InvalidParameters(@JsonProperty("invalid-params") List<InvalidParameter> invalidParameters) {

			super();
			this.invalidParameters = invalidParameters;
		}

		public List<InvalidParameter> getInvalidParameters() {
			return invalidParameters;
		}
	}

	@Data
	@JsonAutoDetect
	private static class InvalidParameter {

		private String name;
		private String reason;

		@JsonCreator
		InvalidParameter(@JsonProperty("name") String name, @JsonProperty("reason") String reason) {

			this.name = name;
			this.reason = reason;
		}
	}
}
