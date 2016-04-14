package org.springframework.hateoas.forms;

import java.util.Collection;

import org.springframework.util.Assert;

public class ValueSuggestBuilder<D> extends AbstractSuggestBuilder {

	protected Collection<D> values;

	private Class<?> componentType;

	public ValueSuggestBuilder(Collection<D> values) {
		this.values = values;
		if (!values.isEmpty()) {
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
