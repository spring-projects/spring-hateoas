/*
 * Copyright 2014-2021 the original author or authors.
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A value object for an ALPS ext element.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @since 0.15
 * @see http://alps.io/spec/#prop-ext
 */
@JsonPropertyOrder({ "id", "href", "value" })
public final class Ext {

	private final String id;
	private final String href;
	private final String value;

	@JsonCreator
	private Ext(@JsonProperty("id") String id, @JsonProperty("href") String href, @JsonProperty("value") String value) {

		this.id = id;
		this.href = href;
		this.value = value;
	}

	public static ExtBuilder builder() {
		return new ExtBuilder();
	}

	public String getId() {
		return this.id;
	}

	public String getHref() {
		return this.href;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Ext ext = (Ext) o;
		return Objects.equals(this.id, ext.id) && Objects.equals(this.href, ext.href)
				&& Objects.equals(this.value, ext.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.href, this.value);
	}

	public String toString() {
		return "Ext(id=" + this.id + ", href=" + this.href + ", value=" + this.value + ")";
	}

	public static class ExtBuilder {

		private String id;
		private String href;
		private String value;

		ExtBuilder() {}

		public Ext.ExtBuilder id(String id) {

			this.id = id;
			return this;
		}

		public Ext.ExtBuilder href(String href) {

			this.href = href;
			return this;
		}

		public Ext.ExtBuilder value(String value) {

			this.value = value;
			return this;
		}

		public Ext build() {
			return new Ext(this.id, this.href, this.value);
		}

		public String toString() {
			return "Ext.ExtBuilder(id=" + this.id + ", href=" + this.href + ", value=" + this.value + ")";
		}
	}
}
