package org.springframework.hateoas.forms;

import org.springframework.hateoas.Link;

/**
 * Suggested values of a {@link Property} that are loaded by a url returning a list of values.
 * 
 */
public class LinkSuggest extends AbstractSuggest {

	private Link link;

	public LinkSuggest(Link link, String textFieldName, String valueFieldName) {
		super(textFieldName, valueFieldName);
		this.link = link;
	}

	public Link getLink() {
		return link;
	}
}
