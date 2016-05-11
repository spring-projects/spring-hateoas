package de.escalon.hypermedia.affordance;

public class SimpleSuggest<T> extends SuggestImpl<T> {
	
	public SimpleSuggest(T origin, SuggestType type) {
		super(origin, type, String.valueOf(origin), String.valueOf(origin));
	}
	
	public static <T> Suggest<T>[] wrap(T[] values, SuggestType type) {
		@SuppressWarnings("unchecked")
		Suggest<T> [] suggests = new Suggest[values.length];
		for (int i = 0; i < suggests.length; i++) {
			suggests[i] = new SimpleSuggest<T>(values[i], type);
		}
		return suggests;
	}

}
