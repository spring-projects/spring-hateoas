package org.springframework.hateoas.mediatype.problem;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.support.MappingUtils;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 */
class JacksonSerializationTest {

	ObjectMapper mapper;

	@BeforeEach
	void setUp() {

		this.mapper = new ObjectMapper();
		this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@Test
	void httpStatusProblemSerialize() throws IOException {

		Problem problem = new Problem(HttpStatus.NOT_FOUND);

		String actual = this.mapper.writeValueAsString(problem);
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("http-status-problem.json", getClass())));
	}

	@Test
	void httpStatusProblemDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("http-status-problem.json", getClass()));

		Problem<?> actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isEqualTo(URI.create("about:blank"));
		assertThat(actual.getTitle()).isEqualTo("Not Found");
		assertThat(actual.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
		assertThat(actual.getDetail()).isNull();
		assertThat(actual.getInstance()).isNull();
	}

	@Test
	void typeOnlySerialize() throws IOException {

		Problem problem = new Problem().withType(URI.create("http://example.com/problem-details"));

		String actual = this.mapper.writeValueAsString(problem);
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("type-only.json", getClass())));
	}

	@Test
	void typeOnlyDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("type-only.json", getClass()));

		Problem<?> actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isEqualTo(URI.create("http://example.com/problem-details"));
		assertThat(actual.getTitle()).isNull();
		assertThat(actual.getStatus()).isNull();
		assertThat(actual.getDetail()).isNull();
		assertThat(actual.getInstance()).isNull();
	}

	@Test
	void titleOnlySerialize() throws IOException {

		Problem problem = new Problem().withTitle("test title");

		String actual = this.mapper.writeValueAsString(problem);
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("title-only.json", getClass())));
	}

	@Test
	void titleOnlyDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("title-only.json", getClass()));

		Problem<?> actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isNull();
		assertThat(actual.getTitle()).isEqualTo("test title");
		assertThat(actual.getStatus()).isNull();
		assertThat(actual.getDetail()).isNull();
		assertThat(actual.getInstance()).isNull();
	}

	@Test
	void statusOnlySerialize() throws IOException {

		Problem problem = new Problem().withStatus(HttpStatus.BAD_GATEWAY);

		String actual = this.mapper.writeValueAsString(problem);
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("status-only.json", getClass())));
	}

	@Test
	void statusOnlyDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("status-only.json", getClass()));

		Problem<?> actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isNull();
		assertThat(actual.getTitle()).isNull();
		assertThat(actual.getStatus()).isEqualTo(502);
		assertThat(actual.getDetail()).isNull();
		assertThat(actual.getInstance()).isNull();
	}

	@Test
	void detailOnlySerialize() throws IOException {

		Problem problem = new Problem().withDetail("test detail");

		String actual = this.mapper.writeValueAsString(problem);
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("detail-only.json", getClass())));
	}

	@Test
	void detailOnlyDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("detail-only.json", getClass()));

		Problem<?> actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isNull();
		assertThat(actual.getTitle()).isNull();
		assertThat(actual.getStatus()).isNull();
		assertThat(actual.getDetail()).isEqualTo("test detail");
		assertThat(actual.getInstance()).isNull();
	}

	@Test
	void instanceOnlySerialize() throws IOException {

		Problem problem = new Problem().withInstance(URI.create("http://example.com/employees/1471"));

		String actual = this.mapper.writeValueAsString(problem);
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("instance-only.json", getClass())));
	}

	@Test
	void instanceOnlyDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("instance-only.json", getClass()));

		Problem<?> actual = this.mapper.readValue(expected, Problem.class);

		assertThat(actual.getType()).isNull();
		assertThat(actual.getTitle()).isNull();
		assertThat(actual.getStatus()).isNull();
		assertThat(actual.getDetail()).isNull();
		assertThat(actual.getInstance()).isEqualTo(URI.create("http://example.com/employees/1471"));
	}

	@Test
	void extensionSerialize() throws IOException {

		AccountProblem problem = new AccountProblem() //
				.withType(URI.create("https://example.com/probs/out-of-credit")) //
				.withTitle("You do not have enough credit.") //
				.withDetail("Your current balance is 30, but that costs 50.") //
				.withInstance(URI.create("/account/12345/msgs/abc")) //
				.withBalance(30) //
				.withAccounts("/account/12345", "/account/67890");

		String actual = this.mapper.writeValueAsString(problem);
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("extension.json", getClass())));
	}

	@Test
	void extensionDeserialize() throws IOException {

		String expected = MappingUtils.read(new ClassPathResource("extension.json", getClass()));

		AccountProblem actual = this.mapper.readValue(expected, AccountProblem.class);

		assertThat(actual.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
		assertThat(actual.getTitle()).isEqualTo("You do not have enough credit.");
		assertThat(actual.getStatus()).isNull();
		assertThat(actual.getDetail()).isEqualTo("Your current balance is 30, but that costs 50.");
		assertThat(actual.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));
		assertThat(actual.getBalance()).isEqualTo(30);
		assertThat(actual.getAccounts()).containsExactlyInAnyOrder("/account/12345", "/account/67890");
	}

	@Test
	void reference1Deserialize() throws IOException {

		AccountProblem accountProblem = this.mapper
				.readValue(MappingUtils.read(new ClassPathResource("reference-1.json", getClass())), AccountProblem.class);

		assertThat(accountProblem.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
		assertThat(accountProblem.getTitle()).isEqualTo("You do not have enough credit.");
		assertThat(accountProblem.getDetail()).isEqualTo("Your current balance is 30, but that costs 50.");
		assertThat(accountProblem.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));
		assertThat(accountProblem.getBalance()).isEqualTo(30);
		assertThat(accountProblem.getAccounts()).containsExactlyInAnyOrder("/account/12345", "/account/67890");
	}

	@Test
	void reference2Deserialize() throws IOException {

		InvalidParameters invalidParameters = this.mapper
				.readValue(MappingUtils.read(new ClassPathResource("reference-2.json", getClass())), InvalidParameters.class);

		assertThat(invalidParameters.getType()).isEqualTo(URI.create("https://example.net/validation-error"));
		assertThat(invalidParameters.getTitle()).isEqualTo("Your request parameters didn't validate.");
		assertThat(invalidParameters.getDetail()).isNull();
		assertThat(invalidParameters.getInstance()).isNull();
		assertThat(invalidParameters.getInvalidParameters()).hasSize(2);
		assertThat(invalidParameters.getInvalidParameters()).containsExactly(
				new InvalidParameter("age", "must be a positive integer"),
				new InvalidParameter("color", "must be 'green', 'red' or 'blue'"));
	}

	/**
	 * First reference domain definition.
	 *
	 * @see https://tools.ietf.org/html/rfc7807#section-3
	 */
	private static class AccountProblem extends Problem<AccountProblem> {

		private int balance;
		private String[] accounts;

		AccountProblem() {
			super();
		}

		AccountProblem withBalance(int balance) {

			this.balance = balance;
			return this;
		}

		AccountProblem withAccounts(String... accounts) {

			this.accounts = accounts;
			return this;
		}

		public int getBalance() {
			return this.balance;
		}

		public String[] getAccounts() {
			return this.accounts;
		}
	}

	/**
	 * Second reference domain definition.
	 *
	 * @see https://tools.ietf.org/html/rfc7807#section-3
	 */
	private static class InvalidParameters extends Problem<InvalidParameters> {

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

	private static class InvalidParameter {

		private String name;
		private String reason;

		InvalidParameter() {}

		InvalidParameter(String name, String reason) {

			this.name = name;
			this.reason = reason;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

		@Override
		public boolean equals(Object o) {

			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			InvalidParameter that = (InvalidParameter) o;
			return Objects.equals(name, that.name) && Objects.equals(reason, that.reason);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, reason);
		}

		@Override
		public String toString() {
			return "InvalidParameter{" + //
					"name='" + name + '\'' + //
					", reason='" + reason + '\'' + //
					'}';
		}
	}
}
