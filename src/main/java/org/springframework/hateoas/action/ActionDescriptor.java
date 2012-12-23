package org.springframework.hateoas.action;

import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.mvc.MethodParameterValue;

/**
 * Describes an HTTP action, e.g. for a form that calls a Controller method which handles the request built by the form.
 * 
 * @author Dietrich Schulten
 * 
 */
public class ActionDescriptor {

	private String actionLink;
	private Map<String, Class<?>> pathVariables = new HashMap<String, Class<?>>();
	private Map<String, MethodParameterValue> requestParams = new HashMap<String, MethodParameterValue>();
	private String httpMethod;
	private String resourceName;

	/**
	 * Creates an action descriptor. E.g. can be used to create HTML forms or Siren actions.
	 * 
	 * @param resourceName can be used by the action representation to identify the action using a form name.
	 * @param actionLink to which the action is submitted
	 * @param httpMethod used during submit
	 */
	public ActionDescriptor(String resourceName, String actionLink, String httpMethod) {
		this.actionLink = actionLink;
		this.httpMethod = httpMethod;
		this.resourceName = resourceName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public String getActionLink() {
		return actionLink;
	}

	public Map<String, Class<?>> getPathVariables() {
		return pathVariables;
	}

	public Map<String, MethodParameterValue> getRequestParams() {
		return requestParams;
	}

	public void addPathVariable(String key, Class<?> type) {
		pathVariables.put(key, type);
	}

	public void addRequestParam(String key, MethodParameterValue methodParameterValue) {
		requestParams.put(key, methodParameterValue);
	}

}
