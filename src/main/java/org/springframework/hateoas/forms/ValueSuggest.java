package org.springframework.hateoas.forms;

import org.springframework.hateoas.Resource;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Suggested values of a {@link Property} that are included into the response. There are two ways: include text/value
 * pairs into the "suggest" attribute of {@link Property} or include a reference to a _embedded element of
 * {@link Resource}
 * @see ValueSuggestType
 * 
 * @param <D>
 */
@JsonSerialize(using = ValueSuggestSerializer.class)
public class ValueSuggest<D> extends AbstractSuggest {

	private final Iterable<D> values;

	private final ValueSuggestType type;

	public ValueSuggest(Iterable<D> values, String textFieldName, String valueFieldName) {
		this(values, textFieldName, valueFieldName, ValueSuggestType.DIRECT);
	}

	public ValueSuggest(Iterable<D> values, String textFieldName, String valueFieldName, ValueSuggestType type) {
		super(textFieldName, valueFieldName);
		this.values = values;
		this.type = type;
	}

	public ValueSuggestType getType() {
		return type;
	}

	public Iterable<D> getValues() {
		return values;
	}

	/**
	 * Types of {@link ValueSuggest}
	 */
	public static enum ValueSuggestType {
		/**
		 * Values are serialized as a list into the "suggest" property {"suggest":[{"text":"...","value":"..."},...]}
		 */
		DIRECT,
		/**
		 * Values are serialized into the _embedded attribute of a {@link Resource} and "suggest.embedded" property
		 * references _embedded attribute {"suggest":{"embedded":"","text-field":"","value-field":""}}
		 */
		EMBEDDED
	}
}
