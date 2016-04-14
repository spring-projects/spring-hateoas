package org.springframework.hateoas.forms;

public class NotSupportedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotSupportedException(String message) {
		super(message);
	}

}
