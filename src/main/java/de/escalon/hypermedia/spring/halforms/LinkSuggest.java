/*
 * Copyright 2016 the original author or authors.
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
package de.escalon.hypermedia.spring.halforms;

import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Suggested values of a {@link Property} that are loaded by a url returning a list of values.
 */
@JsonPropertyOrder({ "link", "valueField", "textField" })
public class LinkSuggest extends AbstractSuggest {

	@JsonSerialize(using = LinkSuggestSerializer.class)
	@JsonProperty("href")
	private Link link;

	public LinkSuggest(Link link, String textFieldName, String valueFieldName) {
		super(textFieldName, valueFieldName);
		this.link = link;
	}

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

}
