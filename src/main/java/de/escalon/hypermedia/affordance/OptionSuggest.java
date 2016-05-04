package de.escalon.hypermedia.affordance;

import java.util.List;

public class OptionSuggest<T> implements Suggest<T> {

	private T value;
	private String valueField;
	private String textField;

	public OptionSuggest(T value) {
		this.value = value;
	}

	public OptionSuggest(T value, String valueField, String textField) {
		this.value = value;
		this.valueField = valueField;
		this.textField = textField;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public String getValueField() {
		return valueField;
	}

	@Override
	public String getTextField() {
		return textField;
	}

	@SuppressWarnings("unchecked")
	public static <T> OptionSuggest<T>[] wrap(List<T> list, String valueField, String textField) {
		OptionSuggest<T>[] suggests = new OptionSuggest[list.size()];
		for (int index = 0; index < suggests.length; index++) {
			suggests[index] = new OptionSuggest<T>(list.get(index), valueField, textField);
		}
		return suggests;
	}

}
