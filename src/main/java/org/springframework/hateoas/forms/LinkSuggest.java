package org.springframework.hateoas.forms;

import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Suggested values of a {@link Property} that are loaded by a url returning a list of values.
 * 
 */
public class LinkSuggest extends AbstractSuggest {

	@JsonSerialize(using = LinkSuggestSerializer.class)
	@JsonProperty("href")
	private final Link link;

	public LinkSuggest(Link link, String textFieldName, String valueFieldName) {
		super(textFieldName, valueFieldName);
		this.link = link;
	}

	public Link getLink() {
		return link;
	}

}
