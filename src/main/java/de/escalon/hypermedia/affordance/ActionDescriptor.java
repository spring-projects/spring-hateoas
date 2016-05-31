package de.escalon.hypermedia.affordance;

import java.util.Collection;
import java.util.Map;

import de.escalon.hypermedia.action.Cardinality;

/**
 * Represents a descriptor for a http method execution. Created by Dietrich on 17.05.2015.
 */
public interface ActionDescriptor {

	/**
	 * Gets action name. Could be used as form name.
	 *
	 * @return name
	 */
	String getActionName();

	/**
	 * Gets method on uniform interface.
	 *
	 * @return method
	 */
	String getHttpMethod();

	/**
	 * Gets names of path variables, if URL has variables.
	 *
	 * @return names or empty collection
	 */
	Collection<String> getPathVariableNames();

	/**
	 * Gets names of expected request headers, if any.
	 *
	 * @return names or empty collection
	 */
	Collection<String> getRequestHeaderNames();

	/**
	 * Gets names of expected request parameters, if any.
	 *
	 * @return names or empty collection
	 */
	Collection<String> getRequestParamNames();

	/**
	 * Gets action parameter by name.
	 *
	 * @param name
	 * @return parameter
	 */
	ActionInputParameter getActionInputParameter(String name);

	/**
	 * Request body descriptor, if the action expects a complex request body.
	 *
	 * @return request body parameter
	 */
	ActionInputParameter getRequestBody();

	/**
	 * Does the action expect a complex request body?
	 *
	 * @return true if applicable
	 */
	boolean hasRequestBody();

	/**
	 * Gets well-defined semantic action type, e.g. http://schema.org/Action subtype.
	 *
	 * @return semantic action type
	 */
	String getSemanticActionType();

	/**
	 * Gets required parameters.
	 *
	 * @return required parameters, may be empty
	 */
	Map<String, ActionInputParameter> getRequiredParameters();

	/**
	 * Hints if the action response is a single object or a collection.
	 *
	 * @return cardinality
	 */
	Cardinality getCardinality();
}
