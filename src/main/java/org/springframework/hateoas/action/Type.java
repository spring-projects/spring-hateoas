package org.springframework.hateoas.action;

public enum Type {
	TEXT("text"), HIDDEN("hidden"), PASSWORD("password"), COLOR("color"), DATE("date"), DATETIME("datetime"), DATETIME_LOCAL(
			"datetime-local"), EMAIL("email"), MONTH("month"), NUMBER("number"), RANGE("range"), SEARCH("search"), TEL("tel"), TIME(
			"time"), URL("url"), WEEK("week");

	private String value;

	Type(String value) {
		this.value = value;
	}

	/**
	 * Returns the correct html input type string value.
	 */
	public String toString() {
		return value;
	}

}
