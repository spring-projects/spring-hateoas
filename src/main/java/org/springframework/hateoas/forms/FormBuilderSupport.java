package org.springframework.hateoas.forms;

import java.util.List;

import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Base class to implement {@link FormBuilder}s based on a Spring MVC {@link UriComponentsBuilder}.
 * 
 */
public abstract class FormBuilderSupport<T extends FormBuilder> extends LinkBuilderSupport<T> implements FormBuilder {

	public FormBuilderSupport(UriComponentsBuilder builder) {
		super(builder);
	}

	@Override
	public Form withKey(String key) {
		Form form = new Form(toUri().normalize().toASCIIString(), key);
		form.setBody(getBody());
		form.setProperties(getProperties());
		form.setMethod(getMethod());
		return form;
	}

	@Override
	public Form withDefaultKey() {
		return withKey(Template.DEFAULT_KEY);
	}

	public abstract Object getBody();

	public abstract List<Property> getProperties();

	public abstract RequestMethod[] getMethod();

}
