package org.springframework.hateoas.forms;

import org.springframework.hateoas.Link;

/**
 * Creates instances of {@link LinkSuggest}
 * 
 */
public class LinkSuggestBuilder extends AbstractSuggestBuilder {

	private final Link link;

	public LinkSuggestBuilder(Link link) {
		this.link = link;
	}

	@Override
	public Suggest build() {
		return new LinkSuggest(link, textFieldName, valueFieldName);
	}

}
