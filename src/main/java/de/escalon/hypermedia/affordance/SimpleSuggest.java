package de.escalon.hypermedia.affordance;

public class SimpleSuggest<T> implements Suggest<T> {

	private final T origin;
	
	public SimpleSuggest(T origin) {
		this.origin = origin;
	}
	
	@Override
	public T getValue() {
		return origin;
	}

	@Override
	public String getTextField() {
		return origin.toString();
	}

	@Override
	public String getValueField() {
		return origin.toString();
	}
	
	public static <T> Suggest<T>[] wrap(T[] values) {
		@SuppressWarnings("unchecked")
		Suggest<T> [] suggests = new Suggest[values.length];
		for (int i = 0; i < suggests.length; i++) {
			suggests[i] = new SimpleSuggest<T>(values[i]);
		}
		return suggests;
	}

}
