package org.springframework.hateoas.forms;

/**
 * @see Suggest
 */
public class AbstractSuggest implements Suggest {

	protected final String textFieldName;

	protected final String valueFieldName;

	public AbstractSuggest(String textFieldName, String valueFieldName) {
		this.textFieldName = textFieldName;
		this.valueFieldName = valueFieldName;
	}

	@Override
	public String getValueField() {
		return valueFieldName;
	}

	@Override
	public String getTextField() {
		return textFieldName;
	}

}
