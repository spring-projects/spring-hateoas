/*
 * Copyright 2014-2016 the original author or authors.
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
@Value
@Builder
@JsonPropertyOrder({ "id", "href", "name", "type", "doc", "descriptor", "ext" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Descriptor {

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
}
