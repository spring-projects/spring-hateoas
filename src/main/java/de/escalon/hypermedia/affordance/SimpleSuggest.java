package de.escalon.hypermedia.affordance;

public class SimpleSuggest<T> extends SuggestImpl<SuggestObjectWrapper> {

	public SimpleSuggest(T origin, SuggestType type) {
		super(new SuggestObjectWrapper(String.valueOf(origin), String.valueOf(origin)), type, "id", "text");
	}

	public SimpleSuggest(String text, String value, SuggestType type) {
		super(new SuggestObjectWrapper(text, value), type, "id", "text");
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
