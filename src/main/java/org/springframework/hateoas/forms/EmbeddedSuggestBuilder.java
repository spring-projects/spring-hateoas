package org.springframework.hateoas.forms;

import org.springframework.hateoas.forms.ValueSuggest.ValueSuggestType;

/**
 * Builds {@link ValueSuggest} of type {@link ValueSuggestType#EMBEDDED}
 *
 */
public class EmbeddedSuggestBuilder<D> extends ValueSuggestBuilder<D> {

	public EmbeddedSuggestBuilder(Iterable<D> values) {
		super(values);
	}

	@Override
	public Suggest build() {
		return new ValueSuggest<D>(values, textFieldName, valueFieldName, ValueSuggestType.EMBEDDED);
	}
}
