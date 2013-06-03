package org.springframework.hateoas.mvc;

import org.springframework.core.MethodParameter;

/**
 * Holds a method parameter value together with {@link MethodParameter} information.
 * 
 * @author Dietrich Schulten
 * 
 */
public class MethodParameterValue extends MethodParameter {

	private Object value;
	private String formattedValue;

	public MethodParameterValue(MethodParameter original, Object value, String formattedValue) {
		super(original);
		this.value = value;
		this.formattedValue = formattedValue;
	}

	/**
	 * The value of the parameter at invocation time.
	 * 
	 * @return value, may be null
	 */
	public Object getCallValue() {
		return value;
	}

	/**
	 * The value of the parameter at invocation time, formatted according to conversion configuration.
	 * 
	 * @return value, may be null
	 */
	public String getCallValueFormatted() {
		return formattedValue;
	}

}
