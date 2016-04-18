package org.springframework.hateoas.forms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @see Suggest
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties({ "type" })
public class AbstractSuggest implements Suggest {

	@JsonProperty("prompt-field")
	protected final String textFieldName;

	@JsonProperty("value-field")
	protected final String valueFieldName;

	public AbstractSuggest(String textFieldName, String valueFieldName) {
		this.textFieldName = textFieldName;
		this.valueFieldName = valueFieldName;
	}

	@Override
	public String getValueField() {
		return valueFieldName;
	}

	@Override
	public String getTextField() {
		return textFieldName;
	}

}
