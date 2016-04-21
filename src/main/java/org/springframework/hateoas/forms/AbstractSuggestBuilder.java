package org.springframework.hateoas.forms;

/**
 * Abstract class that helps to construct {@link Suggest}
 * @see PropertyBuilder
 * @see TemplateBuilder
 */
public abstract class AbstractSuggestBuilder implements SuggestBuilder {

	String textFieldName;

	String valueFieldName;

	@Override
	public AbstractSuggestBuilder textField(String textFieldName) {
		this.textFieldName = textFieldName;
		return this;
	}

	@Override
	public AbstractSuggestBuilder valueField(String valueFieldName) {
		this.valueFieldName = valueFieldName;
		return this;
	}

	@Override
	public abstract Suggest build();
}
