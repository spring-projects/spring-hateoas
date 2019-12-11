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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Encapsulation of an RFC-7807 {@literal Problem} code. While it complies out-of-the-box, it may also be extended to
 * support domain-specific details.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@Getter(onMethod = @__(@JsonProperty))
@Wither
@ToString
@EqualsAndHashCode
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Problem {

	private static Problem EMPTY = new Problem();

	private final @Nullable URI type;
	private final @Nullable String title;
	private final @Nullable @Getter(onMethod = @__(@JsonIgnore)) HttpStatus status;
	private final @Nullable String detail;
	private final @Nullable URI instance;

	@JsonCreator
	public Problem(@JsonProperty("type") URI type, @JsonProperty("title") String title,
			@JsonProperty("status") int status, @JsonProperty("detail") String detail,
			@JsonProperty("instance") URI instance) {

		this(type, title, HttpStatus.resolve(status), detail, instance);
	}

	/**
	 * Returns an empty {@link Problem} instance.
	 *
	 * @return an empty {@link Problem} instance.
	 */
	public static Problem create() {
		return EMPTY;
	}

	/**
	 * Returns an {@link ExtendedProblem} with the given payload as additional properties.
	 *
	 * @param <T>
	 * @param payload must not be {@literal null}.
	 * @return
	 */
	public static <T> ExtendedProblem<T> create(T payload) {

		Assert.notNull(payload, "Payload must not be null!");

		return EMPTY.withProperties(payload);
	}

	/**
	 * Returns a {@link Problem} instance with the given {@link HttpStatus} and defaults as defined in
	 * <a href="https://tools.ietf.org/html/rfc7807#section-4.2">RFC7807</a>.
	 *
	 * @param status must not be {@literal null}.
	 * @return
	 * @see <a href="https://tools.ietf.org/html/rfc7807#section-4.2">RFC7807</a>
	 */
	public static Problem statusOnly(HttpStatus status) {

		Assert.notNull(status, "HttpStatus must not be null!");

		return new Problem(URI.create("about:blank"), status.getReasonPhrase(), status, null, null);
	}

	/**
	 * Creates a new {@link ExtendedProblem} with the given payload as additional properties.
	 *
	 * @param <T>
	 * @param payload must not be {@literal null}.
	 * @return
	 */
	public <T> ExtendedProblem<T> withProperties(T payload) {
		return new ExtendedProblem<>(type, title, status, detail, instance, payload);
	}

	/**
	 * Returns an {@link ExtendedProblem} with a {@link Map<String, Object>} populated by the given consumer as payload.
	 *
	 * @param consumer must not be {@literal null}.
	 * @return
	 */
	public ExtendedProblem<Map<String, Object>> withProperties(Consumer<Map<String, Object>> consumer) {

		Assert.notNull(consumer, "Consumer must not be null!");

		Map<String, Object> map = new HashMap<>();
		consumer.accept(map);

		return withProperties(map);
	}

	/**
	 * Returns an {@link ExtendedProblem} with the given {@link Map} unwrapping as additional properties.
	 *
	 * @param properties must not be {@literal null}.
	 * @return
	 */
	public ExtendedProblem<Map<String, Object>> withProperties(Map<String, Object> properties) {

		Assert.notNull(properties, "Properties must not be null!");

		return new ExtendedProblem<Map<String, Object>>(type, title, status, detail, instance, properties);
	}

	@Nullable
	@JsonProperty("status")
	@JsonInclude(Include.NON_NULL)
	Integer getStatusAsInteger() {
		return status != null ? status.value() : null;
	}

	@Value
	@Getter(onMethod = @__(@JsonIgnore))
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
	public static class ExtendedProblem<T> extends Problem {

		private @NonFinal T extendedProperties;

		ExtendedProblem(@Nullable URI type, @Nullable String title, @Nullable HttpStatus status, @Nullable String detail,
				@Nullable URI instance, @Nullable T properties) {

			super(type, title, status, detail, instance);

			this.extendedProperties = properties;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.problem.Problem#withType(java.net.URI)
		 */
		@Override
		public ExtendedProblem<T> withType(@Nullable URI type) {
			return new ExtendedProblem<>(type, getTitle(), getStatus(), getDetail(), getInstance(), extendedProperties);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.problem.Problem#withTitle(java.lang.String)
		 */
		@Override
		public ExtendedProblem<T> withTitle(@Nullable String title) {
			return new ExtendedProblem<>(getType(), title, getStatus(), getDetail(), getInstance(), extendedProperties);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.problem.Problem#withDetail(java.lang.String)
		 */
		@Override
		public ExtendedProblem<T> withDetail(@Nullable String detail) {
			return new ExtendedProblem<>(getType(), getTitle(), getStatus(), detail, getInstance(), extendedProperties);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.problem.Problem#withInstance(java.net.URI)
		 */
		@Override
		public ExtendedProblem<T> withInstance(@Nullable URI instance) {
			return new ExtendedProblem<>(getType(), getTitle(), getStatus(), getDetail(), instance, extendedProperties);
		}

		/**
		 * Returns the additional properties.
		 *
		 * @return
		 */
		@JsonIgnore
		public T getProperties() {
			return extendedProperties;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.problem.Problem#withProperties(java.lang.Object)
		 */
		@Override
		public <S> ExtendedProblem<S> withProperties(S payload) {
			return super.withProperties(payload);
		}

		// Payload type based serialization

		@Nullable
		@JsonUnwrapped
		T getExtendedProperties() {
			return Map.class.isInstance(extendedProperties) ? null : extendedProperties;
		}

		// Map based serialization

		@Nullable
		@JsonAnyGetter
		@SuppressWarnings("unchecked")
		Map<String, Object> getPropertiesAsMap() {
			return Map.class.isInstance(extendedProperties) ? (Map<String, Object>) extendedProperties : null;
		}

		// Map based deserialization

		@JsonAnySetter
		void setPropertiesAsMap(String key, Object value) {
			getOrInitAsMap().put(key, value);
		}

		@SuppressWarnings("unchecked")
		private Map<String, Object> getOrInitAsMap() {

			if (this.extendedProperties == null) {
				this.extendedProperties = (T) new LinkedHashMap<>();
			}

			return (Map<String, Object>) this.extendedProperties;
		}
	}
}
