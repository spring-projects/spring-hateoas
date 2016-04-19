package org.springframework.hateoas.forms;

import java.util.Arrays;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.forms.ValueSuggest.ValueSuggestType;

/**
 * Builder returned by {@link PropertyBuilder#suggest()} that provides different types of {@link SuggestBuilder}
 *
 */
public class SuggestBuilderProvider {

	private SuggestBuilder suggestBuilder;

	/**
	 * Returns a {@link SuggestBuilder} to construct a {@link ValueSuggest} of type {@link ValueSuggestType#DIRECT}
	 * @param values
	 * @return
	 */
	public <D> SuggestBuilder values(Iterable<D> values) {
		this.suggestBuilder = new ValueSuggestBuilder<D>(values);
		return suggestBuilder;
	}

	public <D> SuggestBuilder values(D[] values) {
		this.suggestBuilder = new ValueSuggestBuilder<D>(Arrays.asList(values));
		return suggestBuilder;
	}

	/**
	 * Returns a {@link SuggestBuilder} to construct a {@link ValueSuggest} of type {@link ValueSuggestType#EMBEDDED}
	 * @param values
	 * @return
	 */
	public <D> SuggestBuilder embedded(Iterable<D> values) {
		this.suggestBuilder = new EmbeddedSuggestBuilder<D>(values);
		return suggestBuilder;
	}

	/**
	 * Returns a {@link SuggestBuilder} to construct a {@link ValueSuggest} of type {@link ValueSuggestType#EMBEDDED}
	 * @param values
	 * @return
	 */
	public <D> SuggestBuilder embedded(D[] values) {
		this.suggestBuilder = new EmbeddedSuggestBuilder<D>(Arrays.asList(values));
		return suggestBuilder;
	}

	/**
	 * Returns a {@link SuggestBuilder} to construct a {@link LinkSuggest}
	 * @param link
	 * @return
	 */
	public SuggestBuilder link(Link link) {
		this.suggestBuilder = new LinkSuggestBuilder(link);
		return suggestBuilder;
	}

	public SuggestBuilder getSuggestBuilder() {
		return suggestBuilder;
	}

	public Suggest build() {
		return suggestBuilder != null ? suggestBuilder.build() : null;
	}
}
