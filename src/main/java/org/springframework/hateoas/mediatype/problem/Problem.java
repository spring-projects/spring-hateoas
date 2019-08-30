/*
 * Copyright 2019 the original author or authors.
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
import java.util.Objects;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulation of an RFC-7807 {@literal Problem} code. While it complies out-of-the-box, it may also be extended to
 * support domain-specific details.
 * 
 * @author Greg Turnquist
 */
public class Problem<T extends Problem<? extends T>> {

	private URI type;
	private String title;
	private HttpStatus status;
	private String detail;
	private URI instance;

	public Problem() {
		this(null, null, null, null, null);
	}

	public Problem(URI type, String title, HttpStatus status, String detail, URI instance) {

		this.type = type;
		this.title = title;
		this.status = status;
		this.detail = detail;
		this.instance = instance;
	}

	@JsonCreator
	public Problem(@JsonProperty("type") URI type, @JsonProperty("title") String title,
			@JsonProperty("status") int status, @JsonProperty("detail") String detail,
			@JsonProperty("instance") URI instance) {
		this(type, title, HttpStatus.resolve(status), detail, instance);
	}

	/**
	 * A {@link Problem} that reflects an {@link HttpStatus} code.
	 *
	 * @see https://tools.ietf.org/html/rfc7807#section-4.2
	 */
	public Problem(HttpStatus httpStatus) {
		this(URI.create("about:blank"), httpStatus.getReasonPhrase(), httpStatus, null, null);
	}

	@SuppressWarnings("unchecked")
	public T withType(URI type) {
		this.type = type;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withTitle(String title) {
		this.title = title;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withStatus(HttpStatus status) {
		this.status = status;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withDetail(String detail) {
		this.detail = detail;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T withInstance(URI instance) {
		this.instance = instance;
		return (T) this;
	}

	@JsonInclude(Include.NON_NULL)
	public URI getType() {
		return this.type;
	}

	@JsonInclude(Include.NON_NULL)
	public String getTitle() {
		return this.title;
	}

	@JsonInclude(Include.NON_NULL)
	public Integer getStatus() {
		if (status != null) {
			return status.value();
		}

		return null;
	}

	@JsonInclude(Include.NON_NULL)
	public String getDetail() {
		return detail;
	}

	@JsonInclude(Include.NON_NULL)
	public URI getInstance() {
		return instance;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Problem problem = (Problem) o;
		return Objects.equals(type, problem.type) && //
				Objects.equals(title, problem.title) && //
				status == problem.status && //
				Objects.equals(detail, problem.detail) && //
				Objects.equals(instance, problem.instance); //
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, title, status, detail, instance);
	}

	@Override
	public String toString() {

		return "Problem{" + //
				"type=" + type + //
				", title='" + title + '\'' + //
				", status=" + status + //
				", detail='" + detail + '\'' + //
				", instance=" + instance + //
				'}';
	}
}
