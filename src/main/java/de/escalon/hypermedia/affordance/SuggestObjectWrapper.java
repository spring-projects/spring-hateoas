package de.escalon.hypermedia.affordance;

public class SuggestObjectWrapper {

	private final String text;
	private final String id;

	public SuggestObjectWrapper(String text, String id) {
		this.text = text;
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public String getId() {
		return id;
	}

}
