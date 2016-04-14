package org.springframework.hateoas.forms;

/**
 * Define the "value" and "text" fields of an object included in a {@link Property} of a HAL-FORMS {@link Template}
 * 
 */
public interface Suggest {
	String getValueField();

	String getTextField();
}
