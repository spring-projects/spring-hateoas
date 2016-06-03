package de.escalon.hypermedia.affordance;

public class SuggestObjectWrapper<T> implements WrappedValue<T> {

	private final String text;
	private final String id;
	private final T original;

	public SuggestObjectWrapper(String text, String id, T original) {
		this.text = text;
		this.id = id;
		this.original = original;
	}

	public String getText() {
		return text;
	}

	public String getId() {
		return id;
	}

	@Override
	public T getValue() {
		return original;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SuggestObjectWrapper other = (SuggestObjectWrapper) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (text == null) {
			if (other.text != null) {
				return false;
			}
		} else if (!text.equals(other.text)) {
			return false;
		}
		return true;
	}

}
