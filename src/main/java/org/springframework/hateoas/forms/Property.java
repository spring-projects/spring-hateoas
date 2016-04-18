package org.springframework.hateoas.forms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Describe a parameter for the associated state transition in a HAL-FORMS document. A {@link Template} may contain a
 * list of {@link Property}
 * 
 * @see http://mamund.site44.com/misc/hal-forms/
 * 
 */
@JsonInclude(Include.NON_EMPTY)
public class Property {

	private String name;

	private Boolean readOnly;

	private String value;

	private String prompt;

	private String regex;

	private boolean templated;

	private Suggest suggest;

	public Property(String name, Boolean readOnly, boolean templated, String value, String prompt, String regex,
			Suggest suggest) {
		this.name = name;
		this.readOnly = readOnly;
		this.templated = templated;
		this.value = value;
		this.prompt = prompt;
		this.regex = regex;
		this.suggest = suggest;
	}

	public String getName() {
		return name;
	}

	public Boolean isReadOnly() {
		return readOnly;
	}

	public String getValue() {
		return value;
	}

	public String getPrompt() {
		return prompt;
	}

	public String getRegex() {
		return regex;
	}

	public boolean isTemplated() {
		return templated;
	}

	public Suggest getSuggest() {
		return suggest;
	}
}
