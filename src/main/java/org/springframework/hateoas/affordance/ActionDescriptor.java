/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.hateoas.affordance;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.affordance.formaction.Cardinality;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Represents a descriptor for a http method execution.
 *
 * @author Dietrich Schulten
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
	HttpMethod getHttpMethod();

	/**
	 * Gets contentType consumed by the action
	 *
	 * TODO: This is Spring MVC specific.
	 * 
	 * @return
	 */
	List<MediaType> getConsumes();

	/**
	 * Gets contentType produced by the action
	 *
	 * TODO: This is Spring MVC specific.
	 * 
	 * @return
	 */
	List<MediaType> getProduces();

	/**
	 * Gets names of path variables, if URL has variables.
	 *
	 * TODO: Possibly Spring MVC specific
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
	 * Gets all action input parameter names.
	 *
	 * @return
	 */
	Collection<String> getActionInputParameterNames();

	/**
	 * Get all of the {@link ActionInputParameter}s.
	 * 
	 * @return
	 */
	Collection<ActionInputParameter> getActionInputParameters();

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

	/**
	 * Visits the body to find parameters
	 * 
	 * @param visitor
	 */
	void accept(ActionInputParameterVisitor visitor);
}
