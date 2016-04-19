package org.springframework.hateoas.forms;

import org.springframework.util.Assert;

public class ValueSuggestBuilder<D> extends AbstractSuggestBuilder {

	protected Iterable<D> values;

	private Class<?> componentType;

	public ValueSuggestBuilder(Iterable<D> values) {
		this.values = values;
		if (values.iterator().hasNext()) {
			this.componentType = values.iterator().next().getClass();
		}
	}

	@Override
	public AbstractSuggestBuilder textField(String textFieldName) {
		Assert.notNull(FieldUtils.findFieldClass(componentType, textFieldName));
		return super.textField(textFieldName);
	}

	@Override
	public AbstractSuggestBuilder valueField(String valueFieldName) {
		Assert.notNull(FieldUtils.findFieldClass(componentType, valueFieldName));
		return super.valueField(valueFieldName);
	}

	@Override
	public Suggest build() {
		return new ValueSuggest<D>(values, textFieldName, valueFieldName);
	}
}
