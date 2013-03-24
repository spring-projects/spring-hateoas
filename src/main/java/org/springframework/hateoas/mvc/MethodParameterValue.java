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

	public MethodParameterValue(MethodParameter original, Object value) {
		super(original);
		this.value = value;
	}

	/**
	 * The value of the parameter at invocation time.
	 * 
	 * @return value, may be null
	 */
	public Object getCallValue() {
		return value;
	}

}
