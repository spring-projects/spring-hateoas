package de.escalon.hypermedia.affordance;

public class SimpleSuggest<T> extends SuggestImpl<SuggestObjectWrapper> {

	public SimpleSuggest(T origin, SuggestType type) {
		this(new SuggestObjectWrapper<T>(String.valueOf(origin), String.valueOf(origin), origin), type);
	}

	public SimpleSuggest(SuggestObjectWrapper<T> wrapper, SuggestType type) {
		super(wrapper, type, "id", "text");
	}

	public SimpleSuggest(String text, String value, SuggestType type) {
		super(new SuggestObjectWrapper<String>(text, value, value), type, "id", "text");
	}

	public SimpleSuggest(String text, String svalue, T value, SuggestType type) {
		super(new SuggestObjectWrapper<T>(text, svalue, value), type, "id", "text");
	}

	public static <T> Suggest<SuggestObjectWrapper>[] wrap(T[] values, SuggestType type) {
		@SuppressWarnings("unchecked")
		Suggest<SuggestObjectWrapper>[] suggests = new Suggest[values.length];
		for (int i = 0; i < suggests.length; i++) {
			suggests[i] = new SimpleSuggest<T>(values[i], type);
		}
		return suggests;
	}

}
