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
	 * @return
	 */
	String getActionName();

	/**
	 * Gets method on uniform interface.
	 *
	 * @return
	 */
	String getHttpMethod();

	/**
	 * Gets contentType of the action
	 * @return
	 */
	String getContentType();

	/**
	 * Gets names of path variables, if URL has variables.
	 *
	 * @return
	 */
	Collection<String> getPathVariableNames();

	/**
	 * Gets names of expected request headers, if any.
	 *
	 * @return
	 */
	Collection<String> getRequestHeaderNames();

	/**
	 * Gets names of expected request parameters, if any.
	 *
	 * @return
	 */
	Collection<String> getRequestParamNames();

	/**
	 * Gets action parameter by name.
	 *
	 * @param name
	 * @return
	 */
	ActionInputParameter getActionInputParameter(String name);

	/**
	 * Request body descriptor, if the action expects a complex request body.
	 *
	 * @return
	 */
	ActionInputParameter getRequestBody();

	/**
	 * Does the action expect a complex request body?
	 *
	 * @return
	 */
	boolean hasRequestBody();

	/**
	 * Gets well-defined semantic action type, e.g. http://schema.org/Action subtype.
	 *
	 * @return
	 */
	String getSemanticActionType();

	/**
	 * Gets required parameters.
	 *
	 * @return
	 */
	Map<String, ActionInputParameter> getRequiredParameters();

	/**
	 * Hints if the action response is a single object or a collection.
	 *
	 * @return cardinality
	 */
	Cardinality getCardinality();

	/**
	 * Visits the body to find parameters
	 * @param visitor
	 */
	void accept(ActionInputParameterVisitor visitor);
}
