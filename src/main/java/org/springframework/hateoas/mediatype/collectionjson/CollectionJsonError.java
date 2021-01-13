/*
 * Copyright 2018-2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import java.util.Objects;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Greg Turnquist
 */
final class CollectionJsonError {

	private final String title;
	private final String code;
	private final String message;

	@JsonCreator
	CollectionJsonError(@JsonProperty("title") @Nullable String title, @JsonProperty("code") @Nullable String code,
			@JsonProperty("message") @Nullable String message) {

		this.title = title;
		this.code = code;
		this.message = message;
	}

	CollectionJsonError() {
		this(null, null, null);
	}

	/**
	 * Create a new {@link CollectionJsonError} by copying the attributes and replacing the {@literal title}.
	 *
	 * @param title
	 * @return
	 */
	CollectionJsonError withTitle(String title) {
		return this.title == title ? this : new CollectionJsonError(title, this.code, this.message);
	}

	/**
	 * Create a new {@link CollectionJsonError} by copying the attributes and replacing the {@literal code}.
	 *
	 * @param code
	 * @return
	 */
	CollectionJsonError withCode(String code) {
		return this.code == code ? this : new CollectionJsonError(this.title, code, this.message);
	}

	/**
	 * Create a new {@link CollectionJsonError} by copying the attributes and replacing the {@literal message}.
	 *
	 * @param message
	 * @return
	 */
	CollectionJsonError withMessage(String message) {
		return this.message == message ? this : new CollectionJsonError(this.title, this.code, message);
	}

	public String getTitle() {
		return this.title;
	}

	public String getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CollectionJsonError that = (CollectionJsonError) o;
		return Objects.equals(this.title, that.title) && Objects.equals(this.code, that.code)
				&& Objects.equals(this.message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.title, this.code, this.message);
	}

	public String toString() {
		return "CollectionJsonError(title=" + this.title + ", code=" + this.code + ", message=" + this.message + ")";
	}
}
