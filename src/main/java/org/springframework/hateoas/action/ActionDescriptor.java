package org.springframework.hateoas.action;

import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.mvc.MethodParameterValue;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Describes an HTTP action, e.g. for a form that calls a Controller method which handles the request built by the form.
 * 
 * @author Dietrich Schulten
 * 
 */
public class ActionDescriptor {

	private String actionLink;
	private Map<String, MethodParameterValue> requestParams = new HashMap<String, MethodParameterValue>();
	private RequestMethod httpMethod;
	private String resourceName;

	/**
	 * Creates an action descriptor. E.g. can be used to create HTML forms or Siren actions.
	 * 
	 * @param resourceName can be used by the action representation, e.g. to identify the action using a form name.
	 * @param actionLink to which the action is submitted
	 * @param requestMethod used during submit
	 */
	public ActionDescriptor(String resourceName, String actionLink, RequestMethod requestMethod) {
		this.actionLink = actionLink;
		this.httpMethod = requestMethod;
		this.resourceName = resourceName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public RequestMethod getHttpMethod() {
		return httpMethod;
	}

	public String getActionLink() {
		return actionLink;
	}


	public Map<String, MethodParameterValue> getRequestParams() {
		return requestParams;
	}


	public void addRequestParam(String key, MethodParameterValue methodParameterValue) {
		requestParams.put(key, methodParameterValue);
	}

}
