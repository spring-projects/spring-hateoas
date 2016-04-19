package org.springframework.hateoas.forms;

import org.springframework.web.util.UriComponentsBuilder;

public abstract class FormBuilderSupport<T extends TemplateBuilder> extends TemplateBuilderSupport<T> {

	public FormBuilderSupport(UriComponentsBuilder builder) {
		super(builder);
	}

	@Override
	public Form withKey(String key) {
		Form form = new Form(toString(), key);
		form.setBody(getBody());
		form.setProperties(getProperties());
		form.setMethod(getMethod());
		form.setContentType(getContentType());
		return form;
	}

	@Override
	public Form withDefaultKey() {
		return withKey(Template.DEFAULT_KEY);
	}

	public abstract Object getBody();

	public abstract String getContentType();

}
