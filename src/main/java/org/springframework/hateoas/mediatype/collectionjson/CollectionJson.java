/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.mediatype.collectionjson;

import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;

import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of the "collection" part of a Collection+JSON document.
 *
 * @author Greg Turnquist
 */
@Value
@Wither(AccessLevel.PACKAGE)
class CollectionJson<T> {

	private String version;
	private String href;
	
	@JsonInclude(Include.NON_EMPTY)
	private List<Link> links;

	@JsonInclude(Include.NON_EMPTY)
	private List<CollectionJsonItem<T>> items;

	@JsonInclude(Include.NON_EMPTY)
	private List<CollectionJsonQuery> queries;

	@JsonInclude(Include.NON_NULL)
	private CollectionJsonTemplate template;

	@JsonInclude(Include.NON_NULL)
	private CollectionJsonError error;

	@JsonCreator
	CollectionJson(@JsonProperty("version") String version, @JsonProperty("href") String href,
				   @JsonProperty("links") List<Link> links, @JsonProperty("items") List<CollectionJsonItem<T>> items,
				   @JsonProperty("queries") List<CollectionJsonQuery> queries,
				   @JsonProperty("template") CollectionJsonTemplate template,
				   @JsonProperty("error") CollectionJsonError error) {

		this.version = version;
		this.href = href;
		this.links = links;
		this.items = items;
		this.queries = queries;
		this.template = template;
		this.error = error;
	}

	CollectionJson() {
		this("1.0", null, null, null, null, null, null);
	}
}
