package org.springframework.hateoas.forms;

/**
 * Builder for creating instances of {@link Suggest}
 * @see PropertyBuilder
 * @see TemplateBuilder
 */
public interface SuggestBuilder {
	public SuggestBuilder textField(String textFieldName);

	public SuggestBuilder valueField(String valueFieldName);

	public Suggest build();
}
