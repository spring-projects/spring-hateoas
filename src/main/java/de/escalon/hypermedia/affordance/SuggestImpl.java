package de.escalon.hypermedia.affordance;

import java.util.List;

public class SuggestImpl<T> implements Suggest<T> {

	private final T value;
	private final String valueField;
	private final String textField;
	private final SuggestType type;

	public SuggestImpl(T value, SuggestType type) {
		this(value, type, null, null);
	}

	public SuggestImpl(T value, SuggestType type, String valueField, String textField) {
		this.value = value;
		this.type = type;
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
	
	@Override
	public SuggestType getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
	public static <T> Suggest<T>[] wrap(List<T> list, String valueField, String textField, SuggestType type) {
		SuggestImpl<T>[] suggests = new SuggestImpl[list.size()];
		for (int index = 0; index < suggests.length; index++) {
			suggests[index] = new SuggestImpl<T>(list.get(index), type, valueField, textField);
		}
		return suggests;
	}
}
