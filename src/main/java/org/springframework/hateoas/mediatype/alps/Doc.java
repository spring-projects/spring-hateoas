/*
 * Copyright 2014-2024 the original author or authors.
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
package org.springframework.hateoas.mediatype.alps;

import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A value object for an ALPS doc element.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @since 0.15
 * @see http://alps.io/spec/#prop-doc
 */
@JsonPropertyOrder({ "format", "href", "value" })
@JsonInclude(JsonInclude.Include.NON_NULL)
@NullUnmarked
public final class Doc {

	private final String href;
	private final String value;
	private final Format format;

	/**
	 * Creates a new {@link Doc} instance with the given value and {@link Format}.
	 *
	 * @param value must not be {@literal null} or empty.
	 * @param format must not be {@literal null}.
	 */
	public Doc(@NonNull String value, @NonNull Format format) {

		Assert.hasText(value, "Value must not be null or empty!");
		Assert.notNull(format, "Format must not be null!");

		this.href = null;
		this.value = value;
		this.format = format;
	}

	@JsonCreator
	private Doc(@JsonProperty("href") String href, @JsonProperty("value") String value,
			@JsonProperty("format") Format format) {

		this.href = href;
		this.value = value;
		this.format = format;
	}

	public static DocBuilder builder() {
		return new DocBuilder();
	}

	@JsonProperty
	public String getHref() {
		return this.href;
	}

	@JsonProperty
	public String getValue() {
		return this.value;
	}

	@JsonProperty
	public Format getFormat() {
		return this.format;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Doc that)) {
			return false;
		}

		return Objects.equals(this.href, that.href)
				&& Objects.equals(this.value, that.value)
				&& this.format == that.format;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.href, this.value, this.format);
	}

	@Override
	public String toString() {
		return "Doc(href=" + this.href + ", value=" + this.value + ", format=" + this.format + ")";
	}

	public static class DocBuilder {

		private String href;
		private String value;
		private Format format;

		DocBuilder() {}

		public Doc.DocBuilder href(@NonNull String href) {

			this.href = href;
			return this;
		}

		public Doc.DocBuilder value(@NonNull String value) {

			this.value = value;
			return this;
		}

		public Doc.DocBuilder format(@NonNull Format format) {

			this.format = format;
			return this;
		}

		public Doc build() {
			return new Doc(this.href, this.value, this.format);
		}

		@Override
		public String toString() {
			return "Doc.DocBuilder(href=" + this.href + ", value=" + this.value + ", format=" + this.format + ")";
		}
	}
}
