package org.springframework.hateoas.forms;

/**
 * Builder for creating instances of {@link Suggest}
 * @see PropertyBuilder
 * @see TemplateBuilder
 */
public interface SuggestBuilder {
	SuggestBuilder textField(String textFieldName);

	SuggestBuilder valueField(String valueFieldName);

	Suggest build();
}
