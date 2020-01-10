/*
 * Copyright 2014-2020 the original author or authors.
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

import lombok.Builder;
import lombok.Value;

import java.util.List;

import org.springframework.hateoas.mediatype.alps.Descriptor.DescriptorBuilder;
import org.springframework.hateoas.mediatype.alps.Doc.DocBuilder;
import org.springframework.hateoas.mediatype.alps.Ext.ExtBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * An ALPS document.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @since 0.15
 * @see http://alps.io
 * @see http://alps.io/spec/#prop-alps
 */
@Value
@Builder(builderMethodName = "alps")
@JsonPropertyOrder({ "version", "doc", "descriptor" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Alps {

	private final String version;
	private final Doc doc;
	private final List<Descriptor> descriptor;

	@JsonCreator
	private Alps(@JsonProperty("version") String version, @JsonProperty("doc") Doc doc,
			@JsonProperty("descriptor") List<Descriptor> descriptor) {

		this.version = "1.0";
		this.doc = doc;
		this.descriptor = descriptor;
	}

	/**
	 * Returns a new {@link DescriptorBuilder}.
	 * 
	 * @return
	 */
	public static DescriptorBuilder descriptor() {
		return Descriptor.builder();
	}

	/**
	 * Returns a new {@link DocBuilder}.
	 * 
	 * @return
	 */
	public static DocBuilder doc() {
		return Doc.builder();
	}

	/**
	 * Returns a new {@link ExtBuilder}.
	 * 
	 * @return
	 */
	public static ExtBuilder ext() {
		return Ext.builder();
	}
}
