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
package org.springframework.hateoas.mediatype.problem;

import static org.assertj.core.api.Assertions.*;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.mediatype.problem.Problem.ExtendedProblem;
import org.springframework.hateoas.support.MappingUtils;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * Jackson serialization tests for the HTTP Problem support.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
class JacksonSerializationTest {

	ObjectMapper mapper;

	@BeforeEach
	void setUp() {

		this.mapper = MappingTestUtils.defaultObjectMapper();
		this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@Test // #786
	void httpStatusProblemSerialize() throws IOException {

		Problem problem = Problem.statusOnly(HttpStatus.NOT_FOUND);

		String actual = this.mapper.writeValueAsString(problem);

		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("http-status-problem.json", getClass())));
	}

	@Test // #786
	void httpStatusProblemDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("http-status-problem.json", getClass()));

		Problem actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isEqualTo(URI.create("about:blank"));
		assertThat(actual.getTitle()).isEqualTo("Not Found");
		assertThat(actual.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(actual.getDetail()).isNull();
		assertThat(actual.getInstance()).isNull();
	}

	@Test // #786
	void typeOnlySerialize() throws IOException {

		Problem problem = new Problem().withType(URI.create("http://example.com/problem-details"));

		String actual = this.mapper.writeValueAsString(problem);
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("type-only.json", getClass())));
	}

	@Test // #786
	void typeOnlyDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("type-only.json", getClass()));

		Problem actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isEqualTo(URI.create("http://example.com/problem-details"));
		assertThat(actual.getTitle()).isNull();
		assertThat(actual.getStatus()).isNull();
		assertThat(actual.getDetail()).isNull();
		assertThat(actual.getInstance()).isNull();
	}

	@Test // #786
	void titleOnlySerialize() throws IOException {

		Problem problem = new Problem().withTitle("test title");

		String actual = this.mapper.writeValueAsString(problem);
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("title-only.json", getClass())));
	}

	@Test // #786
	void titleOnlyDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("title-only.json", getClass()));

		Problem actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isNull();
		assertThat(actual.getTitle()).isEqualTo("test title");
		assertThat(actual.getStatus()).isNull();
		assertThat(actual.getDetail()).isNull();
		assertThat(actual.getInstance()).isNull();
	}

	@Test // #786
	void statusOnlySerialize() throws IOException {

		Problem problem = new Problem().withStatus(HttpStatus.BAD_GATEWAY);

		String actual = this.mapper.writeValueAsString(problem);
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("status-only.json", getClass())));
	}

	@Test // #786
	void statusOnlyDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("status-only.json", getClass()));

		Problem actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isNull();
		assertThat(actual.getTitle()).isNull();
		assertThat(actual.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
		assertThat(actual.getDetail()).isNull();
		assertThat(actual.getInstance()).isNull();
	}

	@Test // #786
	void detailOnlySerialize() throws IOException {

		Problem problem = Problem.create().withDetail("test detail");

		assertThat(this.mapper.writeValueAsString(problem)) //
				.isEqualTo(MappingUtils.read(new ClassPathResource("detail-only.json", getClass())));
	}

	@Test // #786
	void detailOnlyDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("detail-only.json", getClass()));

		Problem actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isNull();
		assertThat(actual.getTitle()).isNull();
		assertThat(actual.getStatus()).isNull();
		assertThat(actual.getDetail()).isEqualTo("test detail");
		assertThat(actual.getInstance()).isNull();
	}

	@Test // #786
	void instanceOnlySerialize() throws IOException {

		Problem problem = Problem.create() //
				.withInstance(URI.create("http://example.com/employees/1471"));

		assertThat(mapper.writeValueAsString(problem)) //
				.isEqualTo(MappingUtils.read(new ClassPathResource("instance-only.json", getClass())));
	}

	@Test // #786
	void instanceOnlyDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("instance-only.json", getClass()));

		Problem actual = mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isNull();
		assertThat(actual.getTitle()).isNull();
		assertThat(actual.getStatus()).isNull();
		assertThat(actual.getDetail()).isNull();
		assertThat(actual.getInstance()).isEqualTo(URI.create("http://example.com/employees/1471"));
	}

	@Test // #786
	void extensionSerialize() throws IOException {

		AccountProblemDetails details = AccountProblemDetails.empty() //
				.withBalance(30) //
				.withAccounts("/account/12345", "/account/67890");

		ExtendedProblem<AccountProblemDetails> problem = Problem.create(details)
				.withType(URI.create("https://example.com/probs/out-of-credit")) //
				.withTitle("You do not have enough credit.") //
				.withDetail("Your current balance is 30, but that costs 50.") //
				.withInstance(URI.create("/account/12345/msgs/abc"));

		String actual = this.mapper.writeValueAsString(problem);

		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("extension.json", getClass())));
	}

	@Test // #786
	void extensionDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("extension.json", getClass()));

		JavaType type = this.mapper.getTypeFactory().constructParametricType(ExtendedProblem.class,
				AccountProblemDetails.class);

		ExtendedProblem<AccountProblemDetails> actual = this.mapper.readValue(expected, type);

		assertThat(actual.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
		assertThat(actual.getTitle()).isEqualTo("You do not have enough credit.");
		assertThat(actual.getStatus()).isNull();
		assertThat(actual.getDetail()).isEqualTo("Your current balance is 30, but that costs 50.");
		assertThat(actual.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));
		assertThat(actual.getProperties().getBalance()).isEqualTo(30);
		assertThat(actual.getProperties().getAccounts()).containsExactlyInAnyOrder("/account/12345", "/account/67890");
	}

	@Test // #786
	void reference1Deserialize() throws IOException {

		JavaType type = mapper.getTypeFactory().constructParametricType(ExtendedProblem.class, AccountProblemDetails.class);

		ExtendedProblem<AccountProblemDetails> accountProblem = this.mapper
				.readValue(MappingUtils.read(new ClassPathResource("reference-1.json", getClass())), type);

		assertThat(accountProblem.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
		assertThat(accountProblem.getTitle()).isEqualTo("You do not have enough credit.");
		assertThat(accountProblem.getDetail()).isEqualTo("Your current balance is 30, but that costs 50.");
		assertThat(accountProblem.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));

		AccountProblemDetails details = accountProblem.getProperties();

		assertThat(details.getBalance()).isEqualTo(30);
		assertThat(details.getAccounts()).containsExactlyInAnyOrder("/account/12345", "/account/67890");
	}

	@Test // #786
	void reference2Deserialize() throws IOException {

		JavaType type = mapper.getTypeFactory().constructParametricType(ExtendedProblem.class, InvalidParameters.class);

		ExtendedProblem<InvalidParameters> invalidParameters = this.mapper
				.readValue(MappingUtils.read(new ClassPathResource("reference-2.json", getClass())), type);

		assertThat(invalidParameters.getType()).isEqualTo(URI.create("https://example.net/validation-error"));
		assertThat(invalidParameters.getTitle()).isEqualTo("Your request parameters didn't validate.");
		assertThat(invalidParameters.getDetail()).isNull();
		assertThat(invalidParameters.getInstance()).isNull();

		List<InvalidParameter> parameters = invalidParameters.getProperties().getInvalidParameters();

		assertThat(parameters).hasSize(2);
		assertThat(parameters).containsExactly( //
				new InvalidParameter("age", "must be a positive integer"), //
				new InvalidParameter("color", "must be 'green', 'red' or 'blue'") //
		);
	}

	@Test // #786
	public void addsPropertiesViaCallback() throws JsonProcessingException {

		ExtendedProblem<Map<String, Object>> problem = Problem.create() //
				.withStatus(HttpStatus.BAD_GATEWAY) //
				.withProperties(map -> {
					map.put("key", "value");
				});

		DocumentContext parse = JsonPath.parse(mapper.writeValueAsString(problem));

		assertThat(parse.read("$.status", int.class)).isEqualTo(502);
		assertThat(parse.read("$.key", String.class)).isEqualTo("value");
	}

	@Test // #786
	void deserializesIntoExtendedProblemWithMap() throws Exception {

		TypeFactory factory = mapper.getTypeFactory();

		JavaType mapType = factory.constructParametricType(Map.class, String.class, Object.class);
		JavaType problemType = factory.constructParametricType(ExtendedProblem.class, mapType);

		ExtendedProblem<Map<String, Object>> result = mapper
				.readValue("{ \"balance\" : 30, \"accounts\" : [ \"/first\", \"/second\" ] }", problemType);

		assertThat(result.getProperties()).containsEntry("balance", 30);
		assertThat(result.getProperties()).containsEntry("accounts", Arrays.asList("/first", "/second"));
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
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@NoArgsConstructor(staticName = "empty", force = true)
	@Wither
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
