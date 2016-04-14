package org.springframework.hateoas.forms;

/**
 * Abstract class that helps to construct {@link Suggest}
 * @see PropertyBuilder
 * @see FormBuilder
 */
public abstract class AbstractSuggestBuilder implements SuggestBuilder {

	protected String textFieldName;

	protected String valueFieldName;

	public AbstractSuggestBuilder textField(String textFieldName) {
		this.textFieldName = textFieldName;
		return this;
	}

	public AbstractSuggestBuilder valueField(String valueFieldName) {
		this.valueFieldName = valueFieldName;
		return this;
	}

	public abstract Suggest build();
}
