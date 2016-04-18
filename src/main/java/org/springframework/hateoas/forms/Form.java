package org.springframework.hateoas.forms;

import org.springframework.web.bind.annotation.RequestBody;

/**
 * HAL-FORMS {@link Template} that contains the argument marked as {@link RequestBody}
 *
 */
public class Form extends Template {

	private static final long serialVersionUID = -933494757445089955L;

	private Object body;

	public Form() {
	}

	public Form(String href, String rel) {
		super(href, rel);
	}

	public void setBody(Object body) {
		this.body = body;
	}

	public Object getBody() {
		return body;
	}
}
