package org.springframework.hateoas.forms;

/**
 * Exception fired by {@link FieldUtils} when a class doesn't have a field of a specified name.
 *
 */
public class FieldNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 2591233443652872298L;

	private final Class<?> targetClass;

	private final String field;

	public FieldNotFoundException(Class<?> targetClass, String field) {
		this.targetClass = targetClass;
		this.field = field;
	}

	public Class<?> getTargetClass() {
		return targetClass;
	}

	public String getField() {
		return field;
	}

}
