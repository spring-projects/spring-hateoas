/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.action;

/**
 * Allows to specify explicit HTML5 input types.
 *
 * @author Dietrich Schulten
 */
public enum Type {
	/**
	 * Determine input type text or number automatically, depending on the annotated parameter
	 */
	FROM_JAVA(null),
	/**
	 * input type text
	 */
	TEXT("text"),
	/**
	 * input type hidden
	 */
	HIDDEN("hidden"),
	/**
	 * input type password
	 */
	PASSWORD("password"),
	/**
	 * Color chooser
	 */
	COLOR("color"),
	/**
	 * Should contain a date, client may use date picker
	 */
	DATE("date"),
	/**
	 * Datetime widget, with timezone.
	 */
	DATETIME("datetime"),
	/**
	 * Datetime widget, no timezone.
	 */
	DATETIME_LOCAL("datetime-local"),
	/**
	 * Email address, may validate and improve touch entry.
	 */
	EMAIL("email"),
	/**
	 * Month/year selector.
	 */
	MONTH("month"),
	/**
	 * Numeric value, normally determined automatically. You can set restrictions on the numbers with {@link Input#max}, {@link Input#min} and {@link Input#step}.
	 */
	NUMBER("number"),
	/**
	 * Allowed range of values, use with {@link Input#max} and {@link Input#min}. Client may use slider.
	 */
	RANGE("range"),
	/**
	 * Search field, may add search entry support, e.g. a delete term widget.
	 */
	SEARCH("search"),
	/**
	 * Phone number
	 */
	TEL("tel"),
	/**
	 * Select time, may use time picker.
	 */
	TIME("time"),
	/**
	 * Field is a URL
	 */
	URL("url"),
	/**
	 * Week/Year selector
	 */
	WEEK("week"),
	/**
	 * Input type checkbox
	 */
	CHECKBOX("checkbox"),
	/**
	 * Input type radio
	 */
	RADIO("radio"),
	SUBMIT("submit");

	private String value;

	Type(String value) {
		this.value = value;
	}

	/**
	 * Returns the correct html input type string value, or null if type should be determined from Java type.
	 */
	public String toString() {
		return value;
	}

	public static Type fromInputType(String inputType) {
		Type[] values = Type.values();
		Type ret = null;
		for (Type type : values) {
			if (inputType.equals(type.value)) {
				ret = type;
				break;
			}
		}
		return ret;
	}

}
