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

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A value object for an ALPS descriptor.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @since 0.15
 * @see http://alps.io/spec/#prop-descriptor
 */
@JsonPropertyOrder({ "id", "href", "name", "type", "doc", "descriptor", "ext" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Descriptor {

	private final String id;
	private final String href;
	private final String name;
	private final Doc doc;
	private final Type type;
	private final Ext ext;
	private final String rt;
	private final List<Descriptor> descriptor;

	@JsonCreator
	private Descriptor(@JsonProperty("id") String id, @JsonProperty("href") String href,
			@JsonProperty("name") String name, @JsonProperty("doc") Doc doc, @JsonProperty("type") Type type,
			@JsonProperty("ext") Ext ext, @JsonProperty("rt") String rt,
			@JsonProperty("descriptor") List<Descriptor> descriptor) {

		this.id = id;
		this.href = href;
		this.name = name;
		this.doc = doc;
		this.type = type;
		this.ext = ext;
		this.rt = rt;
		this.descriptor = descriptor;
	}

	public static DescriptorBuilder builder() {
		return new DescriptorBuilder();
	}

	public String getId() {
		return this.id;
	}

	public String getHref() {
		return this.href;
	}

	public String getName() {
		return this.name;
	}

	public Doc getDoc() {
		return this.doc;
	}

	public Type getType() {
		return this.type;
	}

	public Ext getExt() {
		return this.ext;
	}

	public String getRt() {
		return this.rt;
	}

	public List<Descriptor> getDescriptor() {
		return this.descriptor;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Descriptor that = (Descriptor) o;
		return Objects.equals(this.id, that.id) && Objects.equals(this.href, that.href)
				&& Objects.equals(this.name, that.name) && Objects.equals(this.doc, that.doc) && this.type == that.type
				&& Objects.equals(this.ext, that.ext) && Objects.equals(this.rt, that.rt)
				&& Objects.equals(this.descriptor, that.descriptor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.href, this.name, this.doc, this.type, this.ext, this.rt, this.descriptor);
	}

	public String toString() {

		return "Descriptor(id=" + this.id + ", href=" + this.href + ", name=" + this.name + ", doc=" + this.doc + ", type="
				+ this.type + ", ext=" + this.ext + ", rt=" + this.rt + ", descriptor=" + this.descriptor + ")";
	}

	public static class DescriptorBuilder {

		private String id;
		private String href;
		private String name;
		private Doc doc;
		private Type type;
		private Ext ext;
		private String rt;
		private List<Descriptor> descriptor;

		DescriptorBuilder() {}

		public Descriptor.DescriptorBuilder id(String id) {

			this.id = id;
			return this;
		}

		public Descriptor.DescriptorBuilder href(String href) {

			this.href = href;
			return this;
		}

		public Descriptor.DescriptorBuilder name(String name) {

			this.name = name;
			return this;
		}

		public Descriptor.DescriptorBuilder doc(Doc doc) {

			this.doc = doc;
			return this;
		}

		public Descriptor.DescriptorBuilder type(Type type) {

			this.type = type;
			return this;
		}

		public Descriptor.DescriptorBuilder ext(Ext ext) {

			this.ext = ext;
			return this;
		}

		public Descriptor.DescriptorBuilder rt(String rt) {

			this.rt = rt;
			return this;
		}

		public Descriptor.DescriptorBuilder descriptor(List<Descriptor> descriptor) {

			this.descriptor = descriptor;
			return this;
		}

		public Descriptor build() {
			return new Descriptor(this.id, this.href, this.name, this.doc, this.type, this.ext, this.rt, this.descriptor);
		}

		public String toString() {

			return "Descriptor.DescriptorBuilder(id=" + this.id + ", href=" + this.href + ", name=" + this.name + ", doc="
					+ this.doc + ", type=" + this.type + ", ext=" + this.ext + ", rt=" + this.rt + ", descriptor="
					+ this.descriptor + ")";
		}
	}
}
